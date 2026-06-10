<template>
  <div class="notice-page">
    <div class="container">
      <!-- 顶部标题与操作栏 -->
      <div class="header-bar">
        <h2 class="page-title">消息中心</h2>
        <div class="header-actions">
          <span class="unread-tip" v-if="unreadCount > 0">
            你有 <em>{{ unreadCount }}</em> 条未读消息
          </span>
          <el-button 
            type="primary" 
            link 
            @click="handleMarkAllRead" 
            :disabled="unreadCount === 0 || loading"
          >
            全部标为已读
          </el-button>
        </div>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-box">
        <el-select v-model="query.noticeType" placeholder="消息类型" clearable class="filter-item">
          <el-option label="全部类型" :value="null" />
          <el-option
            v-for="type in typeList"
            :key="type.id"
            :label="type.typeName"
            :value="type.id"
          />
        </el-select>
        <el-select v-model="query.isRead" placeholder="阅读状态" clearable class="filter-item">
          <el-option label="全部状态" :value="null" />
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <!-- 消息列表 -->
      <div class="list-container" v-loading="loading">
        <div
          v-for="item in noticeList"
          :key="item.id"
          class="notice-card"
          :class="{ 'is-unread': item.isRead === 0 }"
          @click="openDetail(item)"
        >
          <div class="card-left">
            <div class="status-dot" v-if="item.isRead === 0"></div>
            <div class="card-content">
              <div class="card-header">
                <span class="card-title">{{ item.title }}</span>
                <el-tag size="small" effect="plain" class="type-tag">
                  {{ item.noticeTypeName }}
                </el-tag>
              </div>
              <div class="card-desc">{{ item.content }}</div>
              <div class="card-footer">{{ item.createTime }}</div>
            </div>
          </div>
          <el-button 
            class="delete-btn" 
            type="danger" 
            link 
            icon="Delete"
            @click.stop="handleDelete(item)"
          >
            删除
          </el-button>
        </div>

        <el-empty v-if="noticeList.length === 0 && !loading" description="暂无相关消息" />
      </div>

      <!-- ✅ 新增：分页组件 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </div>

    <!-- 消息详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="560px" destroy-on-close>
      <div v-if="detailInfo" class="detail-wrapper">
        <h3 class="detail-title">{{ detailInfo.title }}</h3>
        <div class="detail-meta">
          <el-tag size="small" effect="plain">{{ detailInfo.noticeTypeName }}</el-tag>
          <span class="detail-time">{{ detailInfo.createTime }}</span>
        </div>
        <el-divider />
        <div class="detail-text">{{ detailInfo.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import {
  getNoticeTypesService,
  getUserNoticeListService,
  getUserNoticeDetailService,
  getUserUnreadCountService,
  markUserNoticeReadService,
  deleteUserNoticeService
} from '@/api/user/notice'

const loading = ref(false)
const noticeList = ref([])
const unreadCount = ref(0)
const typeList = ref([])
const total = ref(0)       // ✅ 新增：总条数
const pageNum = ref(1)     // ✅ 新增：当前页码
const pageSize = ref(10)   // ✅ 新增：每页条数

const query = ref({
  noticeType: null,
  isRead: null
})

// 详情弹窗状态
const detailVisible = ref(false)
const detailInfo = ref(null)

onMounted(() => {
  getTypeList()
  getList()
  getUnread()
})

// 获取消息类型字典
const getTypeList = async () => {
  try {
    const res = await getNoticeTypesService()
    typeList.value = res.data || []
  } catch (error) {
    console.error('获取消息类型失败', error)
  }
}

// ✅ 修复：获取消息列表 - 补充分页参数 & 正确解析PageVo
const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      noticeType: query.value.noticeType,
      isRead: query.value.isRead
    }
    const res = await getUserNoticeListService(params)
    const pageData = res.data || {}
    
    // 兼容常见的 PageVo 字段名（records / list / rows）
    noticeList.value = pageData.records || pageData.list || pageData.rows || []
    total.value = pageData.total || 0
  } catch (error) {
    console.error('获取消息列表失败', error)
    noticeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ✅ 新增：搜索时重置到第一页
const handleSearch = () => {
  pageNum.value = 1
  getList()
}

// 获取未读数量
const getUnread = async () => {
  try {
    const res = await getUserUnreadCountService()
    unreadCount.value = res.data?.unreadCount || 0
  } catch (error) {
    console.error('获取未读数失败', error)
  }
}

// 打开详情并标记已读
const openDetail = async (item) => {
  try {
    const res = await getUserNoticeDetailService(item.id)
    detailInfo.value = res.data
    detailVisible.value = true

    if (item.isRead === 0) {
      await markUserNoticeReadService([item.id])
      item.isRead = 1
      getUnread()
    }
  } catch (error) {
    ElMessage.error('获取消息详情失败')
  }
}

// 标记全部已读
const handleMarkAllRead = async () => {
  try {
    await markUserNoticeReadService(null)
    ElMessage.success('已全部标为已读')
    getList()
    getUnread()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除单条消息
const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确认要删除这条消息吗？', '提示', {
      type: 'warning'
    })
    await deleteUserNoticeService(item.id)
    ElMessage.success('删除成功')
    getList()
    getUnread()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ✅ 修复：重置时同时重置分页
const resetQuery = () => {
  query.value = { noticeType: null, isRead: null }
  pageNum.value = 1
  getList()
}
</script>

<style scoped>
/* 补充分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-bottom: 20px;
}
</style>

<style scoped>
/* 基础容器 */
.notice-page {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 顶部标题栏 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.unread-tip {
  font-size: 14px;
  color: #606266;
}

.unread-tip em {
  color: #f56c6c;
  font-style: normal;
  font-weight: bold;
  margin: 0 4px;
}

/* 筛选区 */
.filter-box {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.filter-item {
  width: 160px;
}

/* 消息列表卡片 */
.list-container {
  min-height: 500px;
}

.notice-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notice-card:last-child {
  border-bottom: none;
}

.notice-card:hover {
  background-color: #f9fafc;
}

/* 未读状态高亮 */
.notice-card.is-unread {
  background-color: #fdfdfd;
}

.card-left {
  display: flex;
  align-items: flex-start;
  flex: 1;
  min-width: 0; 
}

.status-dot {
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
  margin-top: 6px;
  margin-right: 12px;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.is-unread .card-title {
  font-weight: 600;
}

.type-tag {
  margin-left: 10px;
  font-weight: normal;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.card-footer {
  font-size: 12px;
  color: #c0c4cc;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  margin-left: 16px;
}

.notice-card:hover .delete-btn {
  opacity: 1;
}

/* 详情弹窗样式 */
.detail-wrapper {
  padding: 0 10px;
}

.detail-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.detail-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.detail-time {
  margin-left: 12px;
}

.detail-text {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>