<template>
  <div class="search-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-box">
        <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索商品..."
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <!-- 平铺式搜索历史（放在搜索框和猜你喜欢中间） -->
    <div class="history-section" v-if="historyList.length > 0">
      <div class="section-header">
        <span class="title">最近搜索</span>
        <el-button text size="small" type="danger" @click="clearAllHistory">
          清空
        </el-button>
      </div>
      <div class="history-tags">
        <div
          class="tag-item"
          v-for="item in historyList"
          :key="item"
          @click="selectHistory(item)"
        >
          <span class="tag-text">{{ item }}</span>
          <svg
            class="del-icon"
            width="12"
            height="12"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
            @click.stop="deleteHistory(item)"
          >
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 猜你喜欢 -->
    <Recommend class="recommend-section" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElButton } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getSearchHistoryService,
  addSearchHistoryService,
  deleteSearchHistoryService,
  clearSearchHistoryService
} from '@/api/user/search.js'

import Recommend from './Recommend.vue'

const router = useRouter()
const keyword = ref('')
const historyList = ref([])

// 加载搜索历史
const loadHistory = async () => {
  try {
    const res = await getSearchHistoryService()
    historyList.value = res.data || []
  } catch (e) {
    console.error('加载历史失败', e)
  }
}

// 搜索
const handleSearch = async () => {
  const val = keyword.value.trim()
  if (!val) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  await addSearchHistoryService(val)
  // 搜索后刷新历史列表
  await loadHistory()
  router.push({ path: '/searchResult', query: { keyword: val } })
}

// 点击历史搜索
const selectHistory = (val) => {
  keyword.value = val
  handleSearch()
}

// 删除单条历史
const deleteHistory = async (val) => {
  await deleteSearchHistoryService(val)
  historyList.value = historyList.value.filter(i => i !== val)
}

// 清空全部历史
const clearAllHistory = async () => {
  await clearSearchHistoryService()
  historyList.value = []
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500&display=swap');

/* 整体容器 */
.search-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 15px;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  width: 800px;
  height: 38px;
  background: #f5f5f3;
  border: 0.5px solid #e0e0de;
  border-radius: 8px;
  padding: 0 6px 0 12px;
  gap: 8px;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
}
.search-box:focus-within {
  background: #fff;
  border-color: #aaa;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.search-icon {
  color: #bbb;
  flex-shrink: 0;
  transition: color 0.15s;
}
.search-box:focus-within .search-icon {
  color: #888;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  font-family: 'DM Sans', sans-serif;
  color: #1a1a1a;
  caret-color: #1a1a1a;
  min-width: 0;
}
.search-input::placeholder {
  color: #bbb;
  font-weight: 300;
}

.search-btn {
  flex-shrink: 0;
  height: 28px;
  padding: 0 14px;
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-family: 'DM Sans', sans-serif;
  font-weight: 500;
  cursor: pointer;
  letter-spacing: -0.01em;
  transition: background 0.15s;
}
.search-btn:hover {
  background: #333;
}

/* 平铺式搜索历史 */
.history-section {
  margin-bottom: 32px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}
.title {
  font-weight: 500;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f5f5f3;
  border-radius: 16px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: background 0.15s;
}
.tag-item:hover {
  background: #ebebe8;
}
.tag-text {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.del-icon {
  color: #bbb;
  cursor: pointer;
  transition: color 0.15s;
}
.del-icon:hover {
  color: #666;
}

/* 猜你喜欢 */
.recommend-section {
  margin-top: 0;
}
</style>