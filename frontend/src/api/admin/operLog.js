import adminRequest from '@/utils/adminRequest.js'

// 获取操作日志列表（带条件查询）
export const getOperLogList = (data) => {
  return adminRequest.post('/sys/oper-log/list', data)
} 