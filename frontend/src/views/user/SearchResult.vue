<template>
  <div class="search-page">
    <div class="container">

      <!-- 标题栏 -->
      <div class="search-header">
        <p class="search-meta">
          搜索「<span class="keyword">{{ keyword }}</span>」
          <span class="result-count" v-if="!loading && productList.length">
            · {{ productList.length }} 件结果
          </span>
        </p>
      </div>

      <!-- 骨架屏 -->
      <div v-if="loading" class="product-grid">
        <div class="product-card skeleton-card" v-for="n in 8" :key="n">
          <div class="sk sk-img"></div>
          <div class="sk sk-name"></div>
          <div class="sk sk-name2"></div>
          <div class="sk sk-price"></div>
        </div>
      </div>

      <!-- 空结果 -->
      <div v-else-if="productList.length === 0" class="empty-state">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#d0d0d0" stroke-width="1.2" stroke-linecap="round">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          <path d="M11 8v3M11 14h.01"/>
        </svg>
        <p class="empty-title">没有找到「{{ keyword }}」相关商品</p>
        <p class="empty-sub">换个关键词试试看</p>
      </div>

      <!-- 商品网格 -->
      <div v-else class="product-grid">
        <div
          class="product-card"
          v-for="item in productList"
          :key="item.id"
          @click="toDetail(item.id)"
        >
          <div class="card-img-wrap">
            <img :src="item.pic" :alt="item.productName" class="card-img" />
          </div>
          <div class="card-body">
            <p class="card-name">{{ item.productName }}</p>
            <div class="card-footer">
              <span class="card-price">
                <span class="price-sym">¥</span>{{ item.skuList?.[0]?.price?.toFixed(2) || '0.00' }}
              </span>
              <span class="card-arrow">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <path d="M5 12h14M13 6l6 6-6 6"/>
                </svg>
              </span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchProductService } from '@/api/user/product.js'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const productList = ref([])
const loading = ref(false)

const getSearchResult = async () => {
  if (!keyword.value) return
  loading.value = true
  try {
    const res = await searchProductService(keyword.value)
    console.log('搜索结果：', res)
    productList.value = res.data || []
  } catch {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query,
  (query) => {
    const kw = query.keyword || ''
    keyword.value = kw
    if (kw) getSearchResult()
  },
  { immediate: true } // 组件初次挂载时也执行一次
)

const toDetail = (id) => {
  router.push({ path: '/product/detail', query: { id } })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500&family=DM+Mono&display=swap');

.search-page {
  min-height: calc(100vh - 70px);
  background: #f5f5f3;
  padding: 36px 0 80px;
  font-family: 'DM Sans', sans-serif;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ── Header ── */
.search-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 0.5px solid #e8e8e6;
}
.search-meta {
  font-size: 14px;
  color: #888;
  margin: 0;
}
.keyword {
  color: #1a1a1a;
  font-weight: 500;
}
.result-count {
  font-family: 'DM Mono', monospace;
  font-size: 13px;
}

/* ── Grid ── */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}

/* ── Card ── */
.product-card {
  background: #fff;
  border: 0.5px solid #ebebeb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s, transform 0.15s;
}
.product-card:hover {
  border-color: #d0d0ce;
  box-shadow: 0 4px 16px rgba(0,0,0,0.07);
  transform: translateY(-2px);
}

.card-img-wrap {
  width: 100%;
  aspect-ratio: 1;
  background: #f7f7f5;
  overflow: hidden;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.product-card:hover .card-img {
  transform: scale(1.03);
}

.card-body {
  padding: 14px 14px 12px;
}
.card-name {
  font-size: 13px;
  font-weight: 400;
  color: #1a1a1a;
  line-height: 1.4;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-price {
  font-family: 'DM Mono', monospace;
  font-size: 15px;
  font-weight: 400;
  color: #1a1a1a;
}
.price-sym {
  font-size: 12px;
  margin-right: 1px;
}
.card-arrow {
  color: #ccc;
  display: flex;
  align-items: center;
  transition: color 0.15s, transform 0.15s;
}
.product-card:hover .card-arrow {
  color: #888;
  transform: translateX(2px);
}

/* ── Empty ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 100px 0;
  text-align: center;
}
.empty-title {
  font-size: 14px;
  color: #888;
  margin: 0;
}
.empty-sub {
  font-size: 12px;
  color: #bbb;
  margin: 0;
}

/* ── Skeleton ── */
.skeleton-card {
  pointer-events: none;
}
.sk {
  border-radius: 6px;
  background: linear-gradient(90deg, #ebebeb 25%, #e2e2e0 50%, #ebebeb 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.sk-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 0;
}
.sk-name  { height: 13px; width: 85%; margin: 14px 14px 6px; }
.sk-name2 { height: 13px; width: 55%; margin: 0 14px 12px; }
.sk-price { height: 15px; width: 40%; margin: 0 14px 14px; }
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>