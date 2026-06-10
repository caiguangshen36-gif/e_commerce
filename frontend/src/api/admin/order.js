import adminRequest from '@/utils/adminRequest.js'
//获取订单统计数据
export const getOrderStatsService = () => {
  return adminRequest.get('/order/admin/stats')
}

// 后台查询订单列表
export const getAdminOrderListService = (queryParams = {}) => {
  return adminRequest.post('/order/admin/list', queryParams)
}

// 后台获取订单详情
export const getAdminOrderDetailService = (orderId) => {
  return adminRequest.get('/order/admin/detail', { params: { orderId } }, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

//后台获取未发货订单列表
export const getUndeliveredOrdersService = () => {
  return adminRequest.get('/order/admin/neverDeliver')
}

// 订单发货
export const deliverOrderService = (orderId) => {
  return adminRequest.post('/order/admin/deliver', null, {
    params: { orderId }
  })
}

// 管理员修改订单状态
export const updateOrderStatusService = (orderId, status) => {
  return adminRequest.post('/order/updateStatus', { orderId, status })
}

// 热销商品销售额 TOP5
export function getProductSalesTop5(params) {
  return adminRequest.post('/order/admin/productSalesTop5', params)
}
