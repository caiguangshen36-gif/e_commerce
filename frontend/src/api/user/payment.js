import request from '@/utils/request.js'

// 创建用户支付记录列表
export const createUserPaymentListService = (data) => {
  return request.post('/payment/transaction/create', data)
}

// 创建用户退款记录列表
export const createUserRefundListService = (data) => {
  return request.post('/payment/refund/create', data)
}