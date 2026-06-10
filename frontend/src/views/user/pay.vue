<template>
  <div class="pay-page">
    <div class="container" v-loading="loading">
      <template v-if="orderDetail">
        <!-- 头部信息 -->
        <div class="pay-header">
          <h2>订单支付</h2>
          <div class="pay-meta">
            <span class="order-no">订单号：{{ orderDetail.orderSn }}</span>
            <span class="divider">|</span>
            <span class="countdown" :class="{ expired: countDown <= 0 }">
              请在 {{ countDownText }} 内完成支付
            </span>
          </div>
        </div>

        <!-- 金额与余额 -->
        <div class="finance-box">
          <div class="finance-item amount">
            <div class="label">应付金额</div>
            <div class="value price">¥{{ formatPrice(orderDetail.totalAmount) }}</div>
          </div>
          <div class="finance-item balance">
            <div class="label">账户余额</div>
            <div class="value" :class="{ insufficient: userInfo.balance < orderDetail.totalAmount }">
              ¥{{ formatPrice(userInfo.balance) }}
            </div>
            <el-button 
              v-if="userInfo.balance < orderDetail.totalAmount" 
              type="warning" 
              link 
              size="small" 
              @click="goRecharge"
            >
              余额不足，去充值
            </el-button>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="pay-method">
          <div class="label">支付方式</div>
          <div class="method-item active">
            <el-icon><Wallet /></el-icon>
            <span>余额支付</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="pay-actions">
          <el-button @click="handleCancel">取消订单</el-button>
          <el-button
            type="primary"
            size="large"
            @click="handlePay"
            :loading="paying"
            :disabled="countDown <= 0 || userInfo.balance < orderDetail.totalAmount"
          >
            {{ countDown <= 0 ? '支付已超时' : (paying ? '支付处理中...' : '立即支付') }}
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="未找到支付信息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet } from '@element-plus/icons-vue'
import { getOrderDetailService, cancelOrderService } from '@/api/user/order.js'
import { getUserInfoService, updateUserBalanceService } from '@/api/user/user.js'
import { updateOrderStatusService } from '@/api/user/order.js'
import { createLogisticsService, createLogisticsTraceService } from '@/api/user/logistics.js'
import { createUserPaymentListService } from '@/api/user/payment.js'
import { sendUserNoticeService, sendAdminNoticeService } from '@/api/user/notice.js'
import { reduceStockService, rollbackStockService ,getSkuDetailService} from '@/api/user/product.js'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paying = ref(false)
const orderDetail = ref(null)
const userInfo = ref({ balance: 0 })

// 倒计时 10 分钟
const countDown = ref(10 * 60)
const countDownText = ref('')
let timer = null

const formatPrice = (val) => Number(val || 0).toFixed(2)

