// 导入request.js请求工具
import request from '@/utils/request.js'

// 添加浏览记录
export const addBrowseService = (productId) => {
  return request.post('/product/browse/add', { productId });
};

// 删除单条浏览记录
export const removeBrowseService = (productId) => {
  return request.post('/product/browse/delete', { productId });
};

// 获取用户浏览列表
export const getBrowseListService = (data) => {
  return request.post('/product/browse/list', data);
};

// 清空所有浏览记录
export const clearBrowseService = () => {
  return request.post('/product/browse/clear');
};

// 批量删除足迹
export const batchDeleteBrowseService = (ids) => {
  return request.post('/product/browse/batchDelete', ids)
}