<template>
  <div class="order-list-page">
    <div class="container">
      <h2 class="page-title">我的订单</h2>

      <!-- 搜索筛选栏 -->
      <div class="search-bar">
        <div class="form-item">
          <label>订单编号</label>
          <el-input
            v-model="queryParams.orderSn"
            placeholder="请输入订单编号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="form-item">
          <label>订单状态</label>
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="待收货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </div>

        <div class="form-item">
          <label>下单时间</label>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 300px"
            @change="handleDateChange"
          />
        </div>

        <div class="btn-group">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="list.length === 0 && !loading" class="empty-box">
        <el-empty description="暂无订单" />
      </div>

      <!-- 订单列表 -->
      <div class="order-list" v-loading="loading">
        <div class="order-item" v-for="item in list" :key="item.id">
          <!-- 订单头部 -->
          <div class="order-head">
            <span>订单号：{{ item.orderSn }}</span>
            <span>{{ item.createTime }}</span>
            <el-tag :type="getStatusTagType(item.status)" size="small">
              {{ item.statusText || getStatusText(item.status) }}
            </el-tag>
          </div>

          <!-- 商品列表 -->
          <div class="goods-list">
            <div
              class="goods-item"
              v-for="sku in item.orderItems"
              :key="sku.id"
            >
              <el-image
                :src="sku.pic || '/images/default.png'"
                class="goods-img"
                fit="cover"
                lazy
                error="/images/default.png"
              />
              <div class="goods-info">
                <div class="name">{{ sku.productName }}</div>
                <div class="spec">{{ sku.skuSpecs || '默认规格' }}</div>
              </div>
              <div class="price-col">¥{{ sku.price }}</div>
              <div class="qty-col">×{{ sku.quantity }}</div>
              <div class="subtotal-col">
                ¥{{ (sku.price * sku.quantity).toFixed(2) }}
              </div>
            </div>
          </div>

          <!-- 订单底部 -->
          <div class="order-foot">
            <div class="total">
              实付金额：<span class="price"
                >¥{{ item.payAmount || item.totalAmount }}</span
              >
            </div>
            <div class="btns">
              <el-button size="small" @click="toLogistics(item.id)">
                查看物流
              </el-button>
              <el-button
                size="small"
                @click="toDetail(item.id)"
                v-if="item.status !== 4"
              >
                查看详情
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
import { getOrderListService } from '@/api/user/order.js'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)     
const pageNum = ref(1)   
const pageSize = ref(10) 

// 查询条件
const queryParams = reactive({
  orderSn: '',
  status: null,       
  startTime: '',
  endTime: ''
})

// 日期范围
const dateRange = ref(null)

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderSn: queryParams.orderSn || null,
      status: queryParams.status,
      startTime: queryParams.startTime || null,
      endTime: queryParams.endTime || null
    }

    const res = await getOrderListService(params)
    const pageData = res.data || {}

    list.value = pageData.records || pageData.list || pageData.rows || []
    total.value = pageData.total || 0
  } catch (e) {
    console.error('加载订单列表失败', e)
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

// 日期选择处理
const handleDateChange = (val) => {
  if (val && val.length === 2) {
    queryParams.startTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
}

const resetQuery = () => {
  queryParams.orderSn = ''
  queryParams.status = null 
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = null
  pageNum.value = 1           
  getList()
}

// 订单状态文本
const getStatusText = (status) => {
  const map = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

// 状态标签颜色
const getStatusTagType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger'
  }
  return map[status] || 'info'
}

// 去详情
const toDetail = (id) => {
  router.push({ path: '/order/detail', query: { orderId: id } })
}

// 去物流
const toLogistics = (orderId) => {
  router.push({
    path: '/order/detail',
    query: { orderId, showLogistics: 'true' }
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
.order-list-page {
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

/* 搜索栏布局 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 22px;
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
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-item {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.order-head {
  padding: 12px 16px;
  background: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
  border-bottom: 1px solid #eee;
}
.goods-list {
  padding: 16px;
}
.goods-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.goods-item:last-child {
  margin-bottom: 0;
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
.price-col,
.qty-col,
.subtotal-col {
  width: 100px;
  text-align: center;
  font-size: 13px;
}
.subtotal-col {
  color: #f56c6c;
  font-weight: 500;
}
.order-foot {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
}
.price {
  color: #f56c6c;
  font-weight: 500;
}
.btns {
  display: flex;
  gap: 8px;
}
</style>