// 格式化倒计时
const formatCountDown = () => {
  const m = Math.floor(countDown.value / 60)
  const s = countDown.value % 60
  countDownText.value = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 启动倒计时
const startCountDown = () => {
  formatCountDown()
  timer = setInterval(() => {
    if (countDown.value <= 0) {
      clearInterval(timer)
      timer = null
      //  超时自动取消订单
      autoCancelOrder()
      return
    }
    countDown.value--
    formatCountDown()
  }, 1000)
}

// 超时自动取消订单
const autoCancelOrder = async () => {
  try {
    ElMessage.warning('支付超时，系统自动取消订单')
    const orderId = route.query.orderId

    if (!orderId) return
    const cancelParams = {
      orderId: orderId,
      cancelReason: "支付超时自动取消",
      cancelDescription: "订单未在10分钟内支付，系统自动取消"
    }
    await cancelOrderService(cancelParams)
  } catch (err) {
    console.error('自动取消失败', err)
  } finally {
    router.replace('/userinfo/order')
  }
}

// 加载数据
const loadData = async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error('缺少订单号')
    router.replace('/cart')
    return
  }

  loading.value = true
  try {
    const [orderRes, userRes] = await Promise.all([
      getOrderDetailService(orderId),
      getUserInfoService()
    ])
    
    console.log('原始订单响应:', orderRes.data)
    
    orderDetail.value = orderRes.data
    userInfo.value = userRes.data || { balance: 0 }

    // 检查订单项是否存在
    console.log('订单项是否存在:', orderDetail.value.orderItems)
    console.log('订单项长度:', orderDetail.value.orderItems?.length)
    
    if (orderDetail.value.status !== 0) {
      ElMessage.warning(`订单状态异常（已支付/已取消）`)
      router.replace('/userinfo/order')
      return
    }

    startCountDown()
  } catch (err) {
    console.error('加载支付信息失败:', err)
    ElMessage.error('加载支付信息失败: ' + (err.message || '未知错误'))
    router.replace('/cart')
  } finally {
    loading.value = false
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后需重新下单', '提示', { type: 'warning' })
    // 取消时清除定时器
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    await cancelOrderService(route.query.orderId)
    ElMessage.success('订单已取消')
    router.replace('/userinfo/order')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

// 准备库存数据（用于回滚操作）
const prepareStockData = () => {
  const stockData = []
  
  if (!orderDetail.value) {
    console.error('订单详情为空')
    return stockData
  }
  
  console.log('检查订单项字段:', {
    hasOrderItems: !!orderDetail.value.orderItems,
    itemsLength: orderDetail.value.orderItems?.length || 0,
    itemsContent: orderDetail.value.orderItems
  })
  
  if (orderDetail.value.orderItems && Array.isArray(orderDetail.value.orderItems)) {
    orderDetail.value.orderItems.forEach((item, index) => {
      console.log(`订单项 ${index}:`, item)
      
      // 检查必要的字段是否存在
      if (item.skuId && item.quantity) {
        stockData.push({
          id: item.skuId,
          stock: item.quantity
        })
      } else {
        console.warn(`订单项缺少必要字段:`, item)
      }
    })
  } else {
    console.warn('订单项不存在或不是数组格式')
  }
  
  console.log('准备的库存数据:', stockData)
  return stockData
}

// 支付
const handlePay = async () => {
  if (paying.value || !orderDetail.value) {
    console.log('支付条件不满足:', { paying: paying.value, hasOrderDetail: !!orderDetail.value })
    return
  }

  const balance = Number(userInfo.value.balance)
  const amount = Number(orderDetail.value.totalAmount)

  if (balance < amount) {
    ElMessage.error('账户余额不足，请充值后重试')
    return
  }

  // 检查订单是否有商品
  const stockData = prepareStockData()
  if (stockData.length === 0) {
    ElMessage.error('订单商品信息缺失，请刷新页面重试')
    console.error('库存数据为空，无法进行支付')
    return
  }

  paying.value = true
  try {
    if (timer) {
      clearInterval(timer)
      timer = null
    }

    // 执行业务逻辑
    await updateUserBalanceService({ balance: balance - amount })
    const orderId = orderDetail.value.id
    const orderSn = orderDetail.value.orderSn
    await updateOrderStatusService(orderId, 1)

    // 通过订单ID扣减库存（新的方式）
    console.log('开始扣除库存，订单ID:', orderId)
    const stockResult = await reduceStockService(orderId)
    console.log('库存扣除结果:', stockResult)
    
    if (stockResult.code !== 200) {
      throw new Error(stockResult.message || '库存扣减失败')
    }

    await createLogisticsService({ orderId, deliveryCompany: "自动创建" })
    await createLogisticsTraceService(orderId)
    await createUserPaymentListService({ orderId, paymentMethod: 1, amount })

    // 支付成功后，给用户发送站内信通知
    try {
      await sendUserNoticeService({
        noticeType: 7,
        title: '支付成功通知',
        content: `您的订单 ${orderSn} 已支付成功，支付金额：${amount.toFixed(2)}元，我们将尽快为您发货。`,
        bizId: orderSn
      })
    } catch (msgErr) {
      console.error('发送支付成功通知失败:', msgErr)
    }

    try {
      await sendAdminNoticeService({
        noticeType: 1,
        title: '新订单待发货提醒',
        content: `用户已支付订单 ${orderSn}，请尽快安排发货！`,
        bizId: orderSn,
        roleIds: [1, 5]
      })
    } catch (adminMsgErr) {
      console.error('发送管理员发货通知失败:', adminMsgErr)
    }

    // 3. 提示并跳转
    ElMessage.success('支付成功')
    setTimeout(() => {
      router.replace({
        path: '/pay/result',
        query: { status: 'success', orderNo: orderSn, amount: amount.toFixed(2), payTime: new Date().toLocaleString() }
      })
    }, 1200)

  } catch (err) {
    console.error('支付过程出错:', err)
    
    // 如果支付过程中出现错误，回滚库存
    try {
      const orderId = orderDetail.value.id
      console.log('开始回滚库存，订单ID:', orderId)
      const rollbackResult = await rollbackStockService(orderId)
      console.log('库存回滚结果:', rollbackResult)
    } catch (rollbackErr) {
      console.error('库存回滚失败:', rollbackErr)
      ElMessage.warning('库存回滚失败，请联系客服处理')
    }

    ElMessage.error(err.msg || err.message || '支付异常')
  } finally {
    paying.value = false
  }
}

const goRecharge = () => {
  router.push('/userinfo/info')
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  // 页面离开也清除定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped lang="scss">
:root {
  --pay-bg: #f6f8fa;
  --card-bg: #ffffff;
  --text-main: #1d2129;
  --text-sub: #86909c;
  --border-color: #e5e6eb;
  --primary: #409eff;
  --price-color: #e65c5c;
  --insufficient: #e6a23c;
}

.pay-page {
  min-height: calc(100vh - 70px);
  background: var(--pay-bg);
  padding: 24px 0;
}

.container {
  max-width: 680px;
  margin: 0 auto;
  padding: 0 16px;
}

.pay-header {
  margin-bottom: 24px;
  h2 {
    margin: 0 0 12px;
    font-size: 20px;
    font-weight: 600;
    color: var(--text-main);
  }
  .pay-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-sub);
  }
  .divider { color: #d0d3d9; }
  .countdown { font-weight: 500; }
  .countdown.expired { color: #909399; text-decoration: line-through; }
}

.finance-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.finance-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 80px;
  .label { font-size: 13px; color: var(--text-sub); margin-bottom: 8px; }
  .value { font-size: 22px; font-weight: 700; }
}

.amount .value { color: var(--price-color); }
.balance .value { color: var(--text-main); }
.balance .value.insufficient { color: var(--insufficient); }

.pay-method {
  margin-bottom: 24px;
  .label { font-size: 14px; color: var(--text-sub); margin-bottom: 12px; }
  .method-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 16px;
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: 8px;
    cursor: pointer;
    width: fit-content;
    &.active {
      border-color: var(--primary);
      background: #ecf5ff;
      color: var(--primary);
    }
  }
}

.pay-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  .el-button { min-width: 100px; }
}

@media (max-width: 600px) {
  .finance-box { grid-template-columns: 1fr; }
  .pay-actions { flex-direction: column-reverse; .el-button { width: 100%; } }
}
</style>