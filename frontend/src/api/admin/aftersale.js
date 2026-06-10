import adminRequest from '@/utils/adminRequest.js'

// 1. 用户创建售后申请
export const createAfterSaleService = (data) => {
  return adminRequest.post('/oms/after-sale/create', data)
}

// 2. 用户获取自己的售后列表
export const getUserAfterSaleListService = () => {
  return adminRequest.get('/oms/after-sale/user/list')
}

// 3. 获取售后详情
export const getAfterSaleDetailService = (id) => {
  return adminRequest.get(`/oms/after-sale/detail?id=${id}`)
}

// 4. 管理员审核通过
export const approveAfterSaleService = (id) => {
  return adminRequest.post('/oms/after-sale/admin/approve', { id })
}

// 5. 管理员驳回售后
export const rejectAfterSaleService = (id, rejectReason) => {
  return adminRequest.post('/oms/after-sale/admin/reject', { id, rejectReason })
}

// 6. 管理员退款
export const refundAfterSaleService = (id) => {
  return adminRequest.post('/oms/after-sale/admin/refund', { id })
}

// 7. 用户提交退货物流信息
export const userReturnGoodsService = (data) => {
  return adminRequest.post('/oms/after-sale/user/return', data)
}

// 8. 管理员确认收货
export const receiveAfterSaleGoodsService = (id) => {
  return adminRequest.post('/oms/after-sale/admin/receive', { id })
}

// 9. 管理员获取所有售后列表
export const getAllAfterSaleListService = (params = {}) => {
  return adminRequest.post('/oms/after-sale/admin/list', params)
}


// 10. 管理员获取待审核售后列表
export const getPendingAfterSaleListService = () => {
  return adminRequest.get('/oms/after-sale/admin/pendingReview')
}