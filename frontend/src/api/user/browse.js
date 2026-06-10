//导入request.js请求工具
import request from '@/utils/request.js'

// 获取足迹列表
export const getBrowseListService = () => {
  return request.post('/product/browse/list');
};

// 添加足迹
export const addBrowseService = (productId) => {
  return request.post('/product/browse/add', { productId });
};

// 取消足迹
export const deleteBrowseService = (productId) => {
  return request.post('/product/browse/delete', { productId });
};

// 清空足迹
export const clearBrowseService = () => {
  return request.post('/product/browse/clear');
};