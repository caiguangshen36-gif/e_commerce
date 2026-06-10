<template>
  <div class="page-container">
    <div class="page-header">
      <h2>售后管理</h2>
    </div>

    <!-- 筛选栏 -->
    <el-form :model="queryParams" inline style="margin-bottom: 20px">
      <el-form-item label="售后单号">
        <el-input v-model="queryParams.afterSaleSn" placeholder="请输入售后单号" clearable style="width: 220px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="售后状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 180px">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="已退款" :value="2" />
          <el-option label="驳回" :value="3" />
          <el-option label="用户已退货" :value="4" />
          <el-option label="商家收货完成" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="afterSaleList" border stripe v-loading="loading">
      <el-table-column label="售后信息" width="320">
        <template #default="{ row }">
          <div class="info-item">
            <div class="sn">售后单号：{{ row.afterSaleSn }}</div>
            <div class="order-id">订单ID：{{ row.orderId }}</div>
            <div class="time">申请时间：{{ formatTime(row.createTime) }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="售后类型" align="center">
        <template #default="{ row }">
          <el-tag>{{ getTypeText(row.type) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="退款金额" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">¥{{ Number(row.refundAmount || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="320">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewDetail(row)">查看详情</el-button>

          <el-button v-if="row.status === 0" type="success" link :loading="actionLoading[row.id + '_approve']" @click="handleApprove(row)">
            审核通过
          </el-button>

          <el-button v-if="row.status === 0" type="danger" link @click="openRejectDialog(row)">
            驳回
          </el-button>

          <el-button v-if="row.status === 4" type="warning" link :loading="actionLoading[row.id + '_receive']" @click="handleReceive(row)">
            确认收货
          </el-button>

          <el-button v-if="row.status === 5 || (row.status === 1 && row.type === 1)" type="primary" link :loading="actionLoading[row.id + '_refund']" @click="handleRefund(row)">
            退款
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-box" style="text-align: right; margin-top: 20px">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="getAfterSaleList"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="售后详情" width="700px">
      <div v-if="currentAfterSale">
        <div class="info-card">
          <div class="card-title">基本信息</div>
          <div class="info-row"><span>售后单号：</span>{{ currentAfterSale.afterSaleSn }}</div>
          <div class="info-row"><span>订单ID：</span>{{ currentAfterSale.orderId }}</div>
          <div class="info-row"><span>售后类型：</span>{{ getTypeText(currentAfterSale.type) }}</div>
          <div class="info-row"><span>申请时间：</span>{{ formatTime(currentAfterSale.createTime) }}</div>
          <div class="info-row" v-if="currentAfterSale.auditTime"><span>审核时间：</span>{{ formatTime(currentAfterSale.auditTime) }}</div>
          <div class="info-row" v-if="currentAfterSale.refundTime"><span>退款时间：</span>{{ formatTime(currentAfterSale.refundTime) }}</div>
        </div>

        <div class="info-card">
          <div class="card-title">问题描述</div>
          <div class="info-row"><span>售后原因：</span>{{ currentAfterSale.reason }}</div>
          <div class="info-row"><span>详细描述：</span>{{ currentAfterSale.description }}</div>
        </div>

        <div class="info-card">
          <div class="card-title">退款信息</div>
          <div class="info-row"><span>退款金额：</span><span style="color: #f56c6c; font-weight: bold">¥{{ Number(currentAfterSale.refundAmount || 0).toFixed(2) }}</span></div>
        </div>

        <div class="info-card" v-if="currentAfterSale.delivery">
          <div class="card-title">退货物流</div>
          <div class="info-row"><span>快递公司：</span>{{ currentAfterSale.delivery.deliveryCompany }}</div>
          <div class="info-row"><span>运单号：</span>{{ currentAfterSale.delivery.deliveryNo }}</div>
          <div class="info-row"><span>发货时间：</span>{{ formatTime(currentAfterSale.delivery.sendTime) }}</div>
          <div class="info-row"><span>收货时间：</span>{{ formatTime(currentAfterSale.delivery.receiveTime) }}</div>
        </div>

        <div class="info-card" v-if="currentAfterSale.rejectReason">
          <div class="card-title">驳回信息</div>
          <div class="info-row"><span>驳回原因：</span>{{ currentAfterSale.rejectReason }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回售后申请" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.rejectReason" type="textarea" rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import {
  getAllAfterSaleListService,
  getAfterSaleDetailService,
  approveAfterSaleService,
  rejectAfterSaleService,
  receiveAfterSaleGoodsService,
  refundAfterSaleService
} from '@/api/admin/aftersale.js'
import { sendNoticeToUserService } from '@/api/admin/notice.js'

const route = useRoute()

const loading = ref(false)
const rejectLoading = ref(false)
const actionLoading = reactive({})

const afterSaleList = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentAfterSale = ref(null)
const currentRejectId = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  afterSaleSn: '',
  status: undefined
})

const rejectForm = ref({
  rejectReason: ''
})

// 接收控制台卡片跳转的筛选条件
onMounted(() => {
  const { status } = route.query

  if (status !== undefined && status !== '') {
    queryParams.value.status = Number(status)
  }

  getAfterSaleList()
})

const handleSearch = () => {
  queryParams.value.pageNum = 1
  getAfterSaleList()
}

const handleSizeChange = () => {
  queryParams.value.pageNum = 1
  getAfterSaleList()
}

// 传递分页参数
const getAfterSaleList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      afterSaleSn: queryParams.value.afterSaleSn || null,
      status: queryParams.value.status ?? null
    }

    const res = await getAllAfterSaleListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      afterSaleList.value = pageData.records || pageData.list || pageData.rows || []
      total.value = pageData.total || 0
    } else {
      afterSaleList.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取售后列表失败', err)
    ElMessage.error('获取售后列表失败')
    afterSaleList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    afterSaleSn: '',
    status: undefined
  }
  getAfterSaleList()
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const res = await getAfterSaleDetailService(row.id)
    currentAfterSale.value = res.data
    detailDialogVisible.value = true
  } catch (err) {
    ElMessage.error('获取售后详情失败')
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定审核通过该售后申请？', '提示', { type: 'warning' })

    const loadingKey = row.id + '_approve'
    actionLoading[loadingKey] = true

    try {
      await approveAfterSaleService(row.id)

      const userId = row.userId
      if (userId) {
        sendNoticeToUserService({
          userId,
          noticeType: 10,
          title: '售后申请已通过',
          content: `您的售后单 ${row.afterSaleSn} 已审核通过，状态变更为：${getStatusText(1)}。`,
          bizId: String(row.id)
        }).catch(msgErr => console.error('发送售后审核通知失败:', msgErr))
      }

      ElMessage.success('审核通过')
      getAfterSaleList()
    } finally {
      actionLoading[loadingKey] = false
    }
  } catch (err) {
    if (err !== 'cancel') console.error('审核失败:', err)
  }
}

