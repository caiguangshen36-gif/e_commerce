<template>
  <div class="log-page">
    <el-card shadow="never">
      <!-- 1. 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form" size="default">
        <el-form-item label="用户ID">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="操作描述">
          <el-input
            v-model="queryParams.operation"
            placeholder="请输入操作描述"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="logList"
        style="width: 100%; margin-top: 10px"
        border
        stripe
        v-loading="loading"
      >
        <el-table-column prop="id" label="日志ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="operation" label="操作行为" min-width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="120" align="center" />
        <el-table-column prop="params" label="请求参数" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="180" align="center" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperLogList } from '@/api/admin/operLog.js'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  operation: '',
  startTime: null,
  endTime: null
})

const fetchData = async () => {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.value.startTime = dateRange.value[0]
      queryParams.value.endTime = dateRange.value[1]
    } else {
      queryParams.value.startTime = null
      queryParams.value.endTime = null
    }

    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      userId: queryParams.value.userId || null,
      operation: queryParams.value.operation || null,
      startTime: queryParams.value.startTime,
      endTime: queryParams.value.endTime
    }

    const res = await getOperLogList(params)

    if (res?.code === 200 || res?.success) {
      const pageData = res.data || {}
      logList.value = pageData.records || pageData.list || pageData.rows || []
      total.value = pageData.total || 0
    } else {
      ElMessage.error(res.msg || '获取日志数据失败')
      logList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取日志列表出错:', error)
    ElMessage.error('请求出错，请稍后重试')
    logList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

const handleSizeChange = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userId: '',
    operation: '',
    startTime: null,
    endTime: null
  }
  dateRange.value = []
  fetchData()
}

// ==================== 生命周期 ====================
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.log-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.search-form {
  padding-bottom: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>