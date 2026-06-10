<template>
  <div class="user-page">
    <div class="container">
      <div class="user-layout">

        <!-- 左侧菜单 -->
        <el-aside width="220px" class="user-aside">
          <el-menu
            :default-active="$route.path"
            :active-text-color="'#ab5026'"
            mode="vertical"
            router
          >
            <el-menu-item index="/userinfo/info">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>

            <el-menu-item index="/userinfo/notice">
              <el-icon><Message /></el-icon>
              <span>消息中心</span>
            </el-menu-item>

            <el-menu-item index="/userinfo/order">
              <el-icon><ShoppingBag /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            
            <el-menu-item index="/userinfo/collect">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>

            <el-menu-item index="/userinfo/foot">
              <el-icon><Clock /></el-icon>
              <span>我的足迹</span>
            </el-menu-item>

            <el-menu-item index="/userinfo/afterSale">
              <el-icon><Service /></el-icon>
              <span>我的售后</span>
            </el-menu-item>

            <el-menu-item index="/userinfo/address">
              <el-icon><MapLocation /></el-icon>
              <span>我的地址</span>
            </el-menu-item>

            <el-menu-item index="/userinfo/updatePassword">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 右侧子路由页面 -->
        <el-main class="user-main">
          <router-view />
        </el-main>

      </div>
    </div>
  </div>
</template>

<script setup>
import {
  User,
  Lock,
  ShoppingBag,
  Star,
  Clock,
  MapLocation,
  Service,
  Message
} from '@element-plus/icons-vue'
</script>

<style scoped>
.user-page {
  padding: 30px 0;
  min-height: calc(100vh - 200px);
}

.user-layout {
  display: flex;
  min-height: calc(100vh - 200px);
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

/* 左侧菜单 */
.user-aside {
  background: #fafafa;
  border-right: 1px solid #eee;
  flex-shrink: 0;
}

.user-aside .el-menu {
  border-right: none;
  background: transparent;
}

.user-aside .el-menu-item {
  height: 52px;
  line-height: 52px;
  font-size: 14px;
  color: #444;
  border-radius: 0;
}

.user-aside .el-menu-item:hover {
  background: #f5ede8;
  color: #ab5026;
}

.user-aside .el-menu-item.is-active {
  color: #ab5026;
  background: #fdf0ea;
  font-weight: 600;
  border-right: 3px solid #ab5026;
}

/* 右侧内容 */
.user-main {
  flex: 1;
  padding: 24px 24px;
  background: #f9f9f9;
  min-width: 0;
}

</style>