//导入request.js请求工具
import request from '@/utils/request.js'


//获取分类列表
export const getCategoryListService = () => {
  return request.post('/product/category/list');
}


// 添加分类
export const addCategoryService = (categoryData) => {
  return request.post('/product/category/add', categoryData);
}

// 更新分类
export const updateCategoryService = (categoryData) => {
  return request.post('/product/category/update', categoryData);
}

// 删除分类
export const deleteCategoryService = (id) => {
  return request.post('/product/category/delete', { id });
}

// 更新分类状态
export const updateCategoryStatusService = (id, status) => {
  return request.post('/product/category/updateStatus', { id, status });
}

// 根据父类ID获取子类列表
export const getSubCategoryListService = (parentId) => {
  return request.post('/product/category/listByParentId', { parentId });
}