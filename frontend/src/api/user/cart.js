//导入request.js请求工具
import request from '@/utils/request.js'

// 获取购物车列表
export const getCartListService = () => {
  return request.get('/cart/list');
}

// 添加商品到购物车
export const addToCartService = (cartData) => {
  return request.post('/cart/add', cartData);
}

// 更新购物车中商品数量
export const updateCartItemService = (cartItemData) => {
  return request.post('/cart/update', cartItemData);
}

// 从购物车中删除商品
export const deleteCartItemService = (id) => {
  return request.post('/cart/delete', { id });
}

// 清空购物车
export const clearCartService = () => {
  return request.post('/cart/clear');
}

//结算购物车
export const checkoutCartService = (checkoutData) => {
  return request.post('/cart/settle', checkoutData);
}
