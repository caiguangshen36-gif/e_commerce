<template>
  <div class="comment-section">
    <h3 class="section-title">商品评论</h3>

    <div v-if="loading" class="loading-tip">评论加载中...</div>

    <div v-else-if="commentList.length" class="comment-list">
      <div class="comment-item" v-for="comment in commentList" :key="comment.id">
        <!-- 头像 + 评论主体 -->
        <div class="comment-main">
          <div class="avatar-wrap">
            <img
              v-if="comment.avatar"
              :src="comment.avatar"
              alt="头像"
              class="avatar"
            />
            <div v-else class="avatar-placeholder">
              <el-icon :size="20"><User /></el-icon>
            </div>
          </div>

          <div class="comment-body">
            <div class="comment-header">
              <span class="username">{{ comment.username }}</span>
              <el-rate
                :model-value="comment.score"
                disabled
                size="14"
              />
            </div>
            <div class="comment-time">{{ comment.createTime }}</div>
            <div class="comment-content">{{ comment.content }}</div>

            <!-- 商家回复 展开/收起 -->
            <div
              v-if="comment.replies && comment.replies.length > 0"
              class="reply-toggle"
              @click="toggleReply(comment.id)"
            >
              {{ comment.showReply ? "收起回复" : "查看回复" }}
            </div>

            <!-- 回复列表 -->
            <div
              v-if="comment.showReply && comment.replies"
              class="reply-list"
            >
              <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
                <div class="reply-header">
                  <span>{{ reply.username }}（商家）</span>
                  <span>{{ reply.createTime }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-tip">暂无评论</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElRate, ElIcon } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { getCommentListByProductIdService } from '@/api/user/comment.js'

const route = useRoute()
const productId = route.query.id 

const commentList = ref([])
const loading = ref(false)

// 获取商品评论
const getCommentList = async () => {
  if (!productId) return
  loading.value = true
  try {
    const res = await getCommentListByProductIdService(productId)
    console.log("商品评论：", res)
    
    if (res.code === 200) {
      commentList.value = res.data || []
      // 默认给每条评论加展开状态
      commentList.value.forEach(c => {
        c.showReply = false
      })
    }
  } catch (err) {
    ElMessage.error("评论加载失败")
  } finally {
    loading.value = false
  }
}

// 展开/收起回复
const toggleReply = (commentId) => {
  const comment = commentList.value.find(item => item.id === commentId)
  if (comment) {
    comment.showReply = !comment.showReply
  }
}

onMounted(() => {
  getCommentList()
})
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
}
.section-title {
  font-size: 18px;
  margin: 0 0 20px;
}
.loading-tip, .empty-tip {
  color: #999;
  text-align: center;
  padding: 30px 0;
}
.comment-list {
  margin-bottom: 10px;
}
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f2f2f2;
}
.comment-main {
  display: flex;
  gap: 12px;
}
.avatar-wrap {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}
.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.comment-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}
.comment-content {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
}
.reply-toggle {
  margin-top: 8px;
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
}
.reply-list {
  margin-top: 10px;
  padding-left: 15px;
  border-left: 2px solid #eee;
}
.reply-item {
  padding: 8px 0;
  font-size: 14px;
}
.reply-header {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
}
.reply-content {
  margin-top: 4px;
  color: #555;
}
</style>