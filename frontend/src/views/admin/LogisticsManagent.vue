<template>
  <div class="page-container">
    <div class="page-header">
      <h2>物流管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" inline style="margin-bottom: 20px">
      <el-form-item label="订单号">
        <el-input v-model="queryParams.orderSn" placeholder="请输入订单号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="物流单号">
        <el-input v-model="queryParams.deliveryNo" placeholder="请输入物流单号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="物流公司">
        <el-select v-model="queryParams.deliveryCompany" placeholder="请选择" clearable style="width: 160px">
          <el-option label="顺丰" value="SF" />
          <el-option label="圆通" value="YTO" />
          <el-option label="中通" value="ZTO" />
          <el-option label="申通" value="STO" />
          <el-option label="韵达" value="YD" />
        </el-select>
      </el-form-item>
      <el-form-item label="物流状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 160px">
          <el-option label="待发货" :value="0" />
          <el-option label="已发货" :value="1" />
          <el-option label="运输中" :value="2" />
          <el-option label="已签收" :value="3" />
          <el-option label="异常" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getLogisticsList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 物流列表 -->
    <el-table :data="paginatedList" border stripe v-loading="loading">
      <el-table-column label="物流信息" width="300">
        <template #default="{ row }">
          <div class="logistics-info">
            <div class="info-line">
              <span class="label">物流单号：</span>{{ row.deliveryNo }}
            </div>
            <div class="info-line">
              <span class="label">订单号：</span>{{ row.orderSn }}
            </div>
            <div class="info-line">
              <span class="label">创建时间：</span>{{ formatTime(row.createTime) }}
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="物流公司" align="center">
        <template #default="{ row }">
          {{ getCompanyName(row.deliveryCompany) }}
        </template>
      </el-table-column>

      <el-table-column label="收件人" align="center">
        <template #default="{ row }">
          <div>{{ row.receiver }}</div>
          <div style="color: #999; font-size: 12px">{{ row.phone }}</div>
        </template>
      </el-table-column>

      <el-table-column label="物流状态" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagMap[row.status]?.type">
            {{ statusTagMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="openTraceDialog(row)">查看轨迹</el-button>
          <el-button 
            type="warning" 
            link 
            @click="openAddTraceDialog(row)"
            v-if="row.status === 1 || row.status === 2"
          >
            添加轨迹
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-box" style="text-align: right; margin-top: 20px">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getLogisticsList"
        @current-change="getLogisticsList"
      />
    </div>

    <!-- 查看轨迹弹窗 -->
    <el-dialog v-model="traceDialogVisible" title="物流轨迹" width="600px">
      <el-timeline v-if="currentLogistics.traces?.length">
        <el-timeline-item
          v-for="trace in currentLogistics.traces"
          :key="trace.id"
          :timestamp="formatTime(trace.createTime)"
          placement="top"
        >
          {{ trace.content }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无物流轨迹" />
    </el-dialog>

    <!-- 添加轨迹弹窗 -->
    <el-dialog v-model="addTraceDialogVisible" title="添加物流轨迹" width="500px">
      <el-form :model="addTraceForm" label-width="100px">
        <el-form-item label="轨迹描述">
          <el-input 
            v-model="addTraceForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入物流节点信息（如：包裹已到达XX市分拣中心，正在发往XX地）"
            class="trace-textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addTraceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddTrace">添加轨迹</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAllLogisticsListService,
  addLogisticsTraceService,
  getLogisticsDetailService
} from '@/api/admin/logistics.js'
import {sendNoticeToUserService} from '@/api/admin/notice.js'

const loading = ref(false)
const logisticsList = ref([])

// 搜索条件
const queryParams = ref({
  orderSn: '',
  deliveryNo: '',
  deliveryCompany: '',
  status: null
})

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = computed(() => logisticsList.value.length)
const paginatedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  return logisticsList.value.slice(start, end)
})

// 状态标签映射
const statusTagMap = {
  0: { text: '待发货', type: 'warning' },
  1: { text: '已发货', type: 'primary' },
  2: { text: '运输中', type: 'primary' },
  3: { text: '已签收', type: 'success' },
  4: { text: '异常', type: 'danger' }
}

// 快递公司映射
const companyMap = {
  SF: '顺丰速运',
  YTO: '圆通快递',
  ZTO: '中通快递',
  STO: '申通快递',
  YD: '韵达快递'
}

// 查看轨迹弹窗
const traceDialogVisible = ref(false)
const currentLogistics = ref({ traces: [] })

// 添加轨迹弹窗
const addTraceDialogVisible = ref(false)
const addTraceForm = ref({
  logisticsId: null,
  content: ''
})

onMounted(() => {
  getLogisticsList()
})

// 获取物流列表（带搜索）
const getLogisticsList = async () => {
  loading.value = true
  try {
    const res = await getAllLogisticsListService(queryParams.value)
    logisticsList.value = res.data || []
    pageNum.value = 1
  } catch (err) {
    ElMessage.error('获取物流列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    orderSn: '',
    deliveryNo: '',
    deliveryCompany: '',
    status: null
  }
  getLogisticsList()
}

// 格式化时间
const formatTime = (time) => time ? String(time).replace('T', ' ') : '--'

// 获取快递公司名称
const getCompanyName = (code) => companyMap[code] || code

// 查看物流轨迹
const openTraceDialog = async (row) => {
  const res = await getLogisticsDetailService(row.orderId)
  currentLogistics.value = res.data || { traces: [] }
  traceDialogVisible.value = true
}

// 打开添加轨迹弹窗
const openAddTraceDialog = async (row) => {
  addTraceForm.value = { logisticsId: row.id, content: '' }
  try {
    const res = await getLogisticsDetailService(row.orderId)
    currentLogistics.value = res.data || {} 
  } catch (err) {
    console.error('获取物流详情失败', err)
  }
  
  addTraceDialogVisible.value = true
}

// 提交添加轨迹
const submitAddTrace = async () => {
  if (!addTraceForm.value.content) {
    ElMessage.warning('请输入轨迹描述')
    return
  }
  
  try {
    await addLogisticsTraceService(addTraceForm.value)
    
    // 添加轨迹成功后，给下单用户发送站内信通知
    try {
      const userId = currentLogistics.value.userId
      const orderSn = currentLogistics.value.orderSn
      
      if (userId) {
        await sendNoticeToUserService({
          userId: userId, 
          noticeType: 8, 
          title: '物流状态更新',
          content: `您的订单 ${orderSn} 有新的物流动态：${addTraceForm.value.content}`,
          bizId: String(addTraceForm.value.logisticsId) 
        })
      }
    } catch (msgErr) {
      console.error('发送轨迹更新通知失败:', msgErr)
    }

    ElMessage.success('轨迹添加成功')
    addTraceDialogVisible.value = false
    getLogisticsList()
  } catch (err) {
    ElMessage.error('添加失败')
  }
}
</script>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}
.logistics-info {
  line-height: 1.8;
}
.logistics-info .label {
  color: #666;
  font-size: 13px;
}
.info-line {
  font-size: 13px;
}

.trace-textarea {
  --el-input-textarea-border-color: #dcdfe6;
  --el-input-textarea-hover-border-color: #409eff;
  border-radius: 8px;
  transition: all 0.3s;
}
.trace-textarea:focus-within {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}
</style>