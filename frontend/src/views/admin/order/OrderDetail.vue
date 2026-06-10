<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="container" v-if="orderInfo.id">
      <!-- 顶部状态与单号 -->
      <div class="detail-header">
        <div class="header-left">
          <el-tag :type="statusMap[orderInfo.status]?.type || 'info'" effect="plain" round>
            {{ orderInfo.statusText || '未知状态' }}
          </el-tag>
          <span class="order-sn">订单号：{{ orderInfo.orderSn }}</span>
          <el-button type="primary" link size="small" @click="copyOrderSn">
            <el-icon><CopyDocument /></el-icon> 复制
          </el-button>
        </div>
        <div class="header-right">
          <span class="create-time">创建时间：{{ formatTime(orderInfo.createTime) }}</span>
        </div>
      </div>

      <!-- 订单进度轴 -->
      <div class="card timeline-card" v-if="orderInfo.status !== 4">
        <el-steps :active="activeStep" finish-status="success" align-center simple>
          <el-step title="提交订单" :description="formatTime(orderInfo.createTime)" />
          <el-step title="买家付款" :description="formatTime(orderInfo.payTime)" />
          <el-step title="卖家发货" :description="formatTime(orderInfo.deliveryTime)" />
          <el-step title="交易完成" :description="formatTime(orderInfo.confirmTime)" />
        </el-steps>
      </div>

      <!-- 收货信息 -->
      <div class="card address-card">
        <div class="card-title">收货信息</div>
        <div class="address-body">
          <div class="contact">
            <span class="name">{{ orderInfo.receiver }}</span>
            <span class="phone">{{ orderInfo.phone }}</span>
          </div>
          <div class="addr">{{ orderInfo.address }}</div>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="card items-card">
        <div class="card-title">商品信息</div>
        <div class="item-list" v-if="orderInfo.orderItems?.length">
          <div v-for="item in orderInfo.orderItems" :key="item.id" class="item-row">
            <el-image :src="item.pic" class="item-img" fit="cover" lazy />
            <div class="item-info">
              <div class="name">{{ item.productName }}</div>
              <div class="specs">{{ item.skuSpecs || '默认规格' }}</div>
            </div>
            <div class="item-meta">
              <span class="price">¥{{ formatPrice(item.price) }}</span>
              <span class="qty">×{{ item.quantity }}</span>
            </div>
            <div class="subtotal">¥{{ formatPrice(item.totalPrice) }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无商品明细" :image-size="80" />
      </div>

      <!-- 订单信息与金额 -->
      <div class="card info-card">
        <div class="card-title">订单信息</div>
        <div class="info-grid">
          <div class="info-row">
            <span class="label">订单编号</span>
            <span class="value">{{ orderInfo.orderSn }}</span>
          </div>
          <div class="info-row">
            <span class="label">用户ID</span>
            <span class="value">{{ orderInfo.userId }}</span>
          </div>
          <div class="info-row">
            <span class="label">下单时间</span>
            <span class="value">{{ formatTime(orderInfo.createTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付方式</span>
            <span class="value">余额支付</span>
          </div>
          <div class="info-row divider-row">
            <span class="label">商品总额</span>
            <span class="value">¥{{ formatPrice(orderInfo.totalAmount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">运费</span>
            <span class="value">¥0.00</span>
          </div>
          <div class="info-row">
            <span class="label">优惠减免</span>
            <span class="value discount">-¥0.00</span>
          </div>
          <div class="info-row total">
            <span class="label">实付金额</span>
            <span class="value price">¥{{ formatPrice(orderInfo.payAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 物流信息 -->
      <div class="card logistics-card" v-if="orderInfo.logistics">
        <div class="card-title">物流跟踪</div>
        <div class="logistics-body">
          <el-icon><Van /></el-icon>
          <span>{{ orderInfo.logistics.company || '暂无物流信息' }}</span>
          <span class="logistics-num">{{ orderInfo.logistics.trackingNo || '运单号：待更新' }}</span>
        </div>
      </div>

      <!-- 管理员底部操作：仅返回 -->
      <div class="footer-actions">
        <el-button @click="router.back()">返回列表</el-button>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="未找到订单信息" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CopyDocument, Van } from '@element-plus/icons-vue'
import { getAdminOrderDetailService } from '@/api/admin/order.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orderInfo = ref({})

const statusMap = {
  0: { text: '待付款', type: 'warning', step: 0 },
  1: { text: '待发货', type: 'primary', step: 1 },
  2: { text: '待收货', type: 'info', step: 2 },
  3: { text: '已完成', type: 'success', step: 3 },
  4: { text: '已取消', type: 'info', step: -1 }
}

const statusToStepMap = {
  0: 0,
  1: 2,
  2: 3,
  3: 3
}

const activeStep = computed(() => {
  const status = orderInfo.value.status ?? 0
  return statusToStepMap[status] ?? 0
})

// 格式化
const formatPrice = (val) => Number(val || 0).toFixed(2)
const formatTime = (time) => time ? String(time).replace('T', ' ') : '--'

// 复制订单号
const copyOrderSn = async () => {
  try {
    await navigator.clipboard.writeText(orderInfo.value.orderSn)
    ElMessage.success('订单号已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

// 获取订单详情
onMounted(() => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error('缺少订单ID')
    router.back()
    return
  }
  fetchOrderDetail(orderId)
})

const fetchOrderDetail = async (orderId) => {
  loading.value = true
  try {
    const res = await getAdminOrderDetailService(orderId)
    console.log('管理员订单详情：', res.data)
    orderInfo.value = res.data || {}
  } catch (err) {
    ElMessage.error('获取订单详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
:root {
  --detail-bg: #f6f8fa;
  --card-bg: #ffffff;
  --text-main: #1d2129;
  --text-sub: #86909c;
  --border-color: #e5e6eb;
  --price-color: #e65c5c;
  --primary: #409eff;
  --discount-color: #f56c6c;
}

.order-detail-page {
  min-height: calc(100vh - 70px);
  background: var(--detail-bg);
  padding: 20px 0 100px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
  .header-left { display: flex; align-items: center; gap: 10px; }
  .order-sn { font-size: 14px; color: var(--text-sub); }
  .header-right { font-size: 13px; color: var(--text-sub); }
}

.card {
  background: var(--card-bg);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.address-body {
  .contact { margin-bottom: 8px; }
  .name { font-weight: 600; margin-right: 12px; color: var(--text-main); }
  .phone { color: var(--text-sub); }
  .addr { color: var(--text-main); line-height: 1.6; }
}

.item-list {
  .item-row {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px dashed #e8e8e8;
    &:last-child { border-bottom: none; }
  }
  .item-img {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    border: 1px solid var(--border-color);
    flex-shrink: 0;
  }
  .item-info { flex: 1; margin-left: 12px; min-width: 0; }
  .name {
    font-size: 14px;
    color: var(--text-main);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .specs { font-size: 12px; color: var(--text-sub); margin-top: 4px; }
  .item-meta {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    margin: 0 16px;
    .price { font-size: 14px; color: var(--text-sub); }
    .qty { font-size: 13px; color: var(--text-sub); margin-top: 4px; }
  }
  .subtotal {
    font-size: 15px;
    font-weight: 600;
    color: var(--price-color);
    width: 80px;
    text-align: right;
  }
}

.info-grid {
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    font-size: 14px;
    .label { color: var(--text-sub); width: 80px; flex-shrink: 0; }
    .value { color: var(--text-main); font-weight: 500; }
    &.divider-row { border-top: 1px solid #f0f0f0; margin-top: 4px; padding-top: 12px; }
    &.total { border-top: 1px solid #f0f0f0; margin-top: 4px; padding-top: 12px; }
    &.total .value.price { font-size: 18px; font-weight: 700; }
    .discount { color: var(--discount-color); }
  }
}

.logistics-body {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
  color: var(--text-sub);
  font-size: 14px;
  .logistics-num { margin-left: auto; color: var(--primary); }
}

.footer-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 14px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid var(--border-color);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

@media (max-width: 600px) {
  .container { padding: 0 12px; }
  .item-meta { margin: 0 8px; }
  .subtotal { width: 60px; font-size: 14px; }
  .footer-actions { padding: 12px 16px; flex-wrap: wrap; .el-button { flex: 1; min-width: 45%; } }
}
</style>