<template>
  <div class="detail-page">

    <!-- 加载骨架 -->
    <div v-if="loading" class="skeleton-wrap">
      <div class="sk sk-img"></div>
      <div class="sk-right">
        <div class="sk sk-title"></div>
        <div class="sk sk-tag"></div>
        <div class="sk sk-price"></div>
        <div class="sk sk-sku"></div>
        <div class="sk sk-btn"></div>
      </div>
    </div>

    <!-- 商品详情 -->
    <div v-else class="detail-body">

      <!-- 上半区：图 + 信息 -->
      <div class="detail-main">

        <!-- 左：主图 -->
        <div class="img-wrap">
          <el-image :src="detail.pic" fit="cover" class="main-img" />
        </div>

        <!-- 右：商品信息 -->
        <div class="info-wrap">

          <!-- 标题行 -->
          <div class="title-row">
            <h1 class="product-title">{{ detail.productName }}</h1>
            <button class="collect-btn" :class="{ collected: isCollected }" @click="toggleCollect" :title="isCollected ? '取消收藏' : '收藏'">
              <svg v-if="isCollected" key="collected" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
              <svg v-else key="not-collected" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
              {{ isCollected ? '已收藏' : '收藏' }}
            </button>
          </div>

          <!-- 状态标签 -->
          <span class="status-tag" :class="detail.status === 1 ? 'on' : 'off'">
            {{ detail.status === 1 ? '在售' : '已下架' }}
          </span>

          <!-- 价格 -->
          <div class="price-row" v-if="activeSku">
            <span class="price-symbol">¥</span>
            <span class="price-num">{{ Number(activeSku.price).toFixed(2) }}</span>
          </div>

          <div class="divider"></div>

          <!-- SKU 属性选择（按 attrName 分组） -->
          <div class="section" v-for="group in attrGroups" :key="group.attrName">
            <p class="section-label">{{ group.attrName }}</p>
            <div class="sku-list">
              <button
                v-for="val in group.values"
                :key="val"
                class="sku-btn"
                :class="{
                  active: selectedAttrs[group.attrName] === val,
                  disabled: !isAttrValueAvailable(group.attrName, val)
                }"
                @click="selectAttr(group.attrName, val)"
              >
                {{ val }}
              </button>
            </div>
          </div>

          <!-- 数量 -->
          <div class="section">
            <p class="section-label">数量</p>
            <div class="qty-ctrl">
              <button class="qty-btn" @click="changeQty(-1)">−</button>
              <span class="qty-num">{{ buyNum }}</span>
              <button class="qty-btn" @click="changeQty(1)">+</button>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-row">
            <button class="btn-cart" @click="handleAddToCart">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
              加入购物车
            </button>
            <button class="btn-buy" @click="handleBuyNow">立即购买</button>
          </div>

        </div>
      </div>

      <!-- 下半区：商品详情富文本 -->
      <div class="detail-section" v-if="detail.detailHtml">
        <div class="detail-section-header">
          <span class="section-title">商品详情</span>
        </div>
        <div class="detail-html" v-html="detail.detailHtml"></div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getProductDetailService,
  getSkuListByProductIdService
} from '@/api/user/product.js'
import { addCollectService, removeCollectService, isCollectedService } from '@/api/user/collect.js'
import { addBrowseService } from '@/api/user/foot.js'
import { addToCartService } from '@/api/user/cart.js'
import { createSettleDirectService } from '@/api/user/settle.js'
import { getUserAddressService } from '@/api/user/address.js'

const route = useRoute()
const router = useRouter()
const productId = route.query.id

const detail = ref({})
const skuList = ref([])
const activeSku = ref(null)
const loading = ref(true)
const buyNum = ref(1)
const isCollected = ref(false)
const defaultAddressId = ref(null)
const selectedAttrs = ref({})

//  计算属性分组 
const attrGroups = computed(() => {
  const map = new Map()
  skuList.value.forEach(sku => {
    (sku.skuAttrList || []).forEach(attr => {
      if (!map.has(attr.attrName)) map.set(attr.attrName, new Set())
      map.get(attr.attrName).add(attr.attrValue)
    })
  })
  return Array.from(map.entries()).map(([attrName, valueSet]) => ({
    attrName,
    values: Array.from(valueSet)
  }))
})

// 判断某个属性值在当前已选条件下是否还有可用 sku
const isAttrValueAvailable = (attrName, attrValue) => {
  const hypothetical = { ...selectedAttrs.value, [attrName]: attrValue }
  return skuList.value.some(sku =>
    Object.entries(hypothetical).every(([name, value]) =>
      (sku.skuAttrList || []).some(a => a.attrName === name && a.attrValue === value)
    )
  )
}

