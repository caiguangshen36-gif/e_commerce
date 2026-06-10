import adminRequest from '@/utils/adminRequest.js'

// 获取消息类型
export const getNoticeTypesService = () => {
  return adminRequest.get('/notice/admin/types');
}

// 获取管理员消息列表
export const getAdminNoticeListService = (data) => {
  return adminRequest.post('/notice/admin/list', data);
}

// 获取消息详情
export const getAdminNoticeDetailService = (data) => {
  return adminRequest.post('/notice/admin/detail', data);
}

// 获取未读数量
export const getAdminUnreadCountService = () => {
  return adminRequest.get('/notice/admin/unread-count');
}

// 标记已读（单个/批量/全部）
export const markAdminNoticeReadService = (data) => {
  return adminRequest.post('/notice/admin/mark-read', data);
}

// 删除消息
export const deleteAdminNoticeService = (data) => {
  return adminRequest.post('/notice/admin/delete', data);
}

// 发送通知
export const sendAdminNoticeService = (data) => {
  return adminRequest.post('/notice/admin/send', data);
}

// 发送给指定用户
export const sendNoticeToUserService = (data) => {
  return adminRequest.post('/notice/admin/send-to-user', data);
}