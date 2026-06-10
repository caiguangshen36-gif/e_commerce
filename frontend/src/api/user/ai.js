import request from '../../utils/request.js'

// AI 客服对话
export function aiCustomerService(data) {
  return request.post('/ai/customer-service', data)
}

// AI 智能导购
export function aiShopGuide(data) {
  return request.post('/ai/shop-guide', data)
}