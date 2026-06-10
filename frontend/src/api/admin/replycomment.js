import adminRequest from '@/utils/adminRequest.js'

// ====================== 商品评论接口 ======================

// 2. 根据商品ID获取评论列表（含回复）
export const getCommentListByProductIdService = (productId) => {
  return adminRequest.get(`/comment/product/${productId}`)
}

// 3. 获取当前用户的评论列表
export const getMyCommentListService = () => {
  return adminRequest.get('/comment/user')
}

// 4. 获取单条评论详情
export const getCommentDetailService = (id) => {
  return adminRequest.get(`/comment/detail/${id}`)
}

// 5. 删除评论
export const deleteCommentService = (id) => {
  return adminRequest.post('/comment/delete', { id })
}

// 6. 管理员更新评论状态
export const updateCommentStatusService = (id, status) => {
  return adminRequest.post('/comment/updateStatus', { id, status })
}

// ====================== 评论回复接口 ======================

// 7. 发布评论回复（商家/用户）
export const addCommentReplyService = (replyDto) => {
  return adminRequest.post('/comment/reply/add', replyDto)
}

// 8. 根据评论ID获取所有回复
export const getReplyListByCommentIdService = (commentId) => {
  return adminRequest.get(`/comment/reply/list/${commentId}`)
}

// 9. 删除回复
export const deleteCommentReplyService = (id) => {
  return adminRequest.post('/comment/reply/delete', { id })
}