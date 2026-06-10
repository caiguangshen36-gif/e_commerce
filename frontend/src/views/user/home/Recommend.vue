<template>
  <div class="recommend-product">

    <!-- 标题栏 -->
    <div class="section-header">
      <div class="title-box">
        <span class="like-tag">推荐</span>
        <h2 class="section-title">猜你喜欢</h2>
      </div>
      <span class="section-line"></span>
    </div>

    <!-- 骨架屏 -->
    <div class="product-grid" v-if="loading">
      <div class="product-card sk-card" v-for="n in 12" :key="n">
        <div class="sk sk-img"></div>
        <div class="card-body">
          <div class="sk sk-name"></div>
          <div class="sk sk-name2"></div>
          <div class="sk sk-price"></div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid" v-else-if="productList.length">
      <div
        class="product-card"
        v-for="item in productList"
        :key="item.id"
        @click="toDetail(item)"
      >
        <div class="card-img-wrap">
          <img :src="item.pic" :alt="item.productName" class="card-img" />
        </div>
        <div class="card-body">
          <p class="card-name">{{ item.productName }}</p>
          <div class="card-footer">
            <span class="card-price">
              <span class="price-sym">¥</span>{{ item.skuList?.[0]?.price?.toFixed(2) || '—' }}
            </span>
            <span class="card-arrow">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                <path d="M5 12h14M13 6l6 6-6 6"/>
              </svg>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#d8d8d8" stroke-width="1.2" stroke-linecap="round">
        <path d="M12 2.69l5.44 3.9 2.08 7.4-4.16 6.23L12 21.31l-5.44-3.9-2.08-7.4 4.16-6.23L12 2.69z"/>
      </svg>
      <p>暂无推荐商品</p>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 引入你的推荐商品接口
import { getRecommendProductListService } from '@/api/user/product.js'

const router = useRouter()
const productList = ref([])
const loading = ref(false)

// 获取推荐商品
const getRecommendList = async () => {
  loading.value = true
  try {
    const res = await getRecommendProductListService()
    console.log('猜你喜欢：', res)
    productList.value = res.data || []
  } catch (err) {
    console.error('获取推荐失败：', err)
  } finally {
    loading.value = false
  }
}

// 跳商品详情
const toDetail = (item) => {
  router.push({ path: '/product/detail', query: { id: item.id } })
}

onMounted(() => {
  getRecommendList()
})
</script>

<style scoped>
.recommend-product {
  padding: 0 0 80px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}
.title-box {
  display: flex;
  align-items: center;
  gap: 12px;
}
.like-tag {
  background: linear-gradient(135deg, #cd630b, #e7830a);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  letter-spacing: 1px;
}
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #111;
  margin: 0;
  letter-spacing: 1px;
}
.section-line {
  flex: 1;
  height: 1px;
  background: #eee;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

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
  box-shadow: 0 4px 18px rgba(0,0,0,0.07);
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
  transform: scale(1.04);
}

.card-body {
  padding: 12px 14px 12px;
}
.card-name {
  font-size: 13px;
  color: #1a1a1a;
  margin: 0 0 10px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-price {
  font-size: 15px;
  color: #e63946;
  font-weight: 800;
}
.price-sym {
  font-size: 11px;
  margin-right: 1px;
}
.card-arrow {
  color: #ccc;
  transition: color 0.15s, transform 0.15s;
}
.product-card:hover .card-arrow {
  color: #888;
  transform: translateX(2px);
}


.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 80px 0;
  color: #bbb;
  font-size: 13px;
}
.empty-state p { margin: 0; }

/* 骨架屏 */
.sk-card { pointer-events: none; }
.sk {
  border-radius: 6px;
  background: linear-gradient(90deg, #ebebeb 25%, #e2e2e0 50%, #ebebeb 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.sk-img   { width: 100%; aspect-ratio: 1; border-radius: 0; }
.sk-name  { height: 12px; width: 88%; margin: 12px 14px 6px; }
.sk-name2 { height: 12px; width: 60%; margin: 0 14px 10px; }
.sk-price { height: 15px; width: 38%; margin: 0 14px 14px; }
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>