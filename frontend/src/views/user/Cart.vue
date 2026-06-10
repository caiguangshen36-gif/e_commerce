<template>
  <div class="cart-page">
    <div class="container">
      <div class="cart-wrap">

        <div class="cart-header">
          <p class="cart-title">购物车</p>
          <span class="cart-count-tag">{{ cartList.length }} 件商品</span>
        </div>

        <table class="cart-table">
          <thead>
            <tr>
              <th class="col-check"></th>
              <th>商品</th>
              <th class="col-price right">单价</th>
              <th class="col-qty center">数量</th>
              <th class="col-sub right">小计</th>
              <th class="col-del"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="loading-row">加载中...</td>
            </tr>
            <tr v-else-if="cartList.length === 0">
              <td colspan="6" class="empty-row">购物车还是空的</td>
            </tr>
            <tr
              v-for="item in cartList"
              :key="item.id"
              class="cart-row"
            >
              <td>
                <div class="check-wrap">
                  <input
                    type="checkbox"
                    class="custom-check"
                    :checked="isSelected(item)"
                    @change="toggleSelect(item)"
                  />
                </div>
              </td>
              <td>
                <div class="product-cell">
                  <div class="product-img">
                    <el-image :src="item.pic" fit="cover" style="width:100%;height:100%" />
                  </div>
                  <div>
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-spec">{{ item.skuSpecs }}</div>
                  </div>
                </div>
              </td>
              <td class="right">
                <span class="price-unit">¥{{ Number(item.price).toFixed(2) }}</span>
              </td>
              <td>
                <div class="qty-ctrl">
                  <button class="qty-btn" @click="changeQty(item, -1)">−</button>
                  <span class="qty-num">{{ item.quantity }}</span>
                  <button class="qty-btn" @click="changeQty(item, 1)">+</button>
                </div>
              </td>
              <td class="right">
                <span class="price-sub">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </td>
              <td>
                <button class="del-btn" title="删除" @click="handleDelete(item.id)">
                  <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
                    <path d="M2.5 3.5h10M5.5 3.5V2.5h4v1M4 3.5l.8 9h5.4l.8-9"
                      stroke="currentColor" stroke-width="1.3"
                      stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="cart-footer">
          <div class="footer-left">
            <label class="check-all-wrap">
              <input
                type="checkbox"
                class="custom-check"
                :checked="isAllSelected"
                :indeterminate.prop="isIndeterminate"
                @change="handleCheckAll"
              />
              <span>全选</span>
            </label>
            <button class="del-selected" @click="deleteBatch">删除选中</button>
          </div>
          <div class="footer-right">
            <div class="summary-text">
              已选 <strong>{{ totalNum }}</strong> 件 &nbsp;·&nbsp; 合计
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <button class="pay-btn" @click="toPay">
              去结算
              <span class="pay-badge">{{ selectedCartList.length }}</span>
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getCartListService,
  updateCartItemService,
  deleteCartItemService,
} from '@/api/user/cart.js'
import { createSettleService } from '@/api/user/settle.js'
import { getUserAddressService } from '@/api/user/address.js'


const router = useRouter()
const loading = ref(false)
const cartList = ref([])
const selectedCartList = ref([])
const defaultAddressId = ref(null)

onMounted(async () => {
  await getAddressInfo()
  getCartList()
})

const getCartList = async () => {
  loading.value = true
  try {
    const res = await getCartListService()
    cartList.value = res.data || []
  } catch {
    ElMessage.error('获取购物车失败')
  } finally {
    loading.value = false
  }
}

