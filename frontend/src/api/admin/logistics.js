import adminRequest from '@/utils/adminRequest.js'

// 1. 管理员创建物流（发货）
export const createLogisticsService = (data) => {
  return adminRequest.post('/oms/logistics/create', data)
}

// 2. 管理员更新物流信息（快递公司+单号）
export const updateDeliveryService = (data) => {
  return adminRequest.post('/oms/logistics/admin/updateDelivery', data)
}

// 3. 管理员更新物流状态
export const updateLogisticsStatusService = (id, status) => {
  return adminRequest.post('/oms/logistics/admin/updateStatus', { id, status })
}

// 4. 管理员添加物流轨迹
export const addLogisticsTraceService = (data) => {
  return adminRequest.post('/oms/logistics/admin/addTrace', data)
}

// 5. 管理员获取全部物流列表
export const getAllLogisticsListService = (params = {}) => {
  return adminRequest.post('/oms/logistics/admin/listAll', params)
}

// 6. 查看物流详情（含轨迹）
export const getLogisticsDetailService = (orderId) => {
  return adminRequest.get('/oms/logistics/detail', { params: { orderId } })
}