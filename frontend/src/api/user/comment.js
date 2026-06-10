import request from '../../utils/request.js'

// ====================== 商品评论接口 ======================

// 1. 发布商品评论
export const addCommentService = (commentDto) => {
  return request.post('/comment/add', commentDto)
}

// 2. 根据商品ID获取评论列表
export const getCommentListByProductIdService = (productId) => {
  return request.get('/comment/list', {
    params: { productId }
  })
}

// 3. 获取当前用户的评论列表
export const getMyCommentListService = () => {
  return request.get('/comment/user')
}

// 4. 获取单条评论详情
export const getCommentDetailService = (id) => {
  return request.get(`/comment/detail/${id}`)
}

// 5. 删除评论
export const deleteCommentService = (id) => {
  return request.post('/comment/delete', { id })
}

// 6. 管理员更新评论状态
export const updateCommentStatusService = (id, status) => {
  return request.post('/comment/updateStatus', { id, status })
}

// ====================== 评论回复接口 ======================

// 7. 发布评论回复
export const addCommentReplyService = (replyDto) => {
  return request.post('/comment/reply/add', replyDto)
}

// 8. 根据评论ID获取所有回复
export const getReplyListByCommentIdService = (commentId) => {
  return request.get(`/comment/reply/list/${commentId}`)
}

// 9. 删除回复
export const deleteCommentReplyService = (id) => {
  return request.post('/comment/reply/delete', { id })
}