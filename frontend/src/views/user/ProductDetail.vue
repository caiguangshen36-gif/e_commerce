<template>
  <div class="product-detail-page">
    <!-- 商品详情区域 -->
    <div class="detail-section">
      <Detail />
    </div>

    <!-- 评论与回复区域 -->
    <div class="comment-section">
      <Comment />
    </div>
  </div>
</template>

<script setup>
import Detail from './detail/detail.vue'
import Comment from './detail/comment.vue'
</script>

<style scoped>
.product-detail-page {
  background-color: #f5f7fa;
  min-height: calc(100vh - 70px);
}


.detail-section {
  background: #fff;
  margin: 0 auto 16px auto;
  max-width: 1200px;
  border-radius: 0 0 8px 8px;
}


.comment-section {
  background: #fff;
  border-radius: 8px;
  margin: 0 auto;
  max-width: 1200px;
  padding: 24px;
}
</style>