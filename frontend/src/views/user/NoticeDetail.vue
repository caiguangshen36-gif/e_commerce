<template>
  <div class="user-notice-detail">
    <div class="page-header">
      <h2>消息详情</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <div class="detail-card" v-if="detail">
      <div class="detail-title">{{ detail.title }}</div>
      <div class="info">
        <span>类型：{{ detail.noticeTypeText }}</span>
        <span>状态：{{ detail.isReadText }}</span>
        <span>时间：{{ detail.createTime }}</span>
      </div>
      <div class="content-box">
        <div class="label">消息内容</div>
        <div class="content">{{ detail.content }}</div>
      </div>
      <div class="biz" v-if="detail.bizId">关联单号：{{ detail.bizId }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getUserNoticeDetailService,
  markUserNoticeReadService
} from '@/api/user/notice.js'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const id = route.query.id

const getDetail = async () => {
  try {
    const res = await getUserNoticeDetailService(id)
    detail.value = res.data

    if (detail.value.isRead === 0) {
      await markUserNoticeReadService([id])
    }
  } catch (e) {
    ElMessage.error('消息不存在')
    goBack()
  }
}

const goBack = () => {
  router.push('/userinfo/notice')
}

onMounted(() => getDetail())
</script>

<style scoped>
.user-notice-detail {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.detail-card {
  line-height: 1.8;
}
.detail-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}
.info {
  color: #666;
  font-size: 13px;
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
}
.content-box {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
}
.label {
  font-weight: bold;
  margin-bottom: 6px;
}
.biz {
  margin-top: 15px;
  color: #666;
}
</style>