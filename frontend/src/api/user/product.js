import request from "../../utils/request.js";

// 获取商品列表
export const getProductListService = (queryParams) => {
  return request.post('/product/list', queryParams);
}

//查询商品详情
export const getProductDetailService = (id) => {
  return request.post('/product/detail', { id });
}

//根据分类ID获取商品列表
export const getProductListByCategoryIdService = (categoryId) => {
  return request.post('/product/listByCategory', { categoryId });
}

// 根据商品ID获取SKU列表
export const getSkuListByProductIdService = (productId) => {
  return request.post('/product/sku/list', { productId })
}

// 获取SKU详情
export const getSkuDetailService = (id) => {
  return request.post('/product/sku/detail', { id })
}

//下单减少库存
export const reduceStockService = (orderId) => {
  return request.post('/product/sku/updateStocks', null, {
    params: {
      orderId: orderId
    }
  })
}

// 正确：回滚接口同理
export const rollbackStockService = (orderId) => {
  return request.post('/product/sku/rollbackStocks', null, {
    params: {
      orderId: orderId
    }
  })
}

// 获取所有属性列表
export const getAttributeListService = () => {
  return request.post('/product/attribute/list')
}

// 根据分类ID获取属性列表
export const listByCategoryService = (categoryId) => {
  return request.post('/product/attribute/listByCategory', { categoryId })
}

// 获取属性详情
export const getAttributeDetailService = (id) => {
  return request.post('/product/attribute/detail', { id })
}

//获取热门商品
export const getHotProductListService = () => {
  return request.get('/product/hotList')
}

//猜你感兴趣商品
export const getRecommendProductListService = () => {
  return request.get('/product/recommend')
}

// 商品搜索
export const searchProductService = (keyword) => {
  return request.get('/product/search', {
    params: { keyword }
  })
}