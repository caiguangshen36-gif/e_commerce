//导入request.js请求工具
import request from '@/utils/request.js'

// 获取用户收藏列表
export const getCollectListService = (data) => {
  return request.post('/product/collect/list', data);
};


// 添加收藏
export const addCollectService = (productId) => {
  return request.post('/product/collect/add', { productId });
};

// 取消收藏
export const removeCollectService = (productId) => {
  return request.post('/product/collect/remove', { productId });
};

// 检查是否已收藏
export const isCollectedService = (productId) => {
  return request.post('/product/collect/isCollected', { productId });
};

// 清空收藏
export const clearCollectService = () => {
  return request.post('/product/collect/clear');
};

//批量取消收藏
export const batchRemoveCollectService = (ids) => {
  return request.post('/product/collect/batchRemove', ids)
}