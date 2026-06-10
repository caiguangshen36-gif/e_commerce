<template>
  <div class="after-sale-apply-page">
    <div class="container">
      <h2 class="title">申请售后</h2>

      <div class="card" v-if="orderItem">
        <div class="goods-info">
          <el-image :src="orderItem.pic" class="goods-img" />
          <div class="text">
            <div class="name">{{ orderItem.productName }}</div>
            <div class="specs">{{ orderItem.skuSpecs || '默认规格' }}</div>
            <div class="price">¥{{ formatPrice(orderItem.price) }}</div>
          </div>
        </div>

        <el-form label-width="100px" :model="form" ref="formRef" class="mt20">
          <el-form-item label="售后类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择售后类型">
              <el-option label="仅退款" :value="1" />
              <el-option label="退货退款" :value="2" />
            </el-select>
          </el-form-item>

          <el-form-item label="退款金额" prop="refundAmount">
            <el-input
              v-model.number="form.refundAmount"
              type="number"
              :max="orderItem.totalPrice"
              suffix="元"
            />
          </el-form-item>

          <el-form-item label="售后原因" prop="reason">
            <el-select v-model="form.reason" placeholder="请选择售后原因" @change="onReasonChange">
              <el-option label="商品质量问题" value="商品质量问题" />
              <el-option label="发错货/漏发" value="发错货/漏发" />
              <el-option label="收到商品与描述不符" value="收到商品与描述不符" />
              <el-option label="商品存在瑕疵/破损" value="商品存在瑕疵/破损" />
              <el-option label="尺码/规格不合适" value="尺码/规格不合适" />
              <el-option label="颜色/款式不符" value="颜色/款式不符" />
              <el-option label="不想买了/不想要了" value="不想买了/不想要了" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <!-- 选「其他」时显示额外输入框 -->
          <el-form-item v-if="form.reason === '其他'" label="其他原因" prop="otherReason">
            <el-input v-model="form.otherReason" placeholder="请输入其他售后原因" />
          </el-form-item>

          <el-form-item label="详细说明" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请详细描述问题，方便我们快速处理"
            />
          </el-form-item>
        </el-form>

        <div class="t-right mt20">
          <el-button type="primary" @click="submitApply" :loading="loading">
            提交售后申请
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAfterSaleService } from '@/api/user/afterSale.js'
import { sendAdminNoticeService } from '@/api/user/notice.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const orderId = route.query.orderId
const orderItem = ref(route.query.item ? JSON.parse(route.query.item) : null)

const form = ref({
  orderId: orderId,
  orderItemId: orderItem.value?.id,
  type: 1, // 默认：仅退款
  reason: '',
  otherReason: '',
  description: '',
  refundAmount: orderItem.value?.totalPrice || 0
})

const formatPrice = (val) => Number(val || 0).toFixed(2)

const onReasonChange = (val) => {
  if (val !== '其他') {
    form.value.otherReason = ''
  }
}

const submitApply = async () => {
  if (!form.value.reason) return ElMessage.warning('请选择售后原因')
  if (form.value.reason === '其他' && !form.value.otherReason) {
    return ElMessage.warning('请填写其他售后原因')
  }
  if (!form.value.description) return ElMessage.warning('请填写详细说明')

  loading.value = true
  try {
    const res = await createAfterSaleService(form.value)
    console.log('售后申请结果:', res)
    // 给管理员发送通知
    try {
      await sendAdminNoticeService({
        noticeType: 3, 
        title: '新售后申请待处理',
        content: `用户提交了新的售后申请，单号：${res.data?.afterSaleSn || '未知'}，类型：${form.value.type === 1 ? '仅退款' : '退货退款'}，金额：¥${form.value.refundAmount}。`,
        bizId: res.data?.id?.toString() || '', 
        roleIds: [1,8] //这只是我个人数据库的角色ID，实际使用时请替换为你系统中管理员角色的ID列表
      })
    } catch (msgErr) {
      console.error('发送管理员通知失败:', msgErr)
    }

    try {
      await sendAdminNoticeService({
        noticeType: 2, 
        title: '退款申请通知',
        content: `用户提交了退款申请通知，单号：${res.data?.afterSaleSn || '未知'}，类型：${form.value.type === 1 ? '仅退款' : '退货退款'}，金额：¥${form.value.refundAmount}。`,
        bizId: res.data?.id?.toString() || '', 
        roleIds: [1,8] //这只是我个人数据库的角色ID，实际使用时请替换为你系统中管理员角色的ID列表
      })
    } catch (msgErr) {
      console.error('发送管理员通知失败:', msgErr)
    }

    ElMessage.success('申请成功')
    router.push('/afterSaleResult')
  } catch (e) {
    console.error('售后申请失败:', e)
    ElMessage.error('申请失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.after-sale-apply-page {
  min-height: 100vh;
  background: #f6f8fa;
  padding: 20px 0;
}
.container {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 16px;
}
.title {
  font-size: 18px;
  margin-bottom: 16px;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}
.goods-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.goods-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
}
.name {
  font-weight: 500;
}
.specs {
  font-size: 12px;
  color: #999;
  margin: 4px 0;
}
.price {
  color: #e65c5c;
  font-weight: 500;
}
.mt20 {
  margin-top: 20px;
}
.t-right {
  text-align: right;
}
</style>