// 打开驳回弹窗
const openRejectDialog = (row) => {
  currentRejectId.value = row.id
  rejectForm.value.rejectReason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.value.rejectReason) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  rejectLoading.value = true
  try {
    await rejectAfterSaleService(currentRejectId.value, rejectForm.value.rejectReason)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    getAfterSaleList()
  } catch (err) {
    ElMessage.error('驳回失败')
  } finally {
    rejectLoading.value = false
  }
}

const handleReceive = async (row) => {
  try {
    await ElMessageBox.confirm('确认已收到用户退回的商品？')

    const loadingKey = row.id + '_receive'
    actionLoading[loadingKey] = true

    try {
      await receiveAfterSaleGoodsService(row.id)
      ElMessage.success('已确认收货')
      getAfterSaleList()
    } finally {
      actionLoading[loadingKey] = false
    }
  } catch (err) {
    if (err !== 'cancel') console.error('确认收货失败:', err)
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确认为该订单退款？', '提示', { type: 'warning' })

    const loadingKey = row.id + '_refund'
    actionLoading[loadingKey] = true

    try {
      await refundAfterSaleService(row.id)

      const userId = row.userId
      if (userId) {
        sendNoticeToUserService({
          userId,
          noticeType: 11,
          title: '退款已完成',
          content: `您的售后单 ${row.afterSaleSn} 已完成退款，金额：¥${Number(row.refundAmount || 0).toFixed(2)}。请留意查收。`,
          bizId: String(row.id)
        }).catch(msgErr => console.error('发送退款通知失败:', msgErr))
      }

      ElMessage.success('退款成功')
      getAfterSaleList()
    } finally {
      actionLoading[loadingKey] = false
    }
  } catch (err) {
    if (err !== 'cancel') console.error('退款失败:', err)
  }
}

// 状态文本
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '审核通过', 2: '已退款', 3: '驳回', 4: '用户已退货', 5: '商家收货完成' }
  return map[status] || '未知状态'
}

// 状态标签颜色
const getStatusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'success', 3: 'danger', 4: 'primary', 5: 'info' }
  return map[status] || 'info'
}

// 售后类型文本
const getTypeText = (type) => {
  const map = { 1: '仅退款', 2: '退货退款' }
  return map[type] || '其他'
}

// 格式化时间
const formatTime = (time) => time ? String(time).replace('T', ' ') : '--'
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
.info-item {
  line-height: 1.8;
}
.sn { font-weight: bold; }
.order-id, .time { font-size: 13px; color: #666; }

.info-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}
.card-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}
.info-row {
  margin-bottom: 8px;
  color: #666;
}
.info-row span:first-child {
  display: inline-block;
  width: 100px;
  color: #333;
}
</style>