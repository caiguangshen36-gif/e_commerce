//导入request.js请求工具
import adminRequest from '@/utils/adminRequest.js'

// 管理员登录
export const adminLoginService = (data) => {
  return adminRequest.post('/sys/user/login', data);
}

// 新增管理员
export const addAdminService = (userData) => {
  return adminRequest.post('/sys/user/add', userData);
}

// 管理员列表
export const getAdminListService = () => {
  return adminRequest.post('/sys/user/list');
}

// 获取当前管理员信息
export const getAdminInfoService = () => {
  return adminRequest.post('/sys/user/info');
}

// 修改管理员状态
export const updateAdminStatusService = (id, status) => {
  return adminRequest.post('/sys/user/updateStatus', { id, status });
}

// 修改管理员信息
export const updateAdminService = (data) => {
  return adminRequest.post('/sys/user/update', data)
}

// 修改密码
export const updatePasswordService = (oldPassword, newPassword, repPassword) => {
  return adminRequest.post("/sys/user/updatePassword", {
    oldPassword: oldPassword,
    newPassword: newPassword,
    repPassword: repPassword
  });
};

//获取新增用户统计数据
export const getNewUserStatsService = () => {
  return adminRequest.get('/ums/user/admin/registerStats')
}