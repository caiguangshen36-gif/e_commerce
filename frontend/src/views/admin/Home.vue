<template>
  <div class="dashboard-container">

    <!-- 顶部核心指标 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="card-label">今日销售额</div>
        <div class="card-value">¥{{ formatNumber(todaySales) }}</div>
      </div>
      <div class="stat-card">
        <div class="card-label">今日新增用户</div>
        <div class="card-value">{{ todayNewUser }}</div>
      </div>
      <div class="stat-card">
        <div class="card-label">今日支付订单</div>
        <div class="card-value">{{ todayPaidOrder }}</div>
      </div>
      <div class="stat-card clickable" @click="goToWithQuery('/order', { today: true })">
        <div class="card-label">今日订单数</div>
        <div class="card-value">{{ todayOrderCount }}</div>
      </div>
      <div class="stat-card clickable" @click="goToWithQuery('/order', { status: 1 })">
        <div class="card-label">待发货订单</div>
        <div class="card-value">{{ undeliveredOrderCount }}</div>
      </div>
      <div class="stat-card clickable" @click="goToWithQuery('/after-sale', { status: 0 })">
        <div class="card-label">待审核售后</div>
        <div class="card-value">{{ pendingAfterSaleCount }}</div>
      </div>
      <div class="stat-card clickable" @click="goToWithQuery('/stock-warning', { stockWarning: true })">
        <div class="card-label">库存预警商品</div>
        <div class="card-value warn">{{ stockWarningCount }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="panel">
        <div class="panel-title">本周趋势</div>
        <div ref="trendChartRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">订单状态分布</div>
        <div ref="pieChartRef" class="chart"></div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="bottom-row">
      <div class="panel">
        <div class="panel-title">热销商品排行（销售额 TOP5）</div>
        <el-table :data="hotProductList" stripe style="width:100%">
          <el-table-column label="排名" align="center" width="60">
            <template #default="{ row }">
              <span :class="['rank', row.rank <= 3 ? 'rank-top' : '']">{{ row.rank }}</span>
            </template>
          </el-table-column>
          <el-table-column label="图片" align="center" width="70">
            <template #default="{ row }">
              <el-image
                :src="row.pic"
                fit="cover"
                :preview-src-list="[row.pic]"
                style="width:44px;height:44px;border-radius:4px;display:block;margin:0 auto"
              />
            </template>
          </el-table-column>
          <el-table-column label="商品名称" prop="productName" />
          <el-table-column label="销量" prop="totalQuantity" align="center" width="80" />
          <el-table-column label="销售额" align="center" width="100">
            <template #default="{ row }">¥{{ formatNumber(row.totalSales) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <div class="panel-title">快捷入口</div>
        <div class="quick-list">
          <el-button @click="goTo('/admin')">管理员管理</el-button>
          <el-button @click="goTo('/order')">订单管理</el-button>
          <el-button @click="goTo('/list')">商品管理</el-button>
          <el-button @click="goTo('/after-sale')">售后管理</el-button>
          <el-button @click="goTo('/logistics')">物流管理</el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

import { getOrderStatsService, getProductSalesTop5 } from '@/api/admin/order.js'
import { getNewUserStatsService } from '@/api/admin/admin.js'
import { getPendingAfterSaleListService } from '@/api/admin/afterSale.js'
import { getSkuStockWarningService } from '@/api/admin/productSku.js'

const router = useRouter()
const goTo = (path) => router.push({ path: `/admin${path}` })
const goToWithQuery = (path, query = {}) => router.push({ path: `/admin${path}`, query })

// === 响应式数据 ===
const todayOrderCount = ref(0)
const todaySales = ref(0)
const todayNewUser = ref(0)
const todayPaidOrder = ref(0)
const undeliveredOrderCount = ref(0)
const pendingAfterSaleCount = ref(0)
const stockWarningCount = ref(0)
const hotProductList = ref([])

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

//  优化金额格式化，增加亿级支持和统一小数位
const formatNumber = (num) => {
  const n = Number(num || 0)
  if (n >= 100000000) return (n / 100000000).toFixed(2) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(2) + 'w'
  return n.toFixed(2)
}

//  核心改造：直接使用后端聚合接口，不再拉取全量订单列表
const loadDashboardStats = async () => {
  try {
    // 并行请求所有独立的统计接口，大幅缩短加载时间
    const [statsRes, userRes, afterSaleRes, stockRes] = await Promise.all([
      getOrderStatsService(),
      getNewUserStatsService(),
      getPendingAfterSaleListService(),
      getSkuStockWarningService()
    ])

    // 解析订单聚合统计数据
    const stats = statsRes.data || {}
    todaySales.value = stats.todaySales || 0
    todayOrderCount.value = stats.todayOrderCount || 0
    todayPaidOrder.value = stats.todayPaidOrder || 0
    
    // 待发货订单从 statusDistribution 中获取（对应后端 status=1）
    const statusDist = stats.statusDistribution || {}
    undeliveredOrderCount.value = statusDist['待发货'] || 0

    // 解析其他独立接口数据
    todayNewUser.value = userRes.data?.todayCount || 0
    pendingAfterSaleCount.value = afterSaleRes.data?.length || 0
    stockWarningCount.value = stockRes.data?.length || 0

  } catch (err) {
    console.error('Dashboard 统计加载失败', err)
  }
}

// 热销排行
const loadHotProduct = async () => {
  try {
    const now = new Date()
    const endTime = now.toISOString().split('T')[0] + ' 23:59:59'
    const sevenDaysAgo = new Date(now.getTime() - 6 * 86400000)
    const startTime = sevenDaysAgo.toISOString().split('T')[0] + ' 00:00:00'
    const res = await getProductSalesTop5({ startTime, endTime })
    hotProductList.value = (res.data || []).map((it, i) => ({ ...it, rank: i + 1 }))
  } catch (err) {
    console.error('热销排行加载失败', err)
  }
}

//  趋势图：直接使用后端 weekTrend 数据，纯前端渲染
const initTrendChart = (weekTrend = []) => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'], bottom: 0 },
    grid: { top: 16, right: 48, bottom: 36, left: 48 },
    xAxis: {
      data: weekTrend.map(x => x.date?.slice(5)),
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisTick: { show: false }
    },
    yAxis: [
      { name: '订单数', nameTextStyle: { color: '#909399', fontSize: 11 }, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
      { name: '销售额', nameTextStyle: { color: '#909399', fontSize: 11 }, axisLine: { show: false }, splitLine: { show: false } }
    ],
    series: [
      {
        name: '订单数', type: 'line', smooth: true, symbolSize: 6,
        lineStyle: { width: 2, color: '#409eff' }, itemStyle: { color: '#409eff' },
        data: weekTrend.map(x => x.orderCount)
      },
      {
        name: '销售额', type: 'line', smooth: true, yAxisIndex: 1, symbolSize: 6,
        lineStyle: { width: 2, color: '#67c23a' }, itemStyle: { color: '#67c23a' },
        data: weekTrend.map(x => x.sales)
      }
    ]
  })
}

