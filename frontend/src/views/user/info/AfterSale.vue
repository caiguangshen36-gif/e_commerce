<template>
  <div class="after-sale-list-page">
    <div class="container">
      <h2 class="page-title">我的售后</h2>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="form-item">
          <label>售后单号</label>
          <el-input
            v-model="queryParams.afterSaleSn"
            placeholder="请输入售后单号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="form-item">
          <label>状态</label>
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="已退款" :value="2" />
            <el-option label="驳回" :value="3" />
            <el-option label="用户已退货" :value="4" />
            <el-option label="商家收货完成" :value="5" />
          </el-select>
        </div>

        <div class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="list.length === 0 && !loading" class="empty-box">
        <el-empty description="暂无售后记录" />
      </div>

      <!-- 售后列表 -->
      <div class="after-list" v-loading="loading">
        <div class="after-item" v-for="item in list" :key="item.id">
          <!-- 头部 -->
          <div class="after-head">
            <span>售后单号：{{ item.afterSaleSn }}</span>
            <span>{{ formatTime(item.createTime) }}</span>
            <el-tag :type="getStatusTagType(item.status)" size="small">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>

          <!-- 商品信息行 -->
          <div class="goods-info-row">
            <el-image
              :src="item.skuInfo?.pic || '/images/default.png'"
              class="goods-img"
              fit="cover"
              lazy
              error="/images/default.png"
            />
            <div class="goods-info">
              <div class="name">{{ item.skuInfo?.productName || '商品名称' }}</div>
              <div class="spec">{{ item.skuInfo?.skuSpecs || '默认规格' }}</div>
              <div class="type">售后类型：{{ getTypeText(item.type) }}</div>
            </div>
            <div class="refund-col">¥{{ formatPrice(item.refundAmount) }}</div>
          </div>

          <!-- 底部 -->
          <div class="after-foot">
            <div class="total">
              退款金额：<span class="price">¥{{ formatPrice(item.refundAmount) }}</span>
            </div>
            <div class="btns">
              <el-button size="small" @click="toDetail(item.id)">查看详情</el-button>
              <el-button
                v-if="item.status === 1 && item.type === 2"
                type="primary"
                size="small"
                @click="toFillDelivery(item)"
              >
                填写退货物流
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserAfterSaleListService } from '@/api/user/afterSale.js'
import { getSkuListByProductIdService } from '@/api/user/product.js'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const pageNum = ref(1)   
const pageSize = ref(10) 
const total = ref(0)     

// 查询条件
const queryParams = reactive({
  afterSaleSn: '',
  status: undefined
})

// 获取单个商品SKU信息（兜底逻辑）
const fetchItemSkuInfo = async (item) => {
  if (item.skuInfo || !item.productId) return
  try {
    const res = await getSkuListByProductIdService(item.productId)
    if (res.data?.length) {
      item.skuInfo = res.data[0]
    }
  } catch (e) {
    console.error('获取商品信息失败', e)
    item.skuInfo = {}
  }
}

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      afterSaleSn: queryParams.afterSaleSn || null,
      status: queryParams.status ?? null
    }

    const res = await getUserAfterSaleListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      const records = pageData.records || pageData.list || pageData.rows || []

      await Promise.all(records.map(item => fetchItemSkuInfo(item)))

      list.value = records
      total.value = pageData.total || 0
    }
  } catch (e) {
    console.error('加载售后列表失败', e)
    ElMessage.error('加载失败')
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  getList()
}

const resetQuery = () => {
  queryParams.afterSaleSn = ''
  queryParams.status = undefined
  pageNum.value = 1
  getList()
}

// 状态文本
const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '审核通过',
    2: '已退款',
    3: '驳回',
    4: '用户已退货',
    5: '商家收货完成'
  }
  return map[status] || '未知'
}

// 状态标签
const getStatusTagType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'info',
    5: 'info'
  }
  return map[status] || 'info'
}

// 售后类型
const getTypeText = (type) => {
  const map = { 1: '仅退款', 2: '退货退款' }
  return map[type] || '其他'
}

// 格式化
const formatPrice = (val) => Number(val || 0).toFixed(2)
const formatTime = (t) => t ? String(t).replace('T', ' ') : '--'

// 跳转
const toDetail = (id) => {
  router.push({ path: '/afterSaleDetail', query: { id } })
}
const toFillDelivery = (item) => {
  router.push({
    path: '/afterSaleDelivery',
    query: { afterSaleId: item.id, afterSaleSn: item.afterSaleSn }
  })
}

onMounted(() => getList())
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-bottom: 20px;
}
</style>

<style scoped>
.after-sale-list-page {
  width: 100%;
}
.container {
  width: 100%;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.form-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.form-item label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}
.btn-group {
  display: flex;
  gap: 8px;
}

.empty-box {
  background: #fff;
  padding: 60px 0;
  border-radius: 8px;
}
.after-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.after-item {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.after-head {
  padding: 12px 16px;
  background: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
  border-bottom: 1px solid #eee;
}
.goods-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.goods-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  flex-shrink: 0;
}
.goods-info {
  flex: 1;
  font-size: 13px;
}
.name {
  font-weight: 500;
  margin-bottom: 2px;
}
.spec {
  color: #999;
  margin-bottom: 2px;
}
.type {
  color: #409eff;
}
.refund-col {
  width: 100px;
  text-align: right;
  font-size: 13px;
  color: #f56c6c;
  font-weight: 500;
}
.after-foot {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
}
.total {
  font-size: 14px;
}
.price {
  color: #f56c6c;
  font-weight: 500;
}
.btns {
  display: flex;
  gap: 10px;
}
</style>