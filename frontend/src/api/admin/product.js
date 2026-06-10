import adminRequest from "../../utils/adminRequest.js";

// 获取商品列表
export const getProductListService = (queryParams) => {
  return adminRequest.post('/product/list', queryParams);
}

// 添加商品
export const addProductService = (productData) => {
  return adminRequest.post('/product/add', productData);
}

// 更新商品
export const updateProductService = (productData) => {
  return adminRequest.post('/product/update', productData);
}

// 删除商品
export const deleteProductService = (id) => {
  return adminRequest.post('/product/delete', { id });
}

//更新商品状态
export const updateProductStatusService = (id, status) => {
  return adminRequest.post('/product/updateStatus', { id, status });
}

//查询商品详情
export const getProductDetailService = (id) => {
  return adminRequest.post('/product/detail', { id });
}

//根据分类ID获取商品列表
export const getProductListByCategoryIdService = (categoryId) => {
  return adminRequest.post('/product/listByCategory', { categoryId });
}

//获取热门商品
export const getHotProductListService = () => {
  return adminRequest.get('/product/hotList')
}

//设置热门商品
export const setHotProductService = (parms) => {
  return adminRequest.post('/product/updateHotStatus', parms);
}


// 商品搜索
export const searchProductService = (keyword) => {
  return adminRequest.get('/product/search', {
    params: { keyword }
  })
}