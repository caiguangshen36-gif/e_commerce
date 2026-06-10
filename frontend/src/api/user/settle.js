import request from '@/utils/request.js'

// 创建结算单
export function createSettleService(data) {
  return request.post('/settle/create', data)
}

// 立即购买 - 直接创建结算单
export const createSettleDirectService = (data) => {
  return request.post('/settle/createDirect', data)
}

// 获取结算单详情 
export function getSettleDetailService(id) {
  return request.post('/settle/detail', { id })
}

// 确认结算单（生成正式订单）
export function confirmSettleService(id) {
  return request.post('/settle/confirm', { id })
}

// 取消结算单 
export function cancelSettleService(id) {
  return request.post('/settle/cancel', { id })
}

// 获取我的结算单列表
export function getSettleListService() {
  return request.post('/settle/list')
}