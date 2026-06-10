<template>
  <div class="admin-layout">
    <!-- 左侧侧边栏 -->
    <el-aside width="220px" class="aside-wrap">
      <!-- LOGO -->
      <div class="logo">电商管理后台</div>

      <!-- 菜单 -->
      <el-menu
        router
        :default-active="$route.path"
        @select = "handleSelect"
        class="menu"
        background-color="#fff"
        text-color="#333"
        active-text-color="#409EFF"
      >
        <!-- 使用递归组件渲染菜单 -->
        <template v-for="item in menuStore.menus" :key="item.id">
          <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
            <template #title>
              <!-- 动态渲染图标 -->
              <el-icon v-if="item.icon">
                <component :is="iconMap[item.icon]" />
              </el-icon>
              <span>{{ item.title || item.menuName }}</span>
            </template>

            <!-- 递归渲染子菜单 -->
            <template v-for="child in item.children" :key="child.id">
              <el-menu-item :index="child.path">
                <el-icon v-if="child.icon">
                  <component :is="iconMap[child.icon]" />
                </el-icon>
                <span>{{ child.title || child.menuName }}</span>
              </el-menu-item>
            </template>
          </el-sub-menu>

          <!--没有子菜单 -->
          <el-menu-item v-else :index="item.path">
            <el-icon v-if="item.icon">
              <component :is="iconMap[item.icon]" />
            </el-icon>
            <span>{{ item.title || item.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <div class="main-wrap">
      <!-- 顶部栏 -->
      <div class="header">
        <div class="right">
          <span>管理员</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </div>

      <!-- 面包屑导航 -->
      <div class="breadcrumb-box">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-for="(item, index) in $route.matched" :key="index">
            {{ item.meta.title || '未设置标题' }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useMenuStore } from '@/stores/menu.js'
import { useTokenStore } from '@/stores/token'
import { getAdminUnreadCountService } from '@/api/admin/notice.js'

import { 
  House, Message, Document, Van, GoodsFilled, 
  User, UserFilled, Lock, Clock, Picture, Box,
  Menu, Setting, Edit, Collection, Operation 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const menuStore = useMenuStore() 

// 图标映射表
const iconMap = {
  House,
  Message,
  Document,
  Van,
  Box,
  GoodsFilled,
  User,         
  UserFilled,
  Lock,
  Clock,
  Picture,
  Menu,
  Setting,
  Edit,         
  Collection,   
  Operation     
}

// 未读消息数量
const unreadCount = ref(0)

// 获取未读消息数量
const getUnreadCount = async () => {
  try {
    const res = await getAdminUnreadCountService()
    unreadCount.value = res.data?.unreadCount || 0
  } catch (e) {
    console.error('获取未读消息数量失败', e)
    unreadCount.value = 0
  }
}

// 退出登录
const logout = () => {
  tokenStore.removeToken()
  menuStore.clearMenus() 
  ElMessage.success('退出成功')
  router.push('/admin/login')
}

// 挂载时加载
onMounted(() => {
  getUnreadCount()
})

watch(
  () => route.fullPath,
  (newPath) => {
    if (newPath.startsWith('/admin/notice')) {
      getUnreadCount()
    }
  },
  { immediate: true }
)

// 防抖函数
const debounce = (fn, delay) => {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 处理菜单选择
const handleSelect = debounce((key) => {
  console.log('跳转至:', key)
}, 500) 
</script>

<style scoped>
.admin-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.aside-wrap {
  background: #fff;
  height: 100vh;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 10;
}

.logo {
  height: 60px;
  line-height: 60px;
  color: #121111;
  font-size: 16px;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #eee;
  background: #fff;
}

.menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}

.breadcrumb-box {
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.main-content {
  flex: 1;
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>