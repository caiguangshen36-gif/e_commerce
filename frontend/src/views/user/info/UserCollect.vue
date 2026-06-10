<template>
  <div class="collect-page">
    <div class="container">
      <div class="collect-card">

        <!-- 搜索栏 -->
        <div class="search-bar">
          <div class="form-item">
            <label>商品名称</label>
            <el-input
              v-model="queryParams.productName"
              placeholder="请输入商品名称"
              clearable
              style="width: 240px"
              @keyup.enter="handleSearch"
            />
          </div>

          <div class="btn-group">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>

        <!-- 头部 -->
        <div class="card-header">
          <h3>我的收藏</h3>
          <div class="header-btns">
            <el-button
              v-if="selectedIds.length > 0"
              type="danger"
              size="small"
              @click="batchDelete"
            >
              批量取消收藏({{ selectedIds.length }})
            </el-button>
            <el-button type="text" size="small" @click="clearAll">
              <el-icon style="margin-right: 4px"><Delete /></el-icon>清空收藏
            </el-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="list.length === 0 && !loading" class="empty-box">
          <el-empty description="暂无收藏商品" />
        </div>

        <template v-else>
          <!-- 表头 -->
          <div class="list-header">
            <span class="col-check">
              <el-checkbox v-model="checkAll" @change="handleCheckAll" />
            </span>
            <span class="col-goods">商品</span>
            <span class="col-price">价格</span>
            <span class="col-time">收藏时间</span>
          </div>

          <div class="goods-list" v-loading="loading">
            <div
              class="goods-item"
              v-for="item in list"
              :key="item.collectId"
              @click="toDetail(item.productId)"
            >
              <!-- 勾选框 阻止冒泡 -->
              <div class="col-check" @click.stop>
                <el-checkbox v-model="item.checked" @change="onItemCheck" />
              </div>

              <!-- 商品信息 -->
              <div class="col-goods goods-info">
                <img :src="item.pic" :alt="item.productName" class="goods-img" />
                <div class="goods-text">
                  <p class="goods-name">{{ item.productName }}</p>
                </div>
              </div>

              <!-- 价格 -->
              <div class="col-price">
                <span class="price-sym">¥</span>
                <span class="price-val">
                  {{ item.skuList?.length > 0 ? item.skuList[0].price.toFixed(2) : '0.00' }}
                </span>
              </div>

              <!-- 收藏时间 -->
              <div class="col-time cell-muted">
                {{ formatTime(item.createTime) }}
              </div>
            </div>
          </div>

          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[8, 16, 32]"
            layout="total, sizes, prev, pager, next, jumper"
            class="pagination"
            background
            @size-change="getList"
            @current-change="getList"
          />
        </template>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getCollectListService,
  clearCollectService,
  batchRemoveCollectService
} from '@/api/user/collect.js'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 查询条件
const queryParams = ref({
  productName: ''
})

// 勾选状态
const checkAll = ref(false)
const selectedIds = ref([])
const formatTime = (time) => time ? String(time).replace('T', ' ') : ''

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      productName: queryParams.value.productName || null
    }

    const res = await getCollectListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      const records = pageData.records || pageData.list || pageData.rows || []
      
      list.value = records.map(item => ({ ...item, checked: false }))
      total.value = pageData.total || 0
      
      checkAll.value = false
      updateSelected()
    }
  } catch (e) {
    console.error('加载收藏列表失败', e)
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
  queryParams.value = { productName: '' }
  pageNum.value = 1
  getList()
}


// 全选/取消全选
const handleCheckAll = (val) => {
  list.value.forEach(item => item.checked = val)
  updateSelected()
}

// 单个勾选改变时
const onItemCheck = () => {
  updateSelected()
  const allChecked = list.value.length > 0 && list.value.every(item => item.checked)
  checkAll.value = allChecked
}

// 更新已选中ID列表
const updateSelected = () => {
  selectedIds.value = list.value
    .filter(item => item.checked)
    .map(item => item.collectId)
}

// 批量取消收藏
const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    return ElMessage.warning('请选择要取消收藏的商品')
  }
  await ElMessageBox.confirm('确定取消选中的收藏？', '提示', { type: 'warning' })
  try {
    const res = await batchRemoveCollectService(selectedIds.value)
    if (res.code === 200) {
      ElMessage.success('批量取消成功')
      getList()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 清空收藏
const clearAll = async () => {
  await ElMessageBox.confirm('确定要清空所有收藏吗？', '提示', { type: 'warning' })
  try {
    const res = await clearCollectService()
    if (res.code === 200) {
      ElMessage.success('清空成功')
      list.value = []
      total.value = 0
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 跳商品详情
const toDetail = (id) => {
  router.push({ path: '/product/detail', query: { id } })
}

onMounted(() => getList())
</script>

<style scoped>
/* 整体页面样式 */
.collect-page {
  width: 100%;
}
.container {
  width: 100%;
}
.collect-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

/* 搜索栏*/
.search-bar {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
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

/* 头部 */
.card-header {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
}
.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.header-btns {
  display: flex;
  gap: 10px;
}

/* 列表头部 */
.list-header {
  display: flex;
  padding: 12px 20px;
  font-size: 14px;
  color: #666;
  background: #fafafa;
  border-bottom: 1px solid #f5f5f5;
}
.col-check {
  width: 60px;
  display: flex;
  align-items: center;
}
.col-goods {
  flex: 1;
}
.col-price {
  width: 140px;
  text-align: center;
}
.col-time {
  width: 200px;
  text-align: center;
}

/* 商品行 */
.goods-list {
  padding: 0;
}
.goods-item {
  display: flex;
  padding: 14px 20px;
  align-items: center;
  border-bottom: 1px solid #f8f8f8;
  cursor: pointer;
  transition: background 0.2s;
}
.goods-item:hover {
  background: #fafbfc;
}

/* 商品信息 */
.goods-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.goods-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
  border: 1px solid #f0f0f0;
}
.goods-name {
  font-size: 14px;
  color: #333;
  margin: 0;
  line-height: 1.4;
}

/* 价格 */
.price-sym {
  font-size: 12px;
  color: #f56c6c;
}
.price-val {
  font-size: 14px;
  font-weight: 500;
  color: #f56c6c;
}

/* 时间 */
.cell-muted {
  font-size: 13px;
  color: #999;
}

/* 分页 */
.pagination {
  padding: 16px 20px;
  text-align: right;
}

/* 空状态 */
.empty-box {
  padding: 80px 0;
  text-align: center;
}
</style>