const getAddressInfo = async () => {
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

const isSelected = (item) => selectedCartList.value.some(i => i.id === item.id)

const toggleSelect = (item) => {
  const idx = selectedCartList.value.findIndex(i => i.id === item.id)
  if (idx === -1) selectedCartList.value.push(item)
  else selectedCartList.value.splice(idx, 1)
}

const isAllSelected = computed(() =>
  cartList.value.length > 0 && selectedCartList.value.length === cartList.value.length
)

const isIndeterminate = computed(() =>
  selectedCartList.value.length > 0 && selectedCartList.value.length < cartList.value.length
)

const handleCheckAll = (e) => {
  if (e.target.checked) {
    selectedCartList.value = [...cartList.value]
  } else {
    selectedCartList.value = []
  }
}

const changeQty = async (item, delta) => {
  const next = item.quantity + delta
  if (next < 1 || next > 10) return
  item.quantity = next
  try {
    await updateCartItemService({ id: item.id, quantity: next })
  } catch {
    item.quantity -= delta
    ElMessage.error('修改失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
    await deleteCartItemService(id)
    cartList.value = cartList.value.filter(i => i.id !== id)
    selectedCartList.value = selectedCartList.value.filter(i => i.id !== id)
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const deleteBatch = async () => {
  if (!selectedCartList.value.length) return ElMessage.warning('请选择商品')
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedCartList.value.length} 件商品？`, '提示', { type: 'warning' })
    for (const item of selectedCartList.value) {
      await deleteCartItemService(item.id)
    }
    const ids = new Set(selectedCartList.value.map(i => i.id))
    cartList.value = cartList.value.filter(i => !ids.has(i.id))
    selectedCartList.value = []
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const toPay = async () => {
  if (!selectedCartList.value.length) return ElMessage.warning('请选择要结算的商品')
  if (!defaultAddressId.value) return ElMessage.warning('请先添加收货地址')
  try {
    const res = await createSettleService({
      addressId: defaultAddressId.value,
      cartIds: selectedCartList.value.map(i => i.id),
    })

    console.log('结算单创建成功', res.data)
    ElMessage.success('结算单创建成功')
    router.push({ path: '/order/confirm', query: { settleId: res.data.id } })
  } catch (err) {
    ElMessage.error(err.msg || '创建结算单失败')
  }
}

const totalNum = computed(() =>
  selectedCartList.value.reduce((s, i) => s + i.quantity, 0)
)
const totalPrice = computed(() =>
  selectedCartList.value.reduce((s, i) => s + i.price * i.quantity, 0)
)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500&family=DM+Mono:wght@400&display=swap');

.cart-page {
  min-height: calc(100vh - 70px);
  background: #f5f5f3;
  padding: 40px 0;
}

.container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.cart-wrap {
  background: #fff;
  border-radius: 12px;
  padding: 28px 40px;
  border: 0.5px solid #e8e8e6;
}

/* Header */
.cart-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 20px;
  border-bottom: 0.5px solid #ebebeb;
  margin-bottom: 0;
}
.cart-title {
  font-family: 'DM Sans', sans-serif;
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.01em;
}
.cart-count-tag {
  font-size: 12px;
  color: #aaa;
  font-weight: 400;
}

/* Table */
.cart-table {
  width: 100%;
  border-collapse: collapse;
  font-family: 'DM Sans', sans-serif;
}
.cart-table thead th {
  font-size: 11px;
  font-weight: 400;
  color: #bbb;
  text-align: left;
  padding: 14px 8px;
  border-bottom: 0.5px solid #ebebeb;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}
.cart-table thead th.center { text-align: center; }
.cart-table thead th.right  { text-align: right; }

.col-check { width: 40px; }
.col-price { width: 90px; }
.col-qty   { width: 120px; }
.col-sub   { width: 100px; }
.col-del   { width: 44px; }

.cart-row { border-bottom: 0.5px solid #f0f0f0; }
.cart-row td { padding: 20px 8px; vertical-align: middle; }

.right { text-align: right; }

/* Checkbox */
.check-wrap { display: flex; justify-content: center; }
.custom-check {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #d8d8d8;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  background: #fff;
  transition: background 0.15s, border-color 0.15s;
  flex-shrink: 0;
  position: relative;
}
.custom-check:checked {
  background: #1a1a1a;
  border-color: #1a1a1a;
}
.custom-check:checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 2px;
  width: 4px;
  height: 7px;
  border: 1.5px solid #fff;
  border-left: none;
  border-top: none;
  transform: rotate(40deg);
}
.custom-check:indeterminate {
  background: #1a1a1a;
  border-color: #1a1a1a;
}
.custom-check:indeterminate::after {
  content: '';
  position: absolute;
  left: 3px;
  top: 6px;
  width: 8px;
  height: 1.5px;
  background: #fff;
}

/* Product cell */
.product-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}
.product-img {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  background: #f7f7f5;
  flex-shrink: 0;
  overflow: hidden;
  border: 0.5px solid #ebebeb;
}
.product-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  line-height: 1.4;
}
.product-spec {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  font-family: 'DM Mono', monospace;
}

/* Price */
.price-unit {
  font-size: 13px;
  color: #888;
  font-family: 'DM Mono', monospace;
}
.price-sub {
  font-size: 13px;
  color: #1a1a1a;
  font-weight: 500;
  font-family: 'DM Mono', monospace;
}

/* Qty control */
.qty-ctrl {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 0.5px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
  width: 90px;
  margin: 0 auto;
}
.qty-btn {
  background: none;
  border: none;
  width: 28px;
  height: 28px;
  cursor: pointer;
  font-size: 15px;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.12s, color 0.12s;
  flex-shrink: 0;
}
.qty-btn:hover { background: #f5f5f3; color: #1a1a1a; }
.qty-num {
  font-size: 12px;
  font-family: 'DM Mono', monospace;
  color: #1a1a1a;
  min-width: 28px;
  text-align: center;
  border-left: 0.5px solid #ebebeb;
  border-right: 0.5px solid #ebebeb;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Delete button */
.del-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #ccc;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  margin: 0 auto;
  transition: background 0.15s, color 0.15s;
}
.del-btn:hover { background: #fff0f0; color: #c0392b; }

/* Footer */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  margin-top: 4px;
  border-top: 0.5px solid #f0f0f0;
}
.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.check-all-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  user-select: none;
}
.del-selected {
  background: none;
  border: none;
  font-size: 13px;
  color: #bbb;
  cursor: pointer;
  padding: 0;
  font-family: 'DM Sans', sans-serif;
  transition: color 0.15s;
}
.del-selected:hover { color: #c0392b; }

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.summary-text {
  font-size: 13px;
  color: #888;
}
.summary-text strong {
  color: #1a1a1a;
  font-weight: 500;
}
.total-price {
  font-size: 18px;
  font-weight: 500;
  color: #1a1a1a;
  font-family: 'DM Mono', monospace;
  letter-spacing: -0.02em;
  margin-left: 2px;
}

/* Pay button */
.pay-btn {
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 22px;
  font-size: 13px;
  font-family: 'DM Sans', sans-serif;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: -0.01em;
  transition: background 0.15s;
}
.pay-btn:hover { background: #333; }
.pay-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 1px 7px;
  border-radius: 12px;
  font-size: 11px;
  font-family: 'DM Mono', monospace;
}

/* States */
.empty-row,
.loading-row {
  padding: 48px 0;
  text-align: center;
  color: #bbb;
  font-size: 13px;
}
</style>