import request from '@/utils/request.js'

// 获取用户物流详情列表
export const getLogisticsListService = (orderId) => {
  return request.get('/oms/logistics/user/trace/list', { params: { orderId } })
}

//添加物流
export const createLogisticsService = (data) => {
  return request.post('/oms/logistics/create', data)
}

//自动物流轨迹
export const createLogisticsTraceService = (orderId) => {
  return request.post('/oms/logistics/user/addTrace', { orderId })
}


//签收
export const signLogisticsService = (orderId) => {
  return request.post('/oms/logistics/user/sign', null, {
    params: { orderId }
  })
}
