<template>
  <div class="category-page">
    <!-- 左侧一级分类 -->
    <aside class="sidebar">
      <div class="sidebar-inner">
        <p class="sidebar-label">全部分类</p>
        <ul class="level1-list">
          <li
            v-for="cat in level1List"
            :key="cat.id"
            class="level1-item"
            :class="{ active: activeId === cat.id }"
            @click="selectCategory(cat)"
          >
            <span class="level1-icon" v-if="cat.icon">
              <img :src="cat.icon" alt="" />
            </span>
            <!-- ✅ 用 Element Plus 图标替代手写 SVG -->
            <span class="level1-icon placeholder" v-else>
              <el-icon :size="16" color="currentColor">
                <Goods />
              </el-icon>
            </span>
            <span class="level1-name">{{ cat.categoryName }}</span>
            <span class="level1-arrow">
              <el-icon :size="12" color="currentColor">
                <ArrowRight />
              </el-icon>
            </span>
          </li>
        </ul>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="content" v-if="activeCategory">
      <!-- 当前分类标题 -->
      <div class="content-header">
        <h2 class="content-title">{{ activeCategory.categoryName }}</h2>
        <span class="content-count">{{ displayList.length }} 个子分类</span>
      </div>

      <!-- 加载中 -->
      <div class="loading-wrap" v-if="loading">
        <div class="skeleton-grid">
          <div class="skeleton-card" v-for="n in 8" :key="n"></div>
        </div>
      </div>

      <!-- 二级/三级分类网格 -->
      <template v-else>
        <div class="level2-grid" v-if="displayList.length">
          <div
            class="level2-card"
            v-for="sub in displayList"
            :key="sub.id"
            @click="goToProducts(sub)"
          >
            <div class="card-icon-wrap">
              <img v-if="sub.icon" :src="sub.icon" class="card-icon-img" alt="" />
              <div class="card-icon-placeholder" v-else>
                <el-icon :size="22" color="currentColor">
                  <Goods />
                </el-icon>
              </div>
            </div>
            <span class="card-name">{{ sub.categoryName }}</span>
            <span class="card-arrow">
              <el-icon :size="11" color="currentColor">
                <ArrowRight />
              </el-icon>
            </span>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
          <el-icon :size="36" color="#ddd">
            <Warning />
          </el-icon>
          <p>暂无子分类</p>
        </div>
      </template>
    </main>

    <!-- 初始空状态 -->
    <main class="content content-empty" v-else>
      <p class="hint">← 选择左侧分类</p>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElIcon } from 'element-plus'
import { Goods, ArrowRight, Warning } from '@element-plus/icons-vue'
import { getCategoryListService } from '@/api/user/category.js'

const router = useRouter()

const loading = ref(false)
const allCategories = ref([])
const activeId = ref(null)

onMounted(getCategories)

async function getCategories() {
  loading.value = true
  try {
    const res = await getCategoryListService()
    allCategories.value = res.data || []
    console.log('分类数据:', res)
    if (level1List.value.length > 0) {
      selectCategory(level1List.value[0])
    }
  } catch {
    ElMessage.error('获取分类失败')
  } finally {
    loading.value = false
  }
}

const level1List = computed(() =>
  allCategories.value.filter(c => c.level === 1 && c.status === 1)
    .sort((a, b) => a.sort - b.sort)
)

const activeCategory = computed(() =>
  allCategories.value.find(c => c.id === activeId.value) || null
)

const level2List = computed(() => {
  if (!activeCategory.value) return []
  const parentSort = activeCategory.value.sort
  return allCategories.value
    .filter(c => Number(c.parentId) === parentSort && c.status === 1)
    .sort((a, b) => a.sort - b.sort)
})

const displayList = computed(() => {
  const level2 = level2List.value
  const list = []

  level2.forEach(level2Cat => {
    const level3 = allCategories.value.filter(c =>
      Number(c.parentId) === Number(level2Cat.id) && c.status === 1
    )
    if (level3.length > 0) {
      list.push(...level3)
    } else {
      list.push(level2Cat)
    }
  })

  return list.sort((a, b) => a.sort - b.sort)
})

function selectCategory(cat) {
  activeId.value = cat.id
}

function goToProducts(sub) {
  router.push({ path: '/categoryList', query: { categoryId: sub.id } })
}
</script>

<style scoped>
.category-page {
  display: flex;
  min-height: calc(100vh - 70px);
  background: #f5f5f3;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-right: 0.5px solid #ebebeb;
}
.sidebar-inner {
  padding: 24px 0;
  position: sticky;
  top: 70px;
}
.sidebar-label {
  font-size: 10px;
  font-weight: 400;
  color: #bbb;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0 20px;
  margin: 0 0 10px;
}

.level1-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.level1-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 20px;
  cursor: pointer;
  position: relative;
  transition: background 0.12s;
  border-left: 2px solid transparent;
}
.level1-item:hover {
  background: #f9f9f7;
}
.level1-item.active {
  background: #f5f5f3;
  border-left-color: #1a1a1a;
}
.level1-item.active .level1-name {
  color: #1a1a1a;
  font-weight: 500;
}
.level1-item.active .level1-icon {
  color: #1a1a1a;
}

.level1-icon {
  width: 18px;
  height: 18px;
  color: #bbb;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.level1-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.level1-name {
  flex: 1;
  font-size: 13px;
  color: #666;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.level1-arrow {
  color: #d8d8d8;
  flex-shrink: 0;
  transition: color 0.12s;
}
.level1-item.active .level1-arrow { color: #aaa; }

.content {
  flex: 1;
  padding: 32px 36px;
  min-width: 0;
}
.content-empty {
  display: flex;
  align-items: center;
  justify-content: center;
}
.hint {
  font-size: 13px;
  color: #ccc;
}

.content-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 0.5px solid #ebebeb;
}
.content-title {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0;
}
.content-count {
  font-size: 12px;
  color: #bbb;
}

.level2-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(148px, 1fr));
  gap: 12px;
}

.level2-card {
  background: #fff;
  border: 0.5px solid #ebebeb;
  border-radius: 10px;
  padding: 20px 16px 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
  position: relative;
}
.level2-card:hover {
  border-color: #d0d0d0;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.card-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f5f5f3;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-icon-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.card-icon-placeholder {
  color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  line-height: 1.3;
  flex: 1;
}

.card-arrow {
  position: absolute;
  bottom: 14px;
  right: 14px;
  color: #ccc;
  transition: color 0.15s, transform 0.15s;
}
.level2-card:hover .card-arrow {
  color: #888;
  transform: translateX(2px);
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(148px, 1fr));
  gap: 12px;
}
.skeleton-card {
  height: 110px;
  border-radius: 10px;
  background: linear-gradient(90deg, #f0f0ee 25%, #e8e8e6 50%, #f0f0ee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 80px 0;
  color: #bbb;
  font-size: 13px;
}

.loading-wrap { width: 100%; }
</style>