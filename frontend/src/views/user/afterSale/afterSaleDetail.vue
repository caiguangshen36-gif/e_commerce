<template>
  <div class="after-detail-page" v-loading="loading">
    <div class="container" v-if="info.id">
      <div class="detail-card">
        <!-- 头部状态 -->
        <div class="detail-header">
          <el-tag :type="getStatusTagType(info.status)" size="small" round>
            {{ getStatusText(info.status) }}
          </el-tag>
          <span class="sn">售后单号：{{ info.afterSaleSn }}</span>
        </div>

        <!-- 进度条 -->
        <div class="timeline">
          <el-steps
            :active="info.type === 1 ? getActiveStepForOnlyRefund : getActiveStepForReturnRefund"
            finish-status="success"
            align-center
          >
            <el-step title="提交申请" />
            <el-step title="商家审核" />
            <el-step v-if="info.type === 2" title="用户退货" />
            <el-step v-if="info.type === 2" title="商家收货" />
            <el-step title="退款完成" />
          </el-steps>
        </div>

        <!-- 商品信息 -->
        <div class="section">
          <div class="section-title">商品信息</div>
          <div class="goods-row">
            <el-image :src="skuInfo.pic || '/images/default.png'" class="goods-img" fit="cover" lazy error="/images/default.png" />
            <div class="goods-info">
              <div class="name">{{ skuInfo.productName || '商品名称' }}</div>
              <div class="spec">{{ skuInfo.skuSpecs || '默认规格' }}</div>
              <div class="meta">单价：¥{{ formatPrice(skuInfo.price) }} × {{ skuInfo.quantity || 1 }}</div>
            </div>
            <div class="total-price">实付：¥{{ formatPrice(info.refundAmount) }}</div>
          </div>
        </div>

        <!-- 售后申请信息 -->
        <div class="section">
          <div class="section-title">售后申请信息</div>
          <div class="info-row">
            <label>售后类型</label>
            <span>{{ getTypeText(info.type) }}</span>
          </div>
          <div class="info-row">
            <label>申请原因</label>
            <span>{{ info.reason }}</span>
          </div>
          <div class="info-row">
            <label>问题描述</label>
            <span>{{ info.description || '无' }}</span>
          </div>
          <div class="info-row">
            <label>申请退款金额</label>
            <span class="price">¥{{ formatPrice(info.refundAmount) }}</span>
          </div>
          <div class="info-row">
            <label>申请时间</label>
            <span>{{ formatTime(info.createTime) }}</span>
          </div>
          <div class="info-row" v-if="info.auditTime">
            <label>审核时间</label>
            <span>{{ formatTime(info.auditTime) }}</span>
          </div>
          <div class="info-row" v-if="info.refundTime">
            <label>退款时间</label>
            <span>{{ formatTime(info.refundTime) }}</span>
          </div>
        </div>

        <!-- 商家备注 -->
        <div class="section" v-if="info.rejectReason">
          <div class="section-title">商家审核备注</div>
          <div class="remark-text">{{ info.rejectReason }}</div>
        </div>

        <!-- 物流信息 -->
        <div class="section" v-if="info.deliveryCompany">
          <div class="section-title">退货物流</div>
          <div class="info-row">
            <label>物流公司</label>
            <span>{{ info.deliveryCompany }}</span>
          </div>
          <div class="info-row">
            <label>物流单号</label>
            <span>{{ info.deliveryNo }}</span>
          </div>
        </div>

        <!-- 关联订单 -->
        <div class="section">
          <div class="section-title">关联订单</div>
          <div class="info-row">
            <label>订单编号</label>
            <span>{{ info.orderSn || info.orderId }}</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-box">
        <el-button @click="router.back()">返回列表</el-button>
        <el-button
          v-if="info.status === 1 && info.type === 2"
          type="primary"
          @click="toFillDelivery"
        >
          填写退货物流
        </el-button>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无售后详情" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAfterSaleDetailService } from '@/api/user/afterSale.js'
