import adminRequest from "../../utils/adminRequest.js";

// 添加轮播图
export const addCarouselService = (data) => {
  return adminRequest.post('/marketing/carousel', data)
}

// 获取轮播图列表
export const getCarouselListService = () => {
  return adminRequest.post('/marketing/carousel/list')
}

// 获取轮播图详情
export const getCarouselDetailService = (id) => {
  return adminRequest.post('/marketing/carousel/detail', { id })
}

// 修改轮播图
export const updateCarouselService = (data) => {
  return adminRequest.post('/marketing/carousel/update', data)
}

// 删除轮播图
export const deleteCarouselService = (id) => {
  return adminRequest.post('/marketing/carousel/delete', { id })
}

// 修改轮播图状态（启用/禁用）
export const updateCarouselStatusService = (id) => {
  return adminRequest.post('/marketing/carousel/updateStatus', { id })
}

// 获取启用的轮播图（前台用）
export const getEnabledCarouselListService = () => {
  return adminRequest.post('/marketing/carousel/enabled')
}