// 根据已选属性精确匹配 sku
const matchSku = () => {
  const entries = Object.entries(selectedAttrs.value)
  if (!entries.length) return null
  return skuList.value.find(sku =>
    entries.every(([name, value]) =>
      (sku.skuAttrList || []).some(a => a.attrName === name && a.attrValue === value)
    )
  ) || null
}

// 点击某个属性值
const selectAttr = (attrName, attrValue) => {
  // 若已选则取消（toggle），否则选中
  if (selectedAttrs.value[attrName] === attrValue) {
    const next = { ...selectedAttrs.value }
    delete next[attrName]
    selectedAttrs.value = next
  } else {
    selectedAttrs.value = { ...selectedAttrs.value, [attrName]: attrValue }
  }
  activeSku.value = matchSku()
}

// 初始化默认选中第一个 sku 的属性组合
const initDefaultAttrs = () => {
  if (!skuList.value.length) return
  const firstSku = skuList.value[0]
  const attrs = {}
  ;(firstSku.skuAttrList || []).forEach(a => {
    attrs[a.attrName] = a.attrValue
  })
  selectedAttrs.value = attrs
  activeSku.value = firstSku
}

//  数据获取 
const getDetail = async () => {
  const res = await getProductDetailService(productId)
  console.log('商品详情：', res)
  detail.value = res.data
}

const getSkuList = async () => {
  const res = await getSkuListByProductIdService(productId)
  skuList.value = res.data || []
  initDefaultAttrs()
}

const changeQty = (delta) => {
  const next = buyNum.value + delta
  if (next >= 1 && next <= 99) buyNum.value = next
}

const checkCollected = async () => {
  try {
    const res = await isCollectedService(productId)
    isCollected.value = res.data
  } catch {}
}

