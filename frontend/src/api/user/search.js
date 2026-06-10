import request from '@/utils/request.js'

// 获取用户搜索历史
export const getSearchHistoryService = () => {
  return request.get('/user/search/history');
}

// 保存搜索历史
export const addSearchHistoryService = (keyword) => {
  return request.post('/user/search/save', { keyword });
}

// 删除单条搜索历史
export const deleteSearchHistoryService = (keyword) => {
  return request.post('/user/search/delete', { keyword });
}

// 清空所有搜索历史
export const clearSearchHistoryService = () => {
  return request.post('/user/search/clear');
}