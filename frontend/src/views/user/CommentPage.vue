<template>
  <div class="comment-page">
    <div class="container">
      <h2 class="title">商品评价</h2>

      <!-- 发布评论（仅已购买 & 已登录显示） -->
      <div class="publish-box" v-if="isLogin && hasBought">
        <el-rate v-model="score" show-score text-color="#ff9900" />
        <el-input
          v-model="content"
          type="textarea"
          :rows="3"
          placeholder="请输入评价内容"
          class="mt10"
        />
        <div class="t-right mt10">
          <el-button type="primary" @click="publishComment">提交评价</el-button>
        </div>
      </div>
      <div v-else-if="isLogin && !hasBought" class="tip-box">
        购买后才能评价此商品
      </div>

      <!-- 评论列表 -->
      <div class="comment-list" v-loading="loading">
        <div class="comment-item" v-for="item in list" :key="item.id">
          <div class="user-info">
            <el-avatar :src="item.avatar || ''">{{ item.username }}</el-avatar>
            <span class="name">{{ item.username }}</span>
            <span class="time">{{ item.createTime }}</span>
          </div>

          <el-rate :value="item.score" disabled class="star" />
          <div class="content">{{ item.content }}</div>

          <!-- 回复列表 -->
          <div class="reply-list" v-if="item.replyList && item.replyList.length">
            <div v-for="rep in item.replyList" :key="rep.id" class="reply-item">
              <span class="r-name">{{ rep.replyUsername }}：</span>
              <span class="r-content">{{ rep.replyContent }}</span>
            </div>
          </div>

          <div class="op-row">
            <div @click="showReplyBox(item.id)" class="op-btn">回复</div>
            <div
              v-if="item.userId === userId"
              @click="delComment(item.id)"
              class="op-btn del"
            >删除</div>
          </div>

          <!-- 回复框 -->
          <div
            class="reply-box"
            v-if="replyCommentId === item.id"
            v-loading="submitting"
          >
            <el-input v-model="replyContent" type="textarea" :rows="2" />
            <div class="t-right mt5">
              <el-button size="small" @click="replyComment(item.id)">
                回复
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="list.length === 0 && !loading" description="暂无评价" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
import {
  getCommentListByProductIdService,
  addCommentService,
  deleteCommentService,
  addCommentReplyService
} from '@/api/user/comment.js'

const route = useRoute()
const tokenStore = useTokenStore()

const productId = ref(route.query.productId)
const orderId = route.query.orderId

const userId = ref(null)
const isLogin = ref(!!tokenStore.token)
const hasBought = ref(true) 
const loading = ref(false)
const list = ref([])

// 发布评论
const score = ref(5)
const content = ref('')

// 回复
const replyCommentId = ref(null)
const replyContent = ref('')
const submitting = ref(false)

const getList = async () => {
  if (!productId.value) return
  loading.value = true
  try {
    const res = await getCommentListByProductIdService(productId.value)
    console.log('评论列表', res)
    list.value = res.data || []
  } catch (err) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 发布评论
const publishComment = async () => {
  if (!content.value) return ElMessage.warning('请输入评价')
  if (!productId.value) return ElMessage.warning('商品参数异常，无法评价')

  try {
    await addCommentService({
      productId: productId.value,
      content: content.value,
      score: score.value,
      orderItemId: 0 
    })
    ElMessage.success('发表成功')
    content.value = ''
    score.value = 5
    getList()
  } catch (err) {
    ElMessage.error('发表失败')
  }
}

// 删除评论
const delComment = async (id) => {
  try {
    await deleteCommentService(id)
    ElMessage.success('删除成功')
    getList()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

// 回复评论
const showReplyBox = (id) => {
  replyCommentId.value = id
  replyContent.value = ''
}
const replyComment = async (cid) => {
  if (!replyContent.value) return ElMessage.warning('请输入回复')
  try {
    await addCommentReplyService({
      commentId: cid,
      replyContent: replyContent.value,
      replyType: 1
    })
    ElMessage.success('回复成功')
    replyCommentId.value = null
    getList()
  } catch (err) {
    ElMessage.error('回复失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.comment-page {
  padding: 20px 0;
  background: #f6f8fa;
  min-height: 100vh;
}
.container {
  max-width: 1000px;
  margin: 0 auto;
  background: #fff;
  padding: 24px;
  border-radius: 12px;
}
.title {
  font-size: 18px;
  margin-bottom: 16px;
}
.publish-box {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 20px;
}
.tip-box {
  padding: 12px;
  color: #999;
  text-align: center;
}
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #eee;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.name {
  font-weight: 500;
}
.time {
  color: #999;
  font-size: 12px;
}
.star {
  margin: 4px 0;
}
.content {
  margin: 8px 0;
  line-height: 1.5;
}
.reply-list {
  background: #f7f8fa;
  padding: 8px 12px;
  border-radius: 6px;
  margin: 8px 0;
}
.reply-item {
  padding: 4px 0;
  font-size: 14px;
}
.r-name {
  color: #1677ff;
  font-weight: 500;
}
.op-row {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}
.op-btn {
  cursor: pointer;
}
.del {
  color: #ff4d4f;
}
.reply-box {
  margin-top: 8px;
}
.t-right {
  text-align: right;
}
.mt10 {
  margin-top: 10px;
}
.mt5 {
  margin-top: 5px;
}
</style>