<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息通知</h2>
      <div>
        <el-button type="success" :loading="markAllLoading" @click="handleMarkAllRead">全部已读</el-button>
      </div>
    </div>

    <!-- 搜索 -->
    <el-form :model="queryParams" inline style="margin-bottom: 20px">
      <el-form-item label="消息类型">
        <el-select v-model="queryParams.noticeType" placeholder="全部" clearable style="width:150px">
          <el-option
            v-for="type in typeList"
            :key="type.id"
            :label="type.typeName"
            :value="type.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="阅读状态">
        <el-select v-model="queryParams.isRead" placeholder="全部" clearable style="width:120px">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column label="消息标题" prop="title" min-width="200">
        <template #default="{ row }">
          <span
            :style="row.isRead === 0 ? 'font-weight:bold' : ''"
            @click="toDetail(row)"
            style="cursor:pointer;color:#409eff"
          >
            {{ row.title }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="类型" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.noticeType)" size="small">
            {{ row.noticeTypeName }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'success' : 'warning'" size="small">
            {{ row.isRead ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="时间" prop="createTime" align="center" width="180" />

      <el-table-column label="操作" align="center" width="180">
        <template #default="{ row }">
          <el-button type="primary" link @click="toDetail(row)">查看详情</el-button>
          <el-button type="danger" link :loading="deleteLoading[row.id]" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      style="margin-top:20px; text-align:right"
      @size-change="handleSizeChange"
      @current-change="getList"
    />

    <!-- 消息详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="500px">
      <div v-if="detailInfo" class="detail-box">
        <h3>{{ detailInfo.title }}</h3>
        <div class="meta">
          <el-tag :type="getTypeTag(detailInfo.noticeType)" size="small">
            {{ detailInfo.noticeTypeName }}
          </el-tag>
          <span style="margin-left: 10px">{{ detailInfo.createTime }}</span>
        </div>
        <el-divider />
        <div class="detail-content">{{ detailInfo.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNoticeTypesService,
  getAdminNoticeListService,
  getAdminNoticeDetailService,
  markAdminNoticeReadService,
  deleteAdminNoticeService
} from '@/api/admin/notice.js'

const loading = ref(false)
const markAllLoading = ref(false)
const deleteLoading = reactive({})

const list = ref([])
const total = ref(0)
const typeList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  noticeType: undefined,
  isRead: undefined,
  title: undefined
})

// 详情弹窗
const detailVisible = ref(false)
const detailInfo = ref(null)

// 初始化：并行加载消息类型和列表
onMounted(async () => {
  await Promise.all([getTypeList(), getList()])
})

// 获取消息类型
const getTypeList = async () => {
  try {
    const res = await getNoticeTypesService()
    typeList.value = res.data || []
  } catch (e) {
    console.error('获取消息类型失败', e)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      noticeType: queryParams.noticeType ?? null,
      isRead: queryParams.isRead ?? null,
      title: queryParams.title || null
    }

    const res = await getAdminNoticeListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      list.value = pageData.records || pageData.list || pageData.rows || []
      total.value = pageData.total || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (e) {
    console.error('获取消息列表失败', e)
    ElMessage.error('获取消息列表失败')
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

// 分页大小变化时重置到第一页
const handleSizeChange = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    noticeType: undefined,
    isRead: undefined,
    title: undefined
  })
  getList()
}

const toDetail = async (row) => {
  try {
    const res = await getAdminNoticeDetailService({ noticeId: row.id })
    detailInfo.value = res.data
    detailVisible.value = true

    // 未读消息点击后自动标记为已读
    if (row.isRead === 0) {
      try {
        await markAdminNoticeReadService({ ids: [row.id] })
        row.isRead = 1
      } catch (err) {
        console.error('标记已读失败', err)
      }
    }
  } catch (err) {
    ElMessage.error('获取消息详情失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该消息？', '提示', { type: 'warning' })

    deleteLoading[id] = true
    try {
      await deleteAdminNoticeService({ noticeId: id })
      ElMessage.success('删除成功')
      if (list.value.length === 1 && queryParams.pageNum > 1) {
        queryParams.pageNum--
      }
      getList()
    } finally {
      deleteLoading[id] = false
    }
  } catch (err) {
    if (err !== 'cancel') console.error('删除失败', err)
  }
}

const handleMarkAllRead = async () => {
  markAllLoading.value = true
  try {
    // 先查询当前筛选条件下所有未读消息的 ID
    const unreadRes = await getAdminNoticeListService({
      pageNum: 1,
      pageSize: 9999,
      isRead: 0,
      noticeType: queryParams.noticeType ?? null,
      title: queryParams.title || null
    })

    const pageData = unreadRes.data || {}
    const unreadList = pageData.records || pageData.list || pageData.rows || []
    const unreadIds = unreadList.map(i => i.id)

    if (!unreadIds.length) {
      ElMessage.warning('暂无未读消息')
      return
    }

    await markAdminNoticeReadService({ ids: unreadIds })
    ElMessage.success(`已将 ${unreadIds.length} 条消息标记为已读`)
    getList()
  } catch (err) {
    console.error('全部已读失败', err)
    ElMessage.error('全部已读失败')
  } finally {
    markAllLoading.value = false
  }
}

// 消息类型标签样式
const getTypeTag = (t) => ({ 1: 'primary', 2: 'warning', 3: 'danger', 4: 'info', 5: 'success' }[t] || 'info')
</script>

<style scoped>
.page-container { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
h2 { margin: 0; font-size: 18px; color: #1f2937; }

.detail-box { line-height: 1.7; }
.detail-box h3 { margin: 0 0 12px; font-size: 16px; color: #303133; }
.meta { color: #666; font-size: 13px; display: flex; align-items: center; }
.detail-content { padding: 10px 0; font-size: 14px; color: #333; white-space: pre-wrap; }
</style>