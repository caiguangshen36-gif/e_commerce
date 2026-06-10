//导入request.js请求工具
import request from '@/utils/request.js'

// 获取订单列表
export const getOrderListService = (data) => {
  return request.post('/order/list', data);
};

// 获取订单详情
export const getOrderDetailService = (orderId) => {
  return request.get('/order/detail', { params: { orderId } });
};

// 创建订单
export const createOrderService = (orderData) => {
  return request.post('/order/create', orderData);
};

// 取消订单 
export const cancelOrderService = (data) => {
  return request.post('/order/cancel', data);
};
// 确认收货
export const confirmOrderService = (orderId) => {
  return request.post('/order/confirm', { orderId });
};

// 删除订单 
export const deleteOrderService = (orderId) => {
  return request.post('/order/delete', { orderId });
}

// 更新订单状态
export const updateOrderStatusService = (orderId, status) => {
  return request.post('/order/updateStatus', { orderId, status })
}