//  饼图：直接使用后端 statusDistribution 数据，纯前端渲染
const initPieChart = (statusDistribution = {}) => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)

  const colors = ['#909399', '#409eff', '#e6a23c', '#67c23a', '#f56c6c']
  const data = Object.entries(statusDistribution).map(([name, value]) => ({ name, value }))

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8 },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['38%', '62%'],
      center: ['50%', '45%'],
      itemStyle: { borderWidth: 2, borderColor: '#fff' },
      label: { show: false },
      data
    }]
  })
}

const resize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

//  生命周期：先并行加载数据，再初始化图表
onMounted(async () => {
  window.addEventListener('resize', resize)

  try {
    // 先获取聚合统计数据（包含图表所需数据）
    const statsRes = await getOrderStatsService()
    const stats = statsRes.data || {}

    // 同时触发其他不依赖 stats 的请求
    const [, userRes, afterSaleRes, stockRes] = await Promise.all([
      Promise.resolve(statsRes), 
      getNewUserStatsService(),
      getPendingAfterSaleListService(),
      getSkuStockWarningService()
    ])

    // 填充卡片数据
    todaySales.value = stats.todaySales || 0
    todayOrderCount.value = stats.todayOrderCount || 0
    todayPaidOrder.value = stats.todayPaidOrder || 0
    undeliveredOrderCount.value = (stats.statusDistribution || {})['待发货'] || 0
    todayNewUser.value = userRes.data?.todayCount || 0
    pendingAfterSaleCount.value = afterSaleRes.data?.length || 0
    stockWarningCount.value = stockRes.data?.length || 0

    // 使用后端返回的数据直接渲染图表
    initTrendChart(stats.weekTrend || [])
    initPieChart(stats.statusDistribution || {})

    // 热销排行独立加载
    loadHotProduct()

  } catch (err) {
    console.error('Dashboard 初始化失败', err)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f6fa;
  min-height: 100%;
}

/* 指标卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px 14px;
}

.stat-card.clickable {
  cursor: pointer;
  transition: border-color 0.2s;
}

.stat-card.clickable:hover {
  border-color: #409eff;
}

.card-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.card-value.warn {
  color: #e6a23c;
}

/* 通用面板 */
.panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px 20px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 图表行 */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.chart {
  height: 280px;
}

/* 底部 */
.bottom-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

/* 排名 */
.rank {
  font-size: 13px;
  color: #909399;
}
.rank.rank-top {
  color: #f56c6c;
  font-weight: 600;
}

/* 快捷入口 */
.quick-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-list .el-button {
  width: 100%;
  justify-content: center;
}
</style>