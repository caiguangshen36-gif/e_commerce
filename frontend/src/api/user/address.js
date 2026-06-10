//导入request.js请求工具
import request from '@/utils/request.js'


//提供调用获取用户ID地址接口的函数
export const getUserAddressService = () => {
  return request.post('/ums/address/List');
};

//提供调用添加用户地址接口的函数
export const addUserAddressService = (addressData) => {
  return request.post('/ums/address/add', addressData);
}

//提供调用更新用户地址接口的函数
export const updateUserAddressService = (addressData) => {
  return request.post('/ums/address/update', addressData);
}

//提供调用删除用户地址接口的函数
export const deleteUserAddressService = (id) => {
  return request.post('/ums/address/delete', { id: id });
};

//提供调用设置默认地址接口的函数
export const setDefaultAddressService = (addressId) => {
  return request.post('/ums/address/default', { id: addressId });
}