import adminRequest from "../../utils/adminRequest.js";

// 商品规格（属性）管理
export const getAttributeListService = (params) => {
  return adminRequest.post('/product/attribute/admin/list', params)
}

/*新增规格 */
export const addAttributeService = (data) => {
  return adminRequest.post('/product/attribute/add', data)
}

/*修改规格*/
export const updateAttributeService = (data) => {
  return adminRequest.post('/product/attribute/update', data)
}

/*获取规格详情*/
export const getAttributeDetailService = (params) => {
  return adminRequest.post('/product/attribute/detail', params)
}

/*修改规格状态*/
export const updateAttributeStatusService = (params) => {
  return adminRequest.post('/product/attribute/updateStatus', params)
}

/* 删除规格*/
export const deleteAttributeService = (params) => {
  return adminRequest.post('/product/attribute/delete', params)
}

/*根据分类ID获取规格列表 */
export const listByCategoryService = (params) => {
  return adminRequest.post('/product/attribute/listByCategory', params)
}

// 规格值管理
//根据规格ID获取规格值列表
export const listValuesByAttrService = (params) => {
  return adminRequest.post('/product/attribute/value/listByAttr', params)
}

// 获取规格值详情
export const getAttributeValueDetailService = (params) => {
  return adminRequest.post('/product/attribute/value/detail', params)
}

//新增规格值
export const addAttributeValueService = (data) => {
  return adminRequest.post('/product/attribute/value/add', data)
}

//修改规格值
export const updateAttributeValueService = (data) => {
  return adminRequest.post('/product/attribute/value/update', data)
}

//修改规格值状态
export const updateAttributeValueStatusService = (params) => {
  return adminRequest.post('/product/attribute/value/updateStatus', params)
}

//删除规格值
export const deleteAttributeValueService = (params) => {
  return adminRequest.post('/product/attribute/value/delete', params)
}
