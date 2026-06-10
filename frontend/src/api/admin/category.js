import adminRequest from "../../utils/adminRequest.js";

//获取分类列表（条件）
export function getCategoryListService(params) {
  return adminRequest.post('/product/category/admin/list', params)
}

//获取分类列表（全部）
export function getAllCategoryListService() {
  return adminRequest.post('/product/category/list')
}


// 添加分类
export const addCategoryService = (categoryData) => {
  return adminRequest.post('/product/category/add', categoryData);
}

// 更新分类
export const updateCategoryService = (categoryData) => {
  return adminRequest.post('/product/category/update', categoryData);
}

// 删除分类
export const deleteCategoryService = (id) => {
  return adminRequest.post('/product/category/delete', { id });
}

// 更新分类状态
export const updateCategoryStatusService = (id, status) => {
  return adminRequest.post('/product/category/updateStatus', { id, status });
}

// 根据父类ID获取子类列表
export const getSubCategoryListService = (parentId) => {
  return adminRequest.post('/product/category/listByParentId', { parentId });
}