const toggleCollect = async () => {
  try {
    if (isCollected.value) {
      await removeCollectService(productId)
      ElMessage.success('已取消收藏')
    } else {
      await addCollectService(productId)
      ElMessage.success('收藏成功')
    }
    isCollected.value = !isCollected.value
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleAddToCart = async () => {
  if (!activeSku.value) return ElMessage.warning('请选择商品规格')
  try {
    await addToCartService({ productId: detail.value.id, skuId: activeSku.value.id, quantity: buyNum.value })
    ElMessage.success('已加入购物车')
  } catch {
    ElMessage.error('加入购物车失败')
  }
}

const handleBuyNow = async () => {
  if (!activeSku.value) return ElMessage.warning('请选择规格')
  if (!defaultAddressId.value) return ElMessage.warning('请先添加收货地址')
  try {
    const res = await createSettleDirectService({
      addressId: defaultAddressId.value,
      productId: detail.value.id,
      skuId: activeSku.value.id,
      quantity: buyNum.value
    })
    ElMessage.success('创建结算单成功')
    router.push({ path: '/order/confirm', query: { settleId: res.data.id } })
  } catch (err) {
    ElMessage.error('购买失败：' + (err.msg || err.message))
  }
}

const getDefaultAddress = async () => {
  try {
    const res = await getUserAddressService()
    if (res.data?.length > 0) {
      const defaultAddr = res.data.find(a => a.isDefault === 1) || res.data[0]
      defaultAddressId.value = defaultAddr.id
    }
  } catch {
    ElMessage.error('获取地址信息失败')
  }
}

onMounted(async () => {
  await getDetail()
  await getSkuList()
  await checkCollected()
  await getDefaultAddress()
  loading.value = false
  if (productId) addBrowseService(productId).catch(() => {})
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500&family=DM+Mono&display=swap');

/* ── Page ── */
.detail-page {
  min-height: calc(100vh - 70px);
  background: #f5f5f3;
  padding: 40px 0 80px;
  font-family: 'DM Sans', sans-serif;
}

/* ── Skeleton ── */
.skeleton-wrap {
  max-width: 980px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  gap: 40px;
}
.sk {
  background: linear-gradient(90deg, #ebebeb 25%, #e2e2e0 50%, #ebebeb 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  border-radius: 8px;
}
.sk-img   { width: 420px; height: 420px; flex-shrink: 0; border-radius: 12px; }
.sk-right { flex: 1; display: flex; flex-direction: column; gap: 14px; padding-top: 8px; }
.sk-title { height: 28px; width: 70%; }
.sk-tag   { height: 20px; width: 48px; }
.sk-price { height: 40px; width: 120px; }
.sk-sku   { height: 36px; width: 80%; }
.sk-btn   { height: 44px; margin-top: 8px; }
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ── Main layout ── */
.detail-body {
  max-width: 980px;
  margin: 0 auto;
  padding: 0 24px;
}
.detail-main {
  display: flex;
  gap: 48px;
  background: #fff;
  border-radius: 12px;
  border: 0.5px solid #ebebeb;
  padding: 32px;
}

/* ── Image ── */
.img-wrap {
  width: 380px;
  flex-shrink: 0;
}
.main-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 10px;
  background: #f7f7f5;
  border: 0.5px solid #ebebeb;
  display: block;
}

/* ── Info ── */
.info-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}
.product-title {
  font-size: 18px;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0;
  line-height: 1.4;
  letter-spacing: -0.02em;
}

.collect-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: 0.5px solid #e0e0e0;
  border-radius: 6px;
  padding: 5px 10px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  font-family: 'DM Sans', sans-serif;
  transition: border-color 0.15s, color 0.15s;
}
.collect-btn:hover { border-color: #c8a84b; color: #c8a84b; }
.collect-btn.collected { border-color: #c8a84b; color: #c8a84b; }

/* Status tag */
.status-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 400;
  padding: 3px 8px;
  border-radius: 4px;
  letter-spacing: 0.02em;
  margin-bottom: 16px;
}
.status-tag.on  { background: #f0f8f0; color: #2e7d3a; border: 0.5px solid #b8debb; }
.status-tag.off { background: #fff0f0; color: #b03030; border: 0.5px solid #f0bebe; }

/* Price */
.price-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
  margin-bottom: 20px;
}
.price-symbol {
  font-size: 15px;
  color: #1a1a1a;
  font-family: 'DM Mono', monospace;
  font-weight: 400;
}
.price-num {
  font-size: 32px;
  font-weight: 500;
  color: #1a1a1a;
  font-family: 'DM Mono', monospace;
  letter-spacing: -0.03em;
}

.divider {
  height: 0.5px;
  background: #f0f0f0;
  margin-bottom: 20px;
}

/* Sections */
.section {
  margin-bottom: 20px;
}
.section-label {
  font-size: 11px;
  font-weight: 400;
  color: #bbb;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin: 0 0 10px;
}

/* SKU */
.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.sku-btn {
  padding: 7px 14px;
  border: 0.5px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  font-family: 'DM Sans', sans-serif;
  transition: border-color 0.12s, background 0.12s, color 0.12s;
}
.sku-btn:hover { border-color: #aaa; color: #1a1a1a; }
.sku-btn.active {
  border-color: #1a1a1a;
  background: #1a1a1a;
  color: #fff;
}
/* 库存不足/不可选状态 */
.sku-btn.disabled {
  opacity: 0.35;
  cursor: not-allowed;
  pointer-events: none;
}

/* Quantity */
.qty-ctrl {
  display: inline-flex;
  align-items: center;
  border: 0.5px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}
.qty-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  font-size: 16px;
  color: #888;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.1s, color 0.1s;
}
.qty-btn:hover { background: #f5f5f3; color: #1a1a1a; }
.qty-num {
  min-width: 36px;
  text-align: center;
  font-size: 13px;
  font-family: 'DM Mono', monospace;
  color: #1a1a1a;
  border-left: 0.5px solid #ebebeb;
  border-right: 0.5px solid #ebebeb;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Action buttons */
.action-row {
  display: flex;
  gap: 10px;
  margin-top: auto;
  padding-top: 8px;
}
.btn-cart, .btn-buy {
  height: 44px;
  border-radius: 8px;
  font-size: 13px;
  font-family: 'DM Sans', sans-serif;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  letter-spacing: -0.01em;
}
.btn-cart {
  flex: 1;
  background: #fff;
  border: 0.5px solid #1a1a1a;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}
.btn-cart:hover { background: #f5f5f3; }
.btn-buy {
  flex: 1;
  background: #1a1a1a;
  border: 0.5px solid #1a1a1a;
  color: #fff;
}
.btn-buy:hover { background: #333; }

/* ── Detail HTML section ── */
.detail-section {
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  border: 0.5px solid #ebebeb;
  overflow: hidden;
}
.detail-section-header {
  padding: 18px 32px;
  border-bottom: 0.5px solid #f0f0f0;
}
.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  letter-spacing: -0.01em;
}
.detail-html {
  padding: 28px 32px;
  font-size: 14px;
  line-height: 1.8;
  color: #444;
}
.detail-html :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}
</style>