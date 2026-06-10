<template>
  <div class="delivery-page">
    <div class="container">
      <h2 class="title">填写退货物流</h2>

      <div class="card" v-if="afterSaleId">
        <el-form label-width="100px" :model="form" ref="formRef" class="form">
          <el-form-item label="物流公司" prop="deliveryCompany">
            <el-select v-model="form.deliveryCompany" placeholder="请选择物流公司">
              <el-option label="顺丰速运" value="顺丰速运" />
              <el-option label="中通快递" value="中通快递" />
              <el-option label="圆通快递" value="圆通快递" />
              <el-option label="申通快递" value="申通快递" />
              <el-option label="韵达快递" value="韵达快递" />
              <el-option label="京东快递" value="京东快递" />
              <el-option label="邮政快递" value="邮政快递" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="t-right mt20">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submit">
            提交物流信息
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
import { userReturnGoodsService } from '@/api/user/afterSale.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

// 读取路由参数
const afterSaleId = ref(route.query.afterSaleId)
const afterSaleSn = ref(route.query.afterSaleSn || '未知')

const form = ref({
  afterSaleId: afterSaleId.value,
  deliveryCompany: '',
  deliveryNo: ''
})


// 提交物流
const submit = async () => {
  if (!form.value.deliveryCompany) return ElMessage.warning('请选择物流公司')

  loading.value = true
  try {
    await userReturnGoodsService(form.value)
    ElMessage.success('提交成功')
    router.push('/afterSaleResult') 
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!afterSaleId.value) {
    console.error('缺少售后单ID')
    ElMessage.error('参数异常')
    router.back()
  }
})
</script>

<style scoped lang="scss">
.delivery-page {
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
  font-weight: 600;
  margin-bottom: 16px;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}
.form {
  margin-bottom: 20px;
}
.t-right {
  text-align: right;
}
.mt20 {
  margin-top: 20px;
}
.after-sale-sn {
  margin-bottom: 16px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}
</style>