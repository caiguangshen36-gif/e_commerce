import { createRouter, createWebHistory } from 'vue-router'
import { userRoutes } from './user'
import { adminRoutes } from './admin'
import { useTokenStore } from '@/stores/token'
import { useMenuStore } from '@/stores/menu.js'

const routes = [
  ...userRoutes,
  ...adminRoutes
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const tokenStore = useTokenStore()
  const menuStore = useMenuStore()

  // 1. 区分前后台：判断目标路由是否属于后台
  const isAdminRoute = to.path.startsWith('/admin')

  // 2. 如果是前台路由（如 /login, /home 等），直接放行
  if (!isAdminRoute) {
    return next()
  }

  // 3. 后台路由的拦截逻辑

  // 3.1 如果是去后台登录页，直接放行
  if (to.path === '/admin/login') {
    return next()
  }

  // 3.2 检查是否有 Admin Token
  if (!tokenStore.adminToken) {
    // 如果没有后台 Token，强制去后台登录
    return next('/admin/login')
  }

  // 3.3 有 Token，但菜单为空（说明是刷新页面），需要重新获取
  if (menuStore.menus.length === 0) {
    try {
      await menuStore.fetchMenus()
      return next({ ...to, replace: true })
    } catch (err) {
      console.error('获取菜单失败，可能是Token过期', err)
      // 如果获取菜单失败（比如 token 过期或接口挂了），强制去登录
      return next('/admin/login')
    }
  }

  // 4. 正常放行
  next()
})

export default router