//导入request.js请求工具
import request from '@/utils/request.js'

// 获取轮播图列表
export const getMartketingListService = () => {
  return request.post('/marketing/carousel/list');
}

// 添加轮播图
export const addMarketingService = (marketingData) => {
  return request.post('/marketing/carousel', marketingData);
}

// 更新轮播图
export const updateMarketingService = (marketingData) => {
  return request.post('/marketing/carousel/update', marketingData);
}

// 删除轮播图
export const deleteMarketingService = (id) => {
  return request.post('/marketing/carousel/delete', { id });
}

// 获取轮播图详情
export const getMarketingDetailService = (id) => {
  return request.post('/marketing/carousel/detail', { id });
}

// 获取【启用】轮播图列表
export const enableMarketingListService = () => {
  return request.post('/marketing/carousel/enabled');
}