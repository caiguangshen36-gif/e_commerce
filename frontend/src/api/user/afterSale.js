import request from '../../utils/request.js'

// 1. 创建售后申请
export const createAfterSaleService = (data) => {
  return request.post('/oms/after-sale/create', data)
}

// 2. 我的售后列表
export const getUserAfterSaleListService = (data) => {
  return request.post('/oms/after-sale/user/list', data)
}

// 3. 售后详情
export const getAfterSaleDetailService = (id) => {
  return request.get('/oms/after-sale/detail', { params: { id } })
}

// 4. 用户退货提交物流
export const userReturnGoodsService = (data) => {
  return request.post('/oms/after-sale/user/return', data)
}

// 5. 获取物流信息
export const getAfterSaleDeliveryService = (afterSaleId) => {
  return request.get('/oms/after-sale/delivery', { params: { afterSaleId } })
}

//6、退款
export const refundAfterSaleService = (id) => {
  return request.post('/oms/after-sale/refund', { id })
}