import adminRequest from '@/utils/adminRequest.js'

// 创建用户退款记录列表
export const createUserRefundListService = (data) => {
  return adminRequest.post('/payment/refund/create', data)
}