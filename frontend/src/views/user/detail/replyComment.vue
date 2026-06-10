<template>
  <div class="reply-item">
    <div class="reply-header">
      <span class="shop-tag">商家回复</span>
      <span class="reply-time">{{ reply.createTime }}</span>
    </div>
    <div class="reply-content">{{ reply.content }}</div>
  </div>
</template>

<script setup>
defineProps({
  reply: {
    type: Object,
    default: () => ({})
  }
})
</script>

<style scoped>
.reply-item {
  background: #f7f8fa;
  padding: 10px 12px;
  border-radius: 6px;
  margin-top: 8px;
  font-size: 14px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.shop-tag {
  color: #1677ff;
  font-weight: 500;
}

.reply-time {
  color: #999;
  font-size: 12px;
}

.reply-content {
  color: #333;
  line-height: 1.5;
}
</style>