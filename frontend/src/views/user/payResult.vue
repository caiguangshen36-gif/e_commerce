<template>
  <div class="result-page">
    <div class="container">
      <div class="result-card" :class="statusClass">
        <!-- 状态图标 -->
        <div class="icon-wrapper">
          <el-icon v-if="isSuccess" class="status-icon success">
            <CircleCheckFilled />
          </el-icon>
          <el-icon v-else class="status-icon fail">
            <CircleCloseFilled />
          </el-icon>
        </div>

        <!-- 标题与提示 -->
        <h2 class="title">{{ isSuccess ? '支付成功' : '支付失败' }}</h2>
        <p class="description">{{ displayMsg }}</p>

        <!-- 订单信息（仅在有数据时显示） -->
        <div class="order-info" v-if="orderNo || amount">
          <div class="info-row" v-if="orderNo">
            <span class="info-label">订单编号</span>
            <span class="info-value copyable" @click="copyText(orderNo)">
              {{ orderNo }} <el-icon><CopyDocument /></el-icon>
            </span>
          </div>
          <div class="info-row" v-if="amount">
            <span class="info-label">支付金额</span>
            <span class="info-value price">¥{{ formatPrice(amount) }}</span>
          </div>
          <div class="info-row" v-if="payTime">
            <span class="info-label">支付时间</span>
            <span class="info-value">{{ payTime }}</span>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="action-group">
          <template v-if="isSuccess">
            <el-button @click="goToOrderList">查看订单</el-button>
            <el-button type="primary" @click="goHome">返回首页</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="retryPay">重新支付</el-button>
            <el-button @click="goToOrderList">返回订单列表</el-button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  CircleCheckFilled, 
  CircleCloseFilled, 
  CopyDocument 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 路由参数解析（兼容后端重定向或前端跳转传参）
const status = ref(route.query.status || route.query.code || 'success')
const orderNo = ref(route.query.orderNo || route.query.settleId || '')
const amount = ref(route.query.amount || 0)
const msg = ref(route.query.msg || '')
const payTime = ref(route.query.payTime || '')

// 核心计算属性
const isSuccess = computed(() => status.value === 'success')
const statusClass = computed(() => isSuccess.value ? 'is-success' : 'is-fail')

const displayMsg = computed(() => {
  if (msg.value) return msg.value
  return isSuccess.value 
    ? '订单已生成，我们将尽快为您发货。您可前往订单列表查看物流进度。'
    : '支付未成功，请检查账户余额或网络状态后重试。'
})

const formatPrice = (val) => Number(val || 0).toFixed(2)

// 复制订单号
const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('订单号已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

// 路由跳转
const goToOrderList = () => router.replace('/userinfo/order')
const goHome = () => router.replace('/')
const retryPay = () => {
  if (route.query.settleId) {
    router.replace({ path: '/pay', query: { settleId: route.query.settleId } })
  } else {
    router.replace('/userinfo/order')
  }
}

// 参数校验与兜底
onMounted(() => {
  if (!route.query.status && !route.query.code) {
    // 无状态参数时，默认按成功处理，但可记录日志或跳回
    console.warn('支付结果页未接收到 status 参数，默认视为成功')
  }
})
</script>

<style scoped lang="scss">
:root {
  --result-bg: #f6f8fa;
  --card-bg: #ffffff;
  --text-main: #1d2129;
  --text-sub: #86909c;
  --border-color: #e5e6eb;
  --success-color: #52c41a;
  --fail-color: #e65c5c;
  --price-color: #e65c5c;
}

.result-page {
  min-height: calc(100vh - 70px);
  background: var(--result-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.container {
  width: 100%;
  max-width: 560px;
}

.result-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 40px 32px;
  text-align: center;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
  
  // 顶部状态色条
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: var(--fail-color);
  }
  &.is-success::before { background: var(--success-color); }
}

.icon-wrapper {
  margin-bottom: 20px;
  .status-icon {
    font-size: 64px;
    animation: scaleIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  }
  .success { color: var(--success-color); }
  .fail { color: var(--fail-color); }
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-main);
  margin: 0 0 10px;
}

.description {
  font-size: 14px;
  color: var(--text-sub);
  line-height: 1.6;
  margin: 0 0 28px;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
}

.order-info {
  background: #fafbfc;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 28px;
  text-align: left;
  
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px dashed #e8e8e8;
    &:last-child { border-bottom: none; }
  }
  
  .info-label {
    font-size: 13px;
    color: var(--text-sub);
  }
  
  .info-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-main);
    &.price { color: var(--price-color); font-weight: 600; }
    &.copyable {
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      transition: color 0.2s;
      &:hover { color: var(--primary, #409eff); }
    }
  }
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  .el-button { min-width: 110px; }
}

@keyframes scaleIn {
  0% { transform: scale(0.6); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

/* 响应式适配 */
@media (max-width: 480px) {
  .result-card { padding: 32px 20px; }
  .icon-wrapper .status-icon { font-size: 52px; }
  .title { font-size: 20px; }
  .action-group { flex-direction: column; .el-button { width: 100%; } }
}
</style>
