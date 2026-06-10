import request from '@/utils/request.js'

// 获取用户消息类型
export const getNoticeTypesService = () => {
  return request.get('/notice/user/types')
}

// 获取用户消息列表
export const getUserNoticeListService = (data) => {
  return request.post('/notice/user/list', data)
}

// 获取消息详情
export const getUserNoticeDetailService = (noticeId) => {
  return request.post('/notice/user/detail', { noticeId })
}

// 获取用户未读消息数量
export const getUserUnreadCountService = () => {
  return request.get('/notice/user/unread-count')
}

// 标记已读（支持单个/批量/全部已读）
export const markUserNoticeReadService = (ids) => {
  return request.post('/notice/user/mark-read', { ids })
}

// 删除消息
export const deleteUserNoticeService = (noticeId) => {
  return request.post('/notice/user/delete', { noticeId })
}

// 发送用户消息
export const sendUserNoticeService = (data) => {
  return request.post('/notice/user/send', data)
}

// 给指定角色的管理员发送系统通知
export const sendAdminNoticeService = (data) => {
  return request.post('/notice/admin/send', data)
}