<template>
  <div class="success-page">
    <div class="success-card">
      <!-- 成功图标 -->
      <div class="success-icon">
        <svg viewBox="0 0 1024 1024" width="80" height="80">
          <path fill="#52c41a" d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m210.3 314.7l-266 324.5c-12.7 15.5-37.7 15.5-50.4 0l-134-163c-9.1-11.1-9.1-27.4 0-38.5 11.1-13.1 31.2-13.1 42.3 0l108.8 131.9 241.9-295.5c11.1-13.1 31.2-13.1 42.3 0 12.7 15.5 9.1 38.5-9.1 50.6z"></path>
        </svg>
      </div>

      <h2 class="title">售后申请提交成功</h2>
      <p class="desc">我们已收到你的售后申请，商家会尽快处理</p>
      <p class="tip">请耐心等待审核，审核结果可在「我的售后」中查看</p>

      <!-- 按钮 -->
      <div class="btn-group">
        <el-button @click="toHome">返回首页</el-button>
        <el-button type="primary" @click="toAfterSaleList">查看我的售后</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

// 返回首页
const toHome = () => {
  router.push('/')
}

// 跳转到我的售后列表
const toAfterSaleList = () => {
  router.push('/userinfo/afterSale')
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  background: #f6f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.success-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 16px;
  padding: 50px 30px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.success-icon {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 6px;
}

.tip {
  font-size: 13px;
  color: #999;
  margin: 0 0 32px;
}

.btn-group {
  display: flex;
  gap: 14px;
  justify-content: center;
}
</style>