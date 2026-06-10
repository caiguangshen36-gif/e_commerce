<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单列表</h2>
    </div>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" inline style="margin-bottom: 20px">
      <el-form-item label="订单编号">
        <el-input v-model="queryParams.orderSn" placeholder="请输入订单编号" clearable style="width: 220px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已发货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker
          v-model="queryParams.createTimeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="[new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)]"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    
    <el-table :data="orderList" border stripe v-loading="loading">
      <el-table-column label="订单信息" width="350">
        <template #default="{ row }">
          <div class="order-info">
            <div class="order-sn">订单号：{{ row.orderSn }}</div>
            <div class="order-user">用户ID：{{ row.userId }}</div>
            <div class="order-time">下单时间：{{ row.createTime }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="收货人" align="center">
        <template #default="{ row }">
          <div>{{ row.receiver }}</div>
          <div style="color: #999; font-size: 12px">{{ row.phone }}</div>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" align="center">
        <template #default="{ row }">
          <div style="color: #f56c6c; font-weight:bold">¥{{ Number(row.payAmount || 0).toFixed(2) }}</div>
          <div style="color: #999; font-size: 12px">总价：¥{{ Number(row.totalAmount || 0).toFixed(2) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" prop="payTime" align="center" width="180" />
      <el-table-column label="操作" align="center" width="220">
        <template #default="{ row }">
          <el-button type="primary" link @click="toDetail(row.id)">查看详情</el-button>
          <el-button
            v-if="row.status === 1"
            type="success"
            link
            @click="openDeliverDialog(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      style="margin-top:20px; text-align:right"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="handleSizeChange"
      @current-change="getOrderList"
    />

    <!-- 发货弹窗 -->
    <el-dialog v-model="deliverDialogVisible" title="订单发货" width="500px">
      <el-form :model="deliverForm" label-width="100px">
        <el-form-item label="快递公司">
          <el-select v-model="deliverForm.deliveryCompany" placeholder="请选择快递公司" style="width: 100%">
            <el-option label="顺丰" value="SF" />
            <el-option label="圆通" value="YTO" />
            <el-option label="中通" value="ZTO" />
            <el-option label="申通" value="STO" />
            <el-option label="韵达" value="YD" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="deliverLoading" @click="submitDeliver">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { getAdminOrderListService, deliverOrderService } from '@/api/admin/order.js'
import { updateDeliveryService } from '@/api/admin/logistics.js'
import { sendNoticeToUserService } from '@/api/admin/notice.js'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const deliverLoading = ref(false)
const orderList = ref([])
const total = ref(0)
const deliverDialogVisible = ref(false)
const currentOrderId = ref(null)
const currentOrderUserId = ref(null)
const currentOrderSn = ref('')

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  orderSn: '',
  status: undefined,
  createTimeRange: []
})

const deliverForm = ref({
  deliveryCompany: ''
})

onMounted(() => {
  const { status, today } = route.query

  if (status !== undefined && status !== '') {
    queryParams.value.status = Number(status)
  }

  if (today === 'true') {
    const now = new Date()
    const y = now.getFullYear()
    const m = String(now.getMonth() + 1).padStart(2, '0')
    const d = String(now.getDate()).padStart(2, '0')
    queryParams.value.createTimeRange = [`${y}-${m}-${d} 00:00:00`, `${y}-${m}-${d} 23:59:59`]
  }

  getOrderList()
})

const handleSizeChange = () => {
  queryParams.value.pageNum = 1
  getOrderList()
}

const handleSearch = () => {
  queryParams.value.pageNum = 1
  getOrderList()
}

const getOrderList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      orderSn: queryParams.value.orderSn || null,
      status: queryParams.value.status ?? null
    }

    if (queryParams.value.createTimeRange?.length === 2) {
      params.startTime = queryParams.value.createTimeRange[0]
      params.endTime = queryParams.value.createTimeRange[1]
    }

    const res = await getAdminOrderListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      orderList.value = pageData.records || pageData.list || pageData.rows || []
      total.value = pageData.total || 0
    } else {
      orderList.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取订单列表失败', err)
    ElMessage.error('获取订单列表失败')
    orderList.value = []
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
    orderSn: '',
    status: undefined,
    createTimeRange: []
  }
  getOrderList()
}

// 跳转详情
const toDetail = (orderId) => {
  if (!orderId) return
  router.push({ path: '/admin/order/detail', query: { orderId } })
}

// 打开发货弹窗
const openDeliverDialog = (row) => {
  currentOrderId.value = row.id
  currentOrderUserId.value = row.userId
  currentOrderSn.value = row.orderSn
  deliverForm.value = { deliveryCompany: '' }
  deliverDialogVisible.value = true
}

// 增加 loading 防止重复提交 + 不再从列表中查找订单信息
const submitDeliver = async () => {
  if (!deliverForm.value.deliveryCompany) {
    ElMessage.warning('请选择快递公司')
    return
  }

  deliverLoading.value = true
  try {
    await deliverOrderService(currentOrderId.value)
    await updateDeliveryService({
      orderId: currentOrderId.value,
      deliveryCompany: deliverForm.value.deliveryCompany
    })

    // 发送站内信通知
    try {
      await sendNoticeToUserService({
        userId: currentOrderUserId.value,
        noticeType: 6,
        title: '订单已发货',
        content: `您的订单 ${currentOrderSn.value} 已由 ${deliverForm.value.deliveryCompany} 快递发出，请注意查收。`,
        bizId: String(currentOrderId.value)
      })
    } catch (msgErr) {
      console.error('发送发货通知失败:', msgErr)
    }

    ElMessage.success('发货成功')
    deliverDialogVisible.value = false
    getOrderList()
  } catch (err) {
    console.error('发货失败', err)
    ElMessage.error('发货失败')
  } finally {
    deliverLoading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
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
.order-info {
  line-height: 1.8;
}
.order-sn {
  font-weight: bold;
}
.order-user, .order-time {
  color: #666;
  font-size: 13px;
}
</style>