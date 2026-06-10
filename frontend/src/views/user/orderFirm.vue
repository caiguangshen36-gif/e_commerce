<template>
  <div class="confirm-page">
    <div class="container" v-loading="loading">
      <template v-if="settleDetail">
        <!-- 收货地址 -->
        <div class="section address-section">
          <div class="section-header">
            <h3>收货信息</h3>
            <el-button type="primary" link @click="openAddressModal">
              <el-icon><Location /></el-icon> 更换
            </el-button>
          </div>
          <div class="address-content" v-if="currentAddress">
            <div class="addr-contact">
              <span class="name">{{ currentAddress.receiver }}</span>
              <span class="phone">{{ currentAddress.phone }}</span>
            </div>
            <div class="addr-detail">
              {{ currentAddress.province }} {{ currentAddress.city }}
              {{ currentAddress.area }} {{ currentAddress.detail }}
            </div>
          </div>
          <div class="address-empty" v-else>
            <el-empty description="暂无收货地址，请添加" :image-size="80" />
          </div>
        </div>

        <!-- 商品清单 -->
        <div class="section items-section">
          <div class="section-header">
            <h3>商品清单</h3>
            <span class="item-count">共 {{ settleDetail.items?.length || 0 }} 件</span>
          </div>
          <div class="items-list">
            <div v-for="item in settleDetail.items" :key="item.id" class="item-row">
              <el-image :src="item.pic" class="item-img" fit="cover" lazy />
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-spec">{{ item.skuSpecs || '默认规格' }}</div>
              </div>
              <div class="item-meta">
                <span class="price">¥{{ formatPrice(item.price) }}</span>
                <span class="qty">×{{ item.quantity }}</span>
              </div>
              <div class="item-subtotal">¥{{ formatPrice(item.totalPrice) }}</div>
            </div>
          </div>
        </div>

        <!-- 费用明细 -->
        <div class="section summary-section">
          <h3>费用明细</h3>
          <div class="summary-row">
            <span>商品总额</span>
            <span>¥{{ formatPrice(settleDetail.totalAmount) }}</span>
          </div>
          <div class="summary-row">
            <span>运费</span>
            <span>¥0.00</span>
          </div>
          <div class="summary-row total">
            <span>应付总额</span>
            <span class="pay-total">¥{{ formatPrice(settleDetail.totalAmount) }}</span>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="footer-action">
          <div class="footer-left">
            共 {{ settleDetail.items.length }} 件商品，合计：
            <span class="footer-total">¥{{ formatPrice(settleDetail.totalAmount) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            @click="handleSubmitOrder"
            :loading="submitting"
            :disabled="submitting || !currentAddress"
          >
            提交订单
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="未找到结算单信息" />
    </div>

    <!-- 地址选择弹窗 -->
    <el-dialog
      v-model="addressModalVisible"
      title="选择收货地址"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
    >
      <div class="address-modal-list">
        <div
          v-for="addr in addressList"
          :key="addr.id"
          class="addr-item"
          :class="{ active: selectedAddressId === addr.id }"
          @click="selectAddress(addr)"
        >
          <el-radio v-model="selectedAddressId" :label="addr.id" />
          <div class="info">
            <div class="top">
              <span class="name">{{ addr.receiver }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
            </div>
            <div class="detail">
              {{ addr.province }} {{ addr.city }} {{ addr.area }} {{ addr.detail }}
            </div>
          </div>
        </div>

        <div v-if="addressList.length === 0" style="text-align:center; padding:20px;">
          暂无收货地址
        </div>
      </div>

      <template #footer>
        <el-button @click="gotoEditAddress">编辑地址</el-button>
        <el-button @click="addressModalVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelectAddress" :disabled="!selectedAddressId">
          确认地址
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getSettleDetailService, confirmSettleService } from '@/api/user/settle.js'
import { getUserAddressService } from '@/api/user/address.js'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const settleDetail = ref(null)

// 当前订单使用的地址
const currentAddress = ref(null)

// 地址弹窗
const addressModalVisible = ref(false)
const addressList = ref([])
const selectedAddressId = ref(null)

// 价格格式化
const formatPrice = (val) => Number(val || 0).toFixed(2)

// 初始化：加载默认地址
onMounted(async () => {
  const id = route.query.settleId
  if (!id) {
    ElMessage.error('缺少结算单号')
    router.replace('/cart')
    return
  }
  await loadSettleDetail(id)
  await loadUserAddress()
})

// 加载用户所有地址，并自动选中默认地址
const loadUserAddress = async () => {
  try {
    const res = await getUserAddressService()
    addressList.value = res.data || []

    // 自动选中默认地址 / 第一条
    const defaultAddr = addressList.value.find(i => i.isDefault === 1) || addressList.value[0]
    if (defaultAddr) {
      currentAddress.value = defaultAddr
      selectedAddressId.value = defaultAddr.id
    }
  } catch (err) {
    currentAddress.value = null
  }
}

// 打开地址选择弹窗
const openAddressModal = async () => {
  await loadUserAddress()
  addressModalVisible.value = true
}

// 选中地址
const selectAddress = (addr) => {
  selectedAddressId.value = addr.id
}

// 确认选择地址 → 关闭弹窗并更新订单地址
const confirmSelectAddress = () => {
  const addr = addressList.value.find(i => i.id === selectedAddressId.value)
  if (addr) {
    currentAddress.value = addr
  }
  addressModalVisible.value = false
  ElMessage.success('已选择收货地址')
}

// 加载结算单
const loadSettleDetail = async (id) => {
  loading.value = true
  try {
    const res = await getSettleDetailService(id)
    settleDetail.value = res.data
  } catch (err) {
    ElMessage.error('获取结算单失败')
  } finally {
    loading.value = false
  }
}
const gotoEditAddress = () => {
  router.push('/userinfo/address')
}

//  提交订单，拿到订单ID后跳转到支付页
const handleSubmitOrder = async () => {
  if (!currentAddress.value) {
    ElMessage.warning('请先选择收货地址')
    return
  }

  submitting.value = true
  try {
    const res = await confirmSettleService(route.query.settleId)
    console.log('提交订单结果!!:', res)

    // 从接口返回中取出订单ID
    const orderId = res.data?.id
    if (!orderId) {
      throw new Error('订单创建失败，未获取到订单ID')
    }

    ElMessage.success('订单提交成功')
    // 跳转到支付页，传递订单ID
    router.push({ path: '/pay', query: { orderId } })
  } catch (err) {
    ElMessage.error(err.msg || err.message || '提交订单失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.confirm-page {
  min-height: calc(100vh - 70px);
  background: #f6f8fa;
  padding: 20px 0 100px;
}

.container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.section {
  padding: 20px;
  border-bottom: 1px solid #eee;

  &:last-child {
    border-bottom: none;
  }

  &.address-section {
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
  }

  &.summary-section {
    border-bottom-left-radius: 8px;
    border-bottom-right-radius: 8px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #1d2129;
  }

  .item-count {
    font-size: 13px;
    color: #86909c;
  }
}

.address-content {
  padding: 12px 0;

  .addr-contact {
    margin-bottom: 8px;

    .name {
      font-weight: 600;
      margin-right: 16px;
      color: #1d2129;
    }

    .phone {
      color: #86909c;
    }
  }

  .addr-detail {
    color: #1d2129;
    font-size: 15px;
    line-height: 1.5;
  }
}

.items-list {
  .item-row {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px dashed #e8e8e8;

    &:last-child {
      border-bottom: none;
    }
  }

  .item-img {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    border: 1px solid #e5e6eb;
    background: #f9fafb;
    flex-shrink: 0;
  }

  .item-info {
    flex: 1;
    margin-left: 12px;
    min-width: 0;
  }

  .item-name {
    font-size: 14px;
    color: #1d2129;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .item-spec {
    font-size: 12px;
    color: #86909c;
    margin-top: 4px;
  }

  .item-meta {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    margin: 0 16px;

    .price {
      font-size: 14px;
      color: #86909c;
    }

    .qty {
      font-size: 13px;
      color: #86909c;
      margin-top: 4px;
    }
  }

  .item-subtotal {
    font-size: 15px;
    font-weight: 600;
    color: #e65c5c;
    width: 80px;
    text-align: right;
  }
}

.summary-section {
  .summary-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    font-size: 14px;
    color: #86909c;

    &.total {
      padding-top: 12px;
      margin-top: 4px;
      border-top: 1px solid #e5e6eb;
      font-weight: 600;
      color: #1d2129;

      .pay-total {
        font-size: 18px;
        color: #e65c5c;
      }
    }
  }
}

.footer-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #e5e6eb;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  z-index: 10;

  .footer-left {
    font-size: 14px;
    color: #86909c;
  }

  .footer-total {
    font-size: 18px;
    font-weight: 700;
    color: #e65c5c;
    margin-left: 4px;
  }

  .el-button {
    min-width: 120px;
  }
}

.address-modal-list {
  max-height: 400px;
  overflow-y: auto;
}

.addr-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 8px;
  border: 1px solid #eee;

  &.active {
    background: #f5f9ff;
    border-color: #409eff;
  }

  .info {
    margin-left: 8px;
    flex: 1;

    .top {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 4px;
    }

    .detail {
      font-size: 13px;
      color: #666;
    }
  }
}
</style>