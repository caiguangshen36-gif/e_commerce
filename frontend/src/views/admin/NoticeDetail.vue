<template>
  <div class="page-container detail-page">
    <div class="page-header">
      <h2>消息详情</h2>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <div class="detail-card" v-if="detail">
      <div class="detail-title">{{ detail.title }}</div>

      <div class="detail-info">
        <span>类型：{{ detail.noticeTypeText }}</span>
        <span>状态：{{ detail.isReadText }}</span>
        <span>时间：{{ detail.createTime }}</span>
      </div>

      <div class="detail-content" v-if="detail.bizId">
        关联单号：{{ detail.bizId }}
      </div>

      <div class="detail-content">
        <div class="label">消息内容：</div>
        <div class="content-text">{{ detail.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getAdminNoticeDetailService,
  markAdminNoticeReadService
} from '@/api/admin/notice.js'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const id = route.query.id

// 获取详情
const getDetail = async () => {
  try {
    const res = await getAdminNoticeDetailService(id)
    detail.value = res.data

    // 未读 → 自动标为已读
    if (detail.value && detail.value.isRead === 0) {
      await markAdminNoticeReadService([id])
    }
  } catch (e) {
    ElMessage.error('消息不存在或已删除')
    setTimeout(() => goBack(), 1000)
  }
}

// 返回
const goBack = () => {
  router.push('/admin/notice')
}

onMounted(() => getDetail())
</script>

<style scoped>
.page-container { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.detail-card { padding: 10px 20px; line-height: 1.8; }
.detail-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; }
.detail-info { color: #666; margin-bottom: 15px; }
.detail-info span { margin-right: 20px; }
.detail-content { margin: 15px 0; color: #333; }
.label { font-weight: bold; margin-bottom: 5px; }
.content-text { padding: 10px; background: #f9f9f9; border-radius: 6px; }
</style>