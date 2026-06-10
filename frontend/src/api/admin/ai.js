import adminRequest from '../../utils/adminRequest.js'

// 一键生成结构化商品标题+详情
export const aiGenerateGoodsDescService = (data) => {
  return adminRequest.post('/ai/generate-goods-desc', data)
}

// 单独生成商品标题
export const aiGenerateTitleService = (data) => {
  return adminRequest.post('/ai/generate-title', data)
}

// 单独生成商品详情文案
export const aiGenerateDetailService = (data) => {
  return adminRequest.post('/ai/generate-detail', data)
}

//  AI 内容审核 
export const aiAuditGoodsContentService = (data) => {
  return adminRequest.post('/ai/audit-goods', data)
}

//  AI 智能搜索
export const aiIntelligentSearchService = (data) => {
  return adminRequest.post('/ai/intelligent-search', data)
}