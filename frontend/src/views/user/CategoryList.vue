<template>
  <div class="product-list-page">
    <div class="container">
      <!-- 顶部标题 -->
      <div class="list-header">
        <h2 class="title">商品列表</h2>
        <span class="tip">共 {{ productList.length }} 个商品</span>
      </div>

      <!-- 商品网格 -->
      <div class="product-grid" v-loading="loading">
        <div
          class="product-card"
          v-for="item in pageProducts"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <div class="img-wrap">
            <img :src="item.pic" alt="" class="img" />
          </div>
          <div class="info">
            <div class="name">{{ item.productName }}</div>
            <div class="price">¥{{ item.skuList?.[0]?.price || 0 }}</div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty" v-if="!loading && pageProducts.length === 0">
          该分类下暂无商品
        </div>
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductListByCategoryIdService } from '@/api/user/product.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const productList = ref([])
const currentPage = ref(1)
const pageSize = ref(12) 
const total = computed(() => productList.value.length)

// 分页后的数据
const pageProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return productList.value.slice(start, end)
})

onMounted(() => {
  getProductList()
})

async function getProductList() {
  const categoryId = route.query.categoryId
  if (!categoryId) return

  loading.value = true
  currentPage.value = 1 
  try {
    const res = await getProductListByCategoryIdService(categoryId)
    console.log('商品列表数据:', res)
    productList.value = res.data || []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 跳详情
function goDetail(id) {
  router.push({
    path: '/product/detail',
    query: { id }
  })
}
</script>

<style scoped>
.product-list-page {
  min-height: calc(100vh - 70px);
  background: #f5f7fa;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 18px;
  font-weight: 500;
  margin: 0;
}

.tip {
  font-size: 13px;
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  min-height: 300px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.img-wrap {
  width: 100%;
  height: 200px;
}

.img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  padding: 12px;
}

.name {
  font-size: 14px;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price {
  font-size: 16px;
  font-weight: bold;
  color: #e63946;
}

.empty {
  grid-column: 1/-1;
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>