//导入request.js请求工具
import request from '@/utils/request.js'

//提供调用注册接口的函数
export const userRegisterService = (registerData) => {
  return request.post('/ums/user/register', registerData);
}

//提供调用登录接口的函数
export const userLoginService = (loginData) => {
  return request.post('/ums/user/login', loginData);
}

//提供调用获取用户信息接口的函数
export const getUserInfoService = () => {
  return request.post('/ums/user/getInfo');
}

//提供调用更新用户信息接口的函数
export const updateUserInfoService = (userInfo) => {
  return request.post('/ums/user/update', userInfo);
}

//提供调用更新用户余额接口的函数
export const updateUserBalanceService = (balanceData) => {
  return request.post('/ums/user/updateBalance', balanceData);
}

//提供调用更新用户密码接口的函数
export const updateUserPasswordService = (passwordData) => {
  return request.post('/ums/user/updatePassword', passwordData);
}

//提供调用上传头像接口的函数
export const uploadAvatarService = (avatarData) => {
  return request.post('/upload', avatarData);
}

