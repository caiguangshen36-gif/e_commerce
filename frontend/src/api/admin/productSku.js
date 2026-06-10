import adminRequest from '../../utils/adminRequest.js'

// 根据商品ID获取SKU列表
export const getSkuListByProductIdService = (params) => {
  return adminRequest.post('/product/sku/list', params)
}

//获取所有SKU列表
export const getAllSkuListService = () => {
  return adminRequest.get('/product/sku/allList')
}

export const getSkuPageListService = (params) => {
  return adminRequest.post('/product/sku/admin/list', params)
}


// 根据ID获取SKU详情
export const getSkuDetailService = (params) => {
  return adminRequest.post('/product/sku/detail', params)
}

// 新增SKU
export const addSkuService = (params) => {
  return adminRequest.post('/product/sku/add', params)
}

// 修改SKU
export const updateSkuService = (skuDto) => {
  return adminRequest.post('/product/sku/update', skuDto)
}

// 修改SKU状态
export const updateSkuStatusService = (params) => {
  return adminRequest.post('/product/sku/updateStatus', params)
}

// 删除SKU
export const deleteSkuService = (params) => {
  return adminRequest.post('/product/sku/delete', params)
}

//获取商品库存预警
export const getSkuStockWarningService = () => {
  return adminRequest.get('/product/sku/admin/stockWarning')
}