import { getSkuListByProductIdService } from '@/api/user/product.js' 
import {getOrderDetailService} from '@/api/user/order.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const info = ref({})
const skuInfo = ref({}) 

// 状态文本
const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '审核通过',
    2: '已退款',
    3: '驳回',
    4: '用户已退货',
    5: '商家收货完成'
  }
  return map[status] || '未知状态'
}

// 状态标签
const getStatusTagType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'success',
    3: 'danger',
    4: 'primary',
    5: 'info'
  }
  return map[status] || 'info'
}

// 售后类型
const getTypeText = (type) => {
  const map = {
    1: '仅退款',
    2: '退货退款'
  }
  return map[type] || '其他'
}

// 仅退款 进度条
const getActiveStepForOnlyRefund = computed(() => {
  const s = info.value.status
  if (s === 0) return 0
  if (s === 1) return 2 // 审核通过
  if (s === 2) return 5 // 退款完成
  return 0
})

// 退货退款 进度条
const getActiveStepForReturnRefund = computed(() => {
  const s = info.value.status
  if (s === 0) return 0
  if (s === 1) return 2 // 审核通过
  if (s === 4) return 3 // 用户已退货
  if (s === 5) return 4 // 商家收货完成
  if (s === 2) return 5 // 退款完成
  return 0
})

// 格式化
const formatPrice = (val) => Number(val || 0).toFixed(2)
const formatTime = (t) => t ? String(t).replace('T', ' ') : '--'

// 获取SKU信息
const fetchSkuInfo = async (productId) => {
  if (!productId) return
  try {
    const res = await getSkuListByProductIdService(productId)
    console.log('商品SKU列表', res)
    if (res.data && res.data.length > 0) {
      skuInfo.value = res.data[0]
    }
  } catch (e) {
    console.error('获取商品信息失败', e)
  }
}

//获取订单编号
const getOrderSn = async (orderId) => {
  try {
      const res = await getOrderDetailService(orderId)
      return res.data ? res.data.orderSn : ''
    } catch (e) {
      console.error('获取订单详情失败', e)
      return ''
    }
} 

// 获取详情
const fetchDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    router.back()
    return
  }
  loading.value = true
  try {
    const res = await getAfterSaleDetailService(id)
    console.log('售后详情', res)
    info.value = res.data || {}
    // 加载商品SKU信息
    if (info.value.productId) {
      await fetchSkuInfo(info.value.productId)
    }
    if (info.value.orderId) {
      info.value.orderSn = await getOrderSn(info.value.orderId)
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

// 去填写物流
const toFillDelivery = () => {
  router.push({
    path: '/afterSaleDelivery',
    query: {
      afterSaleId: info.value.id,
      afterSaleSn: info.value.afterSaleSn
    }
  })
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.after-detail-page {
  min-height: 100vh;
}
.container {
  width: 100%;
}
.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #eee;
}
.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.sn {
  font-size: 13px;
  color: #666;
}
.timeline {
  padding: 10px 0 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}
.section {
  margin-bottom: 20px;
}
.section:last-child {
  margin-bottom: 0;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.goods-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.goods-img {
  width: 70px;
  height: 70px;
  border-radius: 4px;
  object-fit: cover;
}
.goods-info {
  flex: 1;
}
.name {
  font-weight: 500;
  margin-bottom: 4px;
}
.spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.meta {
  font-size: 12px;
  color: #666;
}
.total-price {
  color: #e65c5c;
  font-weight: 500;
}
.info-row {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
}
.info-row label {
  width: 110px;
  color: #999;
  flex-shrink: 0;
}
.price {
  color: #e65c5c;
  font-weight: 500;
}
.remark-text {
  color: #f56c6c;
  padding: 10px;
  background: #fef5f5;
  border-radius: 4px;
}
.action-box {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}
</style>