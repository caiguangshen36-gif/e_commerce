import adminRequest from '@/utils/adminRequest.js'

//获取菜单树形列表
export const getMenuListService = () => {
  return adminRequest.post('/sys/menu/list')
}

//新增菜单
export const addMenuService = (menuData) => {
  return adminRequest.post('/sys/menu/add', menuData)
}

//修改菜单
export const updateMenuService = (menuData) => {
  return adminRequest.post('/sys/menu/update', menuData)
}

//删除菜单
export const deleteMenuService = (id) => {
  return adminRequest.post('/sys/menu/delete', id, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// ====================== 角色-菜单权限 ======================
// 5. 给角色分配菜单权限
export const assignMenusToRoleService = (roleId, menuIds) => {
  return adminRequest.post('/sys/menu/role/assign', { roleId, menuIds })
}

// 6. 获取角色已分配的菜单ID列表
export const getRoleMenuIdsService = (roleId) => {
  return adminRequest.post('/sys/menu/role/menus', roleId)
}

// ====================== 用户菜单 ======================
// 7. 获取当前登录用户的菜单（侧边栏用）
export const getCurrentUserMenusService = () => {
  return adminRequest.post('/sys/menu/current')
}

// 8. 根据用户ID获取菜单
export const getMenusByUserIdService = (userId) => {
  return adminRequest.post('/sys/menu/user/list', userId)
}