import adminRequest from '@/utils/adminRequest.js'

// 1. 获取角色列表
export const getRoleListService = () => {
  return adminRequest.post('/sys/role/list')
}

// 2. 新增角色
export const addRoleService = (roleData) => {
  return adminRequest.post('/sys/role/add', roleData)
}

// 3. 修改角色
export const updateRoleService = (roleData) => {
  return adminRequest.post('/sys/role/update', roleData)
}

// 4. 删除角色 
export const deleteRoleService = (id) => {
  return adminRequest.post('/sys/role/delete', { id: id })
}

// 5. 根据用户ID获取角色列表
export const getRolesByUserIdService = (data) => {
  return adminRequest.post('/sys/role/user/roleList', data)
}

// 6. 给用户分配角色
export const assignRoleService = (userId, roleId) => {
  return adminRequest.post('/sys/role/user/assign', { userId, roleId })
}

// 7. 移除用户角色
export const removeUserRoleService = (userId, roleId) => {
  return adminRequest.post('/sys/role/user/remove', { userId, roleId })
}