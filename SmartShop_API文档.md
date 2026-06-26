# SmartShop 智慧电商系统 — 后端接口文档

> **版本:** v1.0 | **日期:** 2026-06-26 | **基础URL:** `http://localhost:8080`
>
> **目标读者:** 前端开发 / 测试工程师 / 第三方对接

---

## 通用约定

### 统一响应结构

所有接口返回 `Result<T>` 包装：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| code | 含义 |
|:---:|------|
| 200 | 成功 |
| 400 | 参数错误（由 `IllegalArgumentException` 或 `@Valid` 校验失败触发） |
| 401 | 未认证（Token 缺失/过期/无效） |
| 403 | 无权限（`@PreAuthorize` 鉴权失败） |
| 500 | 服务器异常 / 业务错误（`BusinessException` 或未捕获异常） |

### 认证方式

```
Authorization: Bearer <token>
```

- 前台用户 Token → 从 `/ums/user/login` 获取
- 后台管理员 Token → 从 `/sys/user/login` 获取
- Token 存储在 Redis，修改密码后所有旧 Token 失效（`password_version` 机制）

### 分页约定

分页请求参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|:---:|------|
| pageNum | Integer | 1 | 页码，从 1 开始 |
| pageSize | Integer | 10 | 每页条数，最大 100 |

分页响应结构（`PageVo<T>`）：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [...],
    "total": 120
  }
}
```

### 枚举值说明

| 模块 | 字段 | 值 | 含义 |
|------|------|---|------|
| 通用 | status | 0 | 禁用/下架 |
| 通用 | status | 1 | 启用/上架 |
| 订单 | status | 0 | 待支付 |
| 订单 | status | 1 | 已支付 |
| 订单 | status | 2 | 已发货 |
| 订单 | status | 3 | 已签收 |
| 订单 | status | 4 | 已取消(系统自动) |
| 订单 | status | 5 | 已取消(用户主动) |
| 售后 | type | 1 | 仅退款 |
| 售后 | type | 2 | 退货退款 |
| 售后 | status | 0 | 待审核 |
| 售后 | status | 1 | 审核通过 |
| 售后 | status | 2 | 已退款 |
| 售后 | status | 3 | 驳回 |
| 售后 | status | 4 | 用户已退货 |
| 售后 | status | 5 | 商家收货完成 |
| 物流 | status | 0 | 待发货 |
| 物流 | status | 1 | 已发货 |
| 物流 | status | 2 | 运输中 |
| 物流 | status | 3 | 已签收 |
| 物流 | status | 4 | 异常 |
| 库存锁 | status | 0 | 锁定中 |
| 库存锁 | status | 1 | 已扣减 |
| 库存锁 | status | 2 | 已释放 |
| 支付 | payType | 1 | 微信支付 |
| 支付 | payType | 2 | 支付宝 |
| 菜单 | type | 1 | 目录 |
| 菜单 | type | 2 | 菜单 |
| 菜单 | type | 3 | 按钮 |
| 知识库 | type | goods | 商品相关 |
| 知识库 | type | after_sale | 售后相关 |
| 知识库 | type | service | 服务相关 |
| 知识库 | type | payment | 支付相关 |
| 知识库 | type | order | 订单相关 |
| 知识库 | type | logistics | 物流相关 |
| 知识库 | content | promotion | 促销相关 |

---

## 模块一：商品管理

### 概述

商品模块是电商系统的核心，提供商品 CRUD、分类树管理、SKU 多规格体系、属性/规格值管理、收藏与浏览足迹功能。商品数据同步至 Elasticsearch 索引 `pms_product`，支持 IK 中文分词全文搜索。

**前置条件:** 搜索/推荐/热门接口无需登录；管理类接口需后台管理员 Token。

**涉及数据表:** `pms_product`、`pms_category`、`pms_sku`、`pms_attribute`、`pms_attribute_value`、`pms_sku_attr`、`pms_product_collect`、`pms_product_browse`、`pms_sku_stock_lock`

---

### 1.1 商品分页列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 商品分页列表 |
| 请求方法 | POST |
| URL | `/product/list` |
| 功能描述 | 分页查询商品列表，支持关键词、状态、分类筛选 |
| 认证 | ❌ 无需认证（白名单） |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| pageNum | Integer | 否 | ≥1，默认 1 | 页码 | 1 |
| pageSize | Integer | 否 | 1~100，默认 10 | 每页条数 | 10 |
| keyword | String | 否 | — | 商品名称模糊搜索 | "手机" |
| status | Integer | 否 | 0/1 | 上下架状态筛选 | 1 |
| categoryId | Long | 否 | — | 分类筛选 | 1001 |

**响应参数 (data: PageVo\<PmsProductVo\>)**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| list | Array | 商品列表 | — |
| list[].id | Long | 商品ID | 1 |
| list[].productName | String | 商品名称 | "iPhone 15 Pro Max" |
| list[].categoryId | Long | 分类ID | 1001 |
| list[].categoryName | String | 分类名称 | "智能手机" |
| list[].pic | String | 商品主图URL | "https://oss.xxx.com/pic.jpg" |
| list[].originalPrice | BigDecimal | 原价/市场价 | 9999.00 |
| list[].shortDesc | String | 简短描述 | "A17 Pro 芯片" |
| list[].status | Integer | 0-下架 1-上架 | 1 |
| list[].isHot | Integer | 0-否 1-是 | 0 |
| list[].hotSort | Integer | 热门排序（越小越前） | 1 |
| list[].createTime | String | 创建时间 | "2026-01-15 10:30:00" |
| list[].skuList | Array\<SkuVo\> | SKU列表（嵌套） | — |
| list[].skuList[].id | Long | SKU ID | 10 |
| list[].skuList[].skuCode | String | SKU编码 | "SKU-00001" |
| list[].skuList[].price | BigDecimal | 售价 | 8999.00 |
| list[].skuList[].stock | Integer | 库存 | 100 |
| list[].skuList[].pic | String | SKU图片 | "https://..." |
| total | Long | 数据总数 | 120 |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/product/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10,"keyword":"手机","status":1}'
```

---

### 1.2 商品详情

| 项目 | 说明 |
|------|------|
| 接口名称 | 商品详情 |
| 请求方法 | POST |
| URL | `/product/detail` |
| 功能描述 | 根据商品ID获取完整商品信息（含SKU列表、分类名称） |
| 认证 | ❌ 无需认证（白名单） |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| id | Long | ✅ | 商品必须存在 | 商品ID | 1 |

**响应参数 (data: PmsProductVo)**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| id | Long | 商品ID | 1 |
| productName | String | 商品名称 | "iPhone 15 Pro Max 256G" |
| categoryId | Long | 分类ID | 1001 |
| categoryName | String | 分类名称 | "智能手机" |
| pic | String | 商品主图 | "https://oss.xxx.com/pic.jpg" |
| detailHtml | String | 商品详情HTML | "\<p\>...\</p\>" |
| originalPrice | BigDecimal | 原价 | 9999.00 |
| shortDesc | String | 简短描述 | "A17 Pro" |
| status | Integer | 0-下架 1-上架 | 1 |
| isHot | Integer | 0-否 1-是 | 0 |
| hotSort | Integer | 热门排序 | 1 |
| createTime | String | 创建时间 | "2026-01-15 10:30:00" |
| skuList | Array\<SkuVo\> | SKU列表 | — |
| skuList[].skuCode | String | SKU编码 | "SKU-00001" |
| skuList[].price | BigDecimal | 售价 | 8999.00 |
| skuList[].stock | Integer | 库存 | 100 |
| skuList[].skuAttrList | Array | 规格属性列表 | [{"attrName":"颜色","attrValue":"黑色"}] |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/product/detail \
  -H "Content-Type: application/json" \
  -d '{"id":1}'
```

---

### 1.3 添加商品

| 项目 | 说明 |
|------|------|
| 接口名称 | 添加商品 |
| 请求方法 | POST |
| URL | `/product/add` |
| 功能描述 | 后台添加商品（含SKU列表一起提交），成功后同步至ES索引 |
| 认证 | ✅ 后台管理员 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| productName | String | ✅ | @NotBlank | 商品名称 | "iPhone 15" |
| categoryId | Long | ✅ | @NotNull | 商品分类ID | 1001 |
| pic | String | 否 | — | 商品主图URL | "https://oss.xxx.com/pic.jpg" |
| detailHtml | String | 否 | — | 商品详情HTML | "\<p\>详情\</p\>" |
| status | Integer | 否 | 0/1，默认 1 | 上下架状态 | 1 |
| skuList | Array | 否 | — | SKU列表（结构见下方） | — |
| skuList[].skuCode | String | ✅ | @NotBlank，全局唯一 | SKU编码 | "SKU-00001" |
| skuList[].price | BigDecimal | ✅ | @NotNull，>0 | 销售价 | 8999.00 |
| skuList[].costPrice | BigDecimal | 否 | — | 成本价 | 7000.00 |
| skuList[].stock | Integer | 否 | ≥0 | 库存数量 | 100 |
| skuList[].stockWarning | Integer | 否 | — | 库存预警阈值 | 10 |
| skuList[].pic | String | 否 | — | SKU图片 | "https://..." |
| skuList[].weight | BigDecimal | 否 | — | 重量(kg) | 0.22 |
| skuList[].volume | BigDecimal | 否 | — | 体积(m³) | 0.001 |
| skuList[].skuAttrList | Array | 否 | — | SKU规格关联 | — |
| skuList[].skuAttrList[].attrId | Long | 否 | 属性必须存在 | 属性ID | 10 |
| skuList[].skuAttrList[].attrValueId | Long | 否 | 属性值必须存在 | 属性值ID | 50 |

**业务错误码**

| code | msg | 触发条件 |
|:---:|------|----------|
| 500 | skuCode已存在 | 重复的 skuCode |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/product/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "productName":"iPhone 15 Pro Max",
    "categoryId":1001,
    "pic":"https://oss.xxx.com/pic.jpg",
    "detailHtml":"<p>商品详情</p>",
    "status":1,
    "skuList":[{
      "skuCode":"SKU-IP15-001",
      "price":8999.00,
      "stock":100,
      "stockWarning":10,
      "skuAttrList":[{"attrId":1,"attrValueId":3}]
    }]
  }'
```

---

### 1.4 更新商品

| 项目 | 说明 |
|------|------|
| 接口名称 | 更新商品 |
| 请求方法 | POST |
| URL | `/product/update` |
| 功能描述 | 更新商品基本信息，同步ES索引 |
| 认证 | ✅ 后台管理员 Token |

**请求参数:** 同 [1.3 添加商品](#13-添加商品)，`id` 字段必填。

---

### 1.5 更新商品状态（上下架）

| 项目 | 说明 |
|------|------|
| 接口名称 | 更新商品状态 |
| 请求方法 | POST |
| URL | `/product/updateStatus` |
| 功能描述 | 批量/单个设置商品上下架状态 |
| 认证 | ✅ 后台管理员 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| id | Long | ✅ | 商品必须存在 | 商品ID | 1 |
| status | Integer | ✅ | 0（下架）/ 1（上架） | 状态 | 1 |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/product/updateStatus \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"id":1,"status":0}'
```

---

### 1.6 删除商品

| 项目 | 说明 |
|------|------|
| 接口名称 | 删除商品 |
| 请求方法 | POST |
| URL | `/product/delete` |
| 功能描述 | 删除商品（同时删除关联SKU） |
| 认证 | ✅ 后台管理员 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | ✅ | 商品ID | 1 |

---

### 1.7 按分类查询商品

| 项目 | 说明 |
|------|------|
| 接口名称 | 按分类查询商品列表 |
| 请求方法 | POST |
| URL | `/product/listByCategory` |
| 功能描述 | 根据分类ID查询该分类下的所有上架商品 |
| 认证 | ❌ 无需认证 |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | ✅ | 分类ID | 1001 |

**响应:** `Result<List<PmsProductVo>>`

---

### 1.8 ES全文搜索

| 项目 | 说明 |
|------|------|
| 接口名称 | 商品搜索 |
| 请求方法 | GET |
| URL | `/product/search` |
| 功能描述 | Elasticsearch 全文搜索商品，productName 字段 IK 中文分词 |
| 认证 | ❌ 无需认证 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| keyword | String | ✅ | 非空 | 搜索关键词 | "手机 黑色" |

**响应:** `Result<List<PmsProductVo>>`

**cURL 示例**

```bash
curl "http://localhost:8080/product/search?keyword=手机"
```

---

### 1.9 热门商品列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 热门商品列表 |
| 请求方法 | GET |
| URL | `/product/hotList` |
| 功能描述 | 查询 `isHot=1` 的商品，按 `hotSort` 升序排列 |
| 认证 | ❌ 无需认证 |

**响应:** `Result<List<PmsProductVo>>`

---

### 1.10 个性化推荐

| 项目 | 说明 |
|------|------|
| 接口名称 | 个性化商品推荐 |
| 请求方法 | GET |
| URL | `/product/recommend` |
| 功能描述 | 混合推荐算法：收藏商品权重 80% + 浏览记录权重 20%，去重后返回最多 10 个推荐商品。需登录态获取用户ID |
| 认证 | ✅ 前台用户 Token |

**响应:** `Result<List<PmsProductVo>>`

---

### 1.11 设置热门状态

| 项目 | 说明 |
|------|------|
| 接口名称 | 设置商品热门状态 |
| 请求方法 | POST |
| URL | `/product/updateHotStatus` |
| 功能描述 | 后台设置商品是否热门及排序值 |
| 认证 | ✅ 后台管理员 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | ✅ | 商品ID | 1 |
| isHot | Integer | ✅ | 0-否 1-是 | 1 |
| hotSort | Integer | 否 | 排序值，数字越小越靠前 | 1 |

---

### 1.12 商品分类管理

#### 1.12.1 获取分类树（前台）

| 项目 | 说明 |
|------|------|
| URL | POST `/product/category/list` |
| 认证 | ❌ 无需认证 |
| 功能 | 返回全部分类（树形结构，parentId 自关联） |

**响应:** `Result<List<PmsCategory>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 分类ID |
| categoryName | String | 分类名称 |
| parentId | Long | 父分类ID（顶级为 0） |
| level | Integer | 层级（1/2/3） |
| sort | Integer | 排序 |
| icon | String | 分类图标 |
| status | Integer | 0-禁用 1-启用 |

#### 1.12.2 后台分类分页查询

| 项目 | 说明 |
|------|------|
| URL | POST `/product/category/admin/list` |
| 认证 | ✅ 后台管理员 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryName | String | 否 | 分类名称模糊搜索 |
| parentId | Long | 否 | 父分类ID |
| status | Integer | 否 | 状态筛选 |

#### 1.12.3 添加/更新/删除分类

| 操作 | 方法 | URL | 认证 | Body |
|------|------|------|------|------|
| 添加 | POST | `/product/category/add` | ✅ admin | `{categoryName, parentId, level, sort, icon, status}` |
| 更新 | POST | `/product/category/update` | ✅ admin | 同上 + `id` |
| 删除 | POST | `/product/category/delete` | ✅ admin | `{id}` |
| 状态 | POST | `/product/category/updateStatus` | ✅ admin | `{id, status}` |
| 子分类 | POST | `/product/category/listByParentId` | ❌ | `{parentId}` |
| 详情 | POST | `/product/category/detail` | ❌ | `{id}` |

---

### 1.13 商品属性管理

#### 1.13.1 属性列表（前台）

| 项目 | 说明 |
|------|------|
| URL | POST `/product/attribute/list` |
| 认证 | ❌ 无需认证 |

#### 1.13.2 后台属性管理

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 分页查询 | POST | `/product/attribute/admin/list` | ✅ admin | `{pageNum, pageSize, attrName, categoryId, status}` |
| 添加 | POST | `/product/attribute/add` | ✅ admin | `{categoryId, attrName, sort, status, valueList}` |
| 更新 | POST | `/product/attribute/update` | ✅ admin | 同上 + `id` |
| 删除 | POST | `/product/attribute/delete` | ✅ admin | `{id}` |
| 状态 | POST | `/product/attribute/updateStatus` | ✅ admin | `{id, status}` |
| 详情 | POST | `/product/attribute/detail` | ❌ | `{id}` |
| 按分类查 | POST | `/product/attribute/listByCategory` | ❌ | `{categoryId}` |

#### 1.13.3 属性值管理

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 按属性查值 | POST | `/product/attribute/value/listByAttr` | ❌ | `{attrId}` |
| 值详情 | POST | `/product/attribute/value/detail` | ❌ | `{id}` |
| 添加值 | POST | `/product/attribute/value/add` | ✅ admin | `{attrId, attrValue, sort, status}` |
| 更新值 | POST | `/product/attribute/value/update` | ✅ admin | 同上 + `id` |
| 状态 | POST | `/product/attribute/value/updateStatus` | ✅ admin | `{id, status}` |
| 删除值 | POST | `/product/attribute/value/delete` | ✅ admin | `{id}` |

---

### 1.14 SKU 管理

#### 1.14.1 按商品查SKU

| 项目 | 说明 |
|------|------|
| URL | POST `/product/sku/list` |
| 认证 | ❌ 无需认证 |
| Body | `{productId}` |

#### 1.14.2 SKU详情

| 项目 | 说明 |
|------|------|
| URL | POST `/product/sku/detail` |
| 认证 | ✅（前台或后台均可） |
| Body | `{id}` |

**响应:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | SKU ID |
| productId | Long | 所属商品ID |
| skuCode | String | SKU编码 |
| price | BigDecimal | 售价 |
| costPrice | BigDecimal | 成本价 |
| stock | Integer | 库存 |
| stockWarning | Integer | 库存预警阈值 |
| pic | String | SKU图片 |
| weight | BigDecimal | 重量 |
| volume | BigDecimal | 体积 |
| status | Integer | 0-禁用 1-启用 |
| skuAttrList | Array | 规格属性列表 |

#### 1.14.3 后台SKU管理

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 所有SKU | GET | `/product/sku/allList` | ✅ admin | — |
| 分页查询 | POST | `/product/sku/admin/list` | ✅ admin | `{pageNum, pageSize, skuCode, productId, status}` |
| 添加 | POST | `/product/sku/add` | ✅ admin | `{productId, skuCode, price, stock, ...}` |
| 更新 | POST | `/product/sku/update` | ✅ admin | `PmsSkuDto` 全部字段 |
| 状态 | POST | `/product/sku/updateStatus` | ✅ admin | `{id, status}` |
| 删除 | POST | `/product/sku/delete` | ✅ admin | `{id}` |

#### 1.14.4 库存扣减（支付成功回调）

| 项目 | 说明 |
|------|------|
| URL | POST `/product/sku/updateStocks` |
| 认证 | ✅ 前台用户 Token |
| 功能 | 根据订单ID批量扣减对应SKU库存，库存低于预警阈值时自动触发通知 |
| Query | `?orderId=123` |

#### 1.14.5 库存回滚（取消支付）

| 项目 | 说明 |
|------|------|
| URL | POST `/product/sku/rollbackStocks` |
| 认证 | ✅ 前台用户 Token |
| 功能 | 取消支付时回滚已锁定库存 |
| Query | `?orderId=123` |

#### 1.14.6 库存预警列表

| 项目 | 说明 |
|------|------|
| URL | GET `/product/sku/admin/stockWarning` |
| 认证 | ✅ 后台管理员 Token |
| 功能 | 查询所有库存 ≤ stockWarning 阈值的 SKU |

---

### 1.15 商品收藏

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 添加收藏 | POST | `/product/collect/add` | ✅ user | `{productId}` |
| 取消收藏 | POST | `/product/collect/remove` | ✅ user | `{productId}` |
| 收藏列表 | POST | `/product/collect/list` | ✅ user | `{pageNum, pageSize, productName, categoryId}` |
| 是否已收藏 | POST | `/product/collect/isCollected` | ✅ user | `{productId}` |
| 清空收藏 | POST | `/product/collect/clear` | ✅ user | — |
| 批量取消 | POST | `/product/collect/batchRemove` | ✅ user | `{productIds: [1,2,3]}` |

---

### 1.16 浏览记录

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 添加记录 | POST | `/product/browse/add` | ✅ user | `{productId}` |
| 删除单条 | POST | `/product/browse/delete` | ✅ user | `{productId}` |
| 记录列表 | POST | `/product/browse/list` | ✅ user | `{pageNum, pageSize, productName, categoryId}` |
| 清空全部 | POST | `/product/browse/clear` | ✅ user | — |
| 批量删除 | POST | `/product/browse/batchDelete` | ✅ user | `{ids: [1,2,3]}` |

---

## 模块二：订单交易

### 概述

订单交易模块涵盖电商核心交易链路：**购物车 → 结算 → 支付 → 物流 → 收货 → 售后**。采用库存锁定表 `pms_sku_stock_lock` 防止并发超卖，支付成功扣减库存、取消支付回滚库存。

**涉及数据表:** `oms_cart`、`oms_settle`、`oms_settle_item`、`oms_order`、`oms_order_item`、`oms_order_cancel`、`payment_transaction`、`payment_refund`、`oms_logistics`、`oms_logistics_trace`、`oms_after_sale`、`oms_after_sale_delivery`、`pms_sku_stock_lock`

---

### 2.1 购物车

#### 2.1.1 购物车列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取购物车列表 |
| 请求方法 | GET |
| URL | `/cart/list` |
| 功能描述 | 查询当前登录用户的购物车商品（含商品名、规格、价格等关联信息） |
| 认证 | ✅ 前台用户 Token |

**响应 (data: List\<CartVo\>)**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| id | Long | 购物车项ID | 10 |
| userId | Long | 用户ID | 1 |
| productId | Long | 商品ID | 100 |
| skuId | Long | SKU ID | 500 |
| quantity | Integer | 数量 | 2 |
| productName | String | 商品名称 | "iPhone 15" |
| skuSpecs | String | SKU规格描述 | "颜色:黑色,存储:256G" |
| pic | String | 商品图片 | "https://..." |
| price | BigDecimal | 单价 | 8999.00 |
| createTime | String | 加入时间 | "2026-06-20 14:30:00" |

#### 2.1.2 加入购物车

| 项目 | 说明 |
|------|------|
| 接口名称 | 加入购物车 |
| 请求方法 | POST |
| URL | `/cart/add` |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 规则 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Long | ✅ | 商品必须存在且上架 | 商品ID | 100 |
| skuId | Long | ✅ | SKU必须存在且启用 | SKU ID | 500 |
| quantity | Integer | ✅ | ≥1 | 购买数量 | 2 |

**业务错误码**

| code | msg | 触发条件 |
|:---:|------|----------|
| 500 | 商品不存在或已下架 | product 不存在或 status=0 |
| 500 | SKU不存在或已禁用 | sku 不存在或 status=0 |
| 500 | 库存不足 | 当前库存 < 已有购物车数量 + 新增数量 |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <user_token>" \
  -d '{"productId":100,"skuId":500,"quantity":1}'
```

#### 2.1.3 更新/删除购物车

| 操作 | 方法 | URL | 认证 | Body |
|------|------|------|------|------|
| 更新数量 | POST | `/cart/update` | ✅ user | `{id, quantity}` |
| 删除商品 | POST | `/cart/delete` | ✅ user | `{id}` |
| 清空购物车 | POST | `/cart/clear` | ✅ user | — |
| 结算 | POST | `/cart/settle` | ✅ user | `[cartId1, cartId2, ...]` — 选中的购物车ID列表 |

---

### 2.2 结算

#### 2.2.1 购物车结算

| 项目 | 说明 |
|------|------|
| 接口名称 | 购物车结算创建结算单 |
| 请求方法 | POST |
| URL | `/settle/create` |
| 功能描述 | 将选中购物车项转为结算单（临时订单），库存锁定 |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| addressId | Long | ✅ | @NotNull，地址必须属于当前用户 | 收货地址ID | 5 |
| cartIds | List\<Long\> | ✅ | @NotEmpty | 选中的购物车ID列表 | [10, 11] |

#### 2.2.2 直接购买结算

| 项目 | 说明 |
|------|------|
| 接口名称 | 直接购买创建结算单 |
| 请求方法 | POST |
| URL | `/settle/createDirect` |
| 功能描述 | 从商品详情页直接购买（跳过购物车） |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| addressId | Long | ✅ | @NotNull | 收货地址ID | 5 |
| productId | Long | ✅ | @NotNull | 商品ID | 100 |
| skuId | Long | ✅ | @NotNull | SKU ID | 500 |
| quantity | Integer | ✅ | @Min(1) | 数量 | 1 |

#### 2.2.3 结算单详情

| 项目 | 说明 |
|------|------|
| 接口名称 | 结算单详情 |
| 请求方法 | POST |
| URL | `/settle/detail` |
| 功能描述 | 获取结算单完整信息（含收货地址、商品明细、金额） |
| 认证 | ✅ 前台用户 Token |
| Body | `{id}` |

**响应 (data: OmsSettleVo)**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 结算单ID |
| totalAmount | BigDecimal | 总金额 |
| addressId | Long | 地址ID |
| receiver | String | 收货人 |
| phone | String | 收货电话 |
| address | String | 完整地址 |
| status | Integer | 0-待确认 1-已确认 |
| statusText | String | 状态文本 |
| items | Array\<OmsSettleItemVo\> | 商品明细列表 |
| items[].productName | String | 商品名 |
| items[].skuSpecs | String | 规格 |
| items[].price | BigDecimal | 单价 |
| items[].quantity | Integer | 数量 |
| items[].totalPrice | BigDecimal | 小计 |
| createTime | String | 创建时间 |

#### 2.2.4 确认结算（生成订单）

| 项目 | 说明 |
|------|------|
| 接口名称 | 确认结算单生成订单 |
| 请求方法 | POST |
| URL | `/settle/confirm` |
| 功能描述 | 确认结算 → 生成正式订单 + 扣减库存 + 清空对应购物车项 |
| 认证 | ✅ 前台用户 Token |
| Body | `{id}` |

**业务错误码**

| code | msg | 触发条件 |
|:---:|------|----------|
| 500 | 库存不足 | 结算期间库存被其他用户消耗 |

---

### 2.3 订单

#### 2.3.1 用户订单列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 用户订单分页列表 |
| 请求方法 | POST |
| URL | `/order/list` |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| pageNum | Integer | 否 | 默认 1 | 1 |
| pageSize | Integer | 否 | 默认 10 | 10 |
| orderSn | String | 否 | 订单号精确搜索 | "202606260001" |
| status | Integer | 否 | 状态筛选，见枚举表 | 1 |
| startTime | String | 否 | 开始时间 | "2026-01-01" |
| endTime | String | 否 | 结束时间 | "2026-12-31" |

**响应 (data: PageVo\<OrderVo\>)**

| 字段 | 类型 | 说明 |
|------|------|------|
| list[].id | Long | 订单ID |
| list[].orderSn | String | 订单编号 |
| list[].totalAmount | BigDecimal | 订单总额 |
| list[].payAmount | BigDecimal | 实付金额 |
| list[].status | Integer | 订单状态码 |
| list[].statusText | String | 状态文本 |
| list[].receiver | String | 收货人 |
| list[].phone | String | 收货电话 |
| list[].address | String | 收货地址 |
| list[].payTime | String | 支付时间 |
| list[].deliveryTime | String | 发货时间 |
| list[].confirmTime | String | 确认收货时间 |
| list[].createTime | String | 下单时间 |
| list[].orderItems | Array | 订单商品明细 |
| list[].logistics | Object | 物流信息（发货后有） |

#### 2.3.2 订单详情

| 项目 | 说明 |
|------|------|
| 接口名称 | 用户订单详情 |
| 请求方法 | GET |
| URL | `/order/detail` |
| 认证 | ✅ 前台用户 Token |
| Query | `?orderId=123` |

#### 2.3.3 取消订单

| 项目 | 说明 |
|------|------|
| 接口名称 | 取消订单 |
| 请求方法 | POST |
| URL | `/order/cancel` |
| 功能描述 | 用户主动取消订单，含退款+库存回滚逻辑 |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| orderId | Long | ✅ | @NotNull | 订单ID | 123 |
| cancelReason | String | ✅ | @NotBlank | 取消原因枚举 | "不想要了" |
| cancelDescription | String | 否 | — | 详细说明 | "颜色选错了" |

#### 2.3.4 确认收货

| 项目 | 说明 |
|------|------|
| 接口名称 | 确认收货 |
| 请求方法 | POST |
| URL | `/order/confirm` |
| 认证 | ✅ 前台用户 Token |
| Body | `{orderId}` |

#### 2.3.5 删除订单

| 项目 | 说明 |
|------|------|
| 接口名称 | 删除订单 |
| 请求方法 | POST |
| URL | `/order/delete` |
| 认证 | ✅ 前台用户 Token |
| Body | `{orderId}` |

#### 2.3.6 更新订单状态

| 项目 | 说明 |
|------|------|
| 接口名称 | 更新订单状态 |
| 请求方法 | POST |
| URL | `/order/updateStatus` |
| 功能描述 | 支付成功后回调更新订单状态 |
| 认证 | ✅ 前台用户 Token |
| Body | `{orderId, status}` |

---

### 2.4 后台订单管理

| 操作 | 方法 | URL | 认证 | 参数 |
|------|------|------|------|------|
| 订单分页 | POST | `/order/admin/list` | ✅ admin | `{pageNum, pageSize, orderSn, status, startTime, endTime}` |
| 订单详情 | GET | `/order/admin/detail` | ✅ admin | `?orderId=123` |
| 待发货列表 | GET | `/order/admin/neverDeliver` | ✅ admin | — |
| 发货 | POST | `/order/admin/deliver` | ✅ admin | `?orderId=123` |
| 仪表盘统计 | GET | `/order/admin/stats` | ✅ admin | `?startTime=&endTime=` (可选) |
| 销量Top5 | POST | `/order/admin/productSalesTop5` | ✅ admin | `{startTime, endTime}` |

#### 2.4.1 仪表盘统计响应

| 字段 | 类型 | 说明 |
|------|------|------|
| todaySales | BigDecimal | 今日销售额 |
| weekSales | BigDecimal | 本周销售额 |
| todayOrders | Long | 今日订单数 |
| pendingAfterSale | Long | 待审核售后数 |
| dailyStats | Array\<DailyStatsVo\> | 每日销售趋势 |
| dailyStats[].date | String | 日期 |
| dailyStats[].orderCount | Long | 订单数 |
| dailyStats[].sales | BigDecimal | 销售额 |
| topProducts | Array\<ProductSalesVo\> | 销量Top5 |
| topProducts[].productName | String | 商品名 |
| topProducts[].totalQuantity | Integer | 销售数量 |
| topProducts[].totalSales | BigDecimal | 销售额 |

---

### 2.5 支付

#### 2.5.1 创建支付交易

| 项目 | 说明 |
|------|------|
| 接口名称 | 创建支付交易 |
| 请求方法 | POST |
| URL | `/payment/transaction/create` |
| 功能描述 | 为订单创建支付交易记录（模拟支付流程，实际对接需微信/支付宝SDK） |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | ✅ | 订单ID | 123 |
| paymentMethod | Integer | ✅ | 1-微信 2-支付宝 | 1 |
| amount | BigDecimal | ✅ | 支付金额（须与订单实付一致） | 8999.00 |

#### 2.5.2 创建退款

| 项目 | 说明 |
|------|------|
| 接口名称 | 创建退款 |
| 请求方法 | POST |
| URL | `/payment/refund/create` |
| 功能描述 | 取消订单/售后审核通过后创建退款记录 |
| 认证 | ✅ 前台用户 Token |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| transactionId | Long | ✅ | 原支付交易ID |
| refundAmount | BigDecimal | ✅ | 退款金额 |
| reason | String | ✅ | 退款原因 |

---

### 2.6 物流

#### 2.6.1 用户端接口

| 操作 | 方法 | URL | 认证 | 参数 |
|------|------|------|------|------|
| 物流列表 | GET | `/oms/logistics/user/list` | ✅ user | — |
| 物流轨迹 | GET | `/oms/logistics/user/trace/list` | ✅ user | `?orderId=123` |
| 签收 | POST | `/oms/logistics/user/sign` | ✅ user | `?orderId=123` |
| 创建物流 | POST | `/oms/logistics/create` | ✅ user | `{orderId, deliveryCompany, deliveryNo}` |
| 添加轨迹 | POST | `/oms/logistics/user/addTrace` | ✅ user | `{orderId}`（支付成功自动调用） |
| 物流详情 | GET | `/oms/logistics/detail` | ✅ user | `?orderId=123` |

**物流详情响应 (data: LogisticsVo)**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 物流ID |
| orderId | Long | 订单ID |
| orderSn | String | 订单号 |
| deliveryCompany | String | 物流公司 |
| deliveryNo | String | 运单号 |
| status | Integer | 0-待发货 1-已发货 2-运输中 3-已签收 4-异常 |
| statusText | String | 状态文本 |
| receiver | String | 收件人 |
| phone | String | 电话 |
| address | String | 地址 |
| takeTime | String | 签收时间 |
| traces | Array\<TraceVo\> | 物流轨迹列表 |
| traces[].content | String | 轨迹描述 |
| traces[].createTime | String | 轨迹时间 |

#### 2.6.2 后台物流管理

| 操作 | 方法 | URL | 认证 | 参数 |
|------|------|------|------|------|
| 全部物流 | POST | `/oms/logistics/admin/listAll` | ✅ admin | `{orderSn, deliveryNo, deliveryCompany, status}` |
| 更新快递 | POST | `/oms/logistics/admin/updateDelivery` | ✅ admin | `{orderId, deliveryCompany}` |
| 更新状态 | POST | `/oms/logistics/admin/updateStatus` | ✅ admin | `{id, status}` |
| 添加轨迹 | POST | `/oms/logistics/admin/addTrace` | ✅ admin | `{logisticsId, content}` |
| 轨迹列表 | GET | `/oms/logistics/admin/trace/list` | ✅ admin | `?logisticsId=1` |
| 删除轨迹 | POST | `/oms/logistics/admin/trace/delete` | ✅ admin | `{id}` |

---

### 2.7 售后

#### 2.7.1 创建售后申请

| 项目 | 说明 |
|------|------|
| 接口名称 | 创建售后申请 |
| 请求方法 | POST |
| URL | `/oms/after-sale/create` |
| 认证 | ✅ 前台用户 Token |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | ✅ | 订单ID | 123 |
| orderItemId | Long | ✅ | 订单项ID | 10 |
| type | Integer | ✅ | 1-仅退款 2-退货退款 | 2 |
| reason | String | ✅ | 售后原因 | "商品与描述不符" |
| description | String | 否 | 详细说明 | "颜色发错了" |
| refundAmount | BigDecimal | ✅ | 退款金额 | 8999.00 |

#### 2.7.2 售后状态流转

```
用户申请(type=1仅退款/2退货退款, status=0待审核)
  → 管理员审核通过(type=1→status=1待退款/审核通过; type=2→status=1待退货)
    → type=1: 管理员退款(status=1→2已退款)
    → type=2: 用户退货(status=1→4用户已退货) → 管理员收货(status=4→5收货完成) → 退款(status=5→2)
  → 管理员驳回(status=0→3驳回, 附rejectReason)
```

#### 2.7.3 全部接口

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 用户售后列表 | POST | `/oms/after-sale/user/list` | ✅ user | `{pageNum, pageSize, afterSaleSn, status}` |
| 售后详情 | GET | `/oms/after-sale/detail` | ✅ user | `?id=1` |
| 用户退货 | POST | `/oms/after-sale/user/return` | ✅ user | `{afterSaleId, deliveryCompany, deliveryNo}` |
| 售后物流 | GET | `/oms/after-sale/delivery` | ✅ user | `?afterSaleId=1` |
| 后台列表 | POST | `/oms/after-sale/admin/list` | ✅ admin | `{pageNum, pageSize, afterSaleSn, status}` |
| 待审核 | GET | `/oms/after-sale/admin/pendingReview` | ✅ admin | — |
| 审核通过 | POST | `/oms/after-sale/admin/approve` | ✅ admin | `{id}` |
| 驳回 | POST | `/oms/after-sale/admin/reject` | ✅ admin | `{id, rejectReason}` |
| 退款 | POST | `/oms/after-sale/admin/refund` | ✅ admin | `{id}` |
| 确认收货 | POST | `/oms/after-sale/admin/receive` | ✅ admin | `{id}` |

---

## 模块三：AI 模块

### 概述

AI 模块基于 Spring AI 框架对接通义千问（qwen-turbo），提供智能客服、智能导购、AI文案生成、智能搜索等服务。RAG 知识库使用 `text-embedding-v2` 模型向量化，存储在内存 `SimpleVectorStore` 中（应用启动时从 DB 重建）。

**前置条件:** AI 接口均不需要认证（在白名单中），但智能导购和智能客服建议传递 userId 以获取用户上下文。

**超时时间:** AI 接口均为非流式（SSE），单次请求超时 60s，Token 消耗因 prompt 长度而异。

**涉及数据表:** `ai_knowledge_base`

---

### 3.1 AI 智能客服

| 项目 | 说明 |
|------|------|
| 接口名称 | AI 智能客服 |
| 请求方法 | POST |
| URL | `/ai/customer-service` |
| 功能描述 | 五级级联客服：订单查询 → 物流查询 → 售后查询 → RAG 知识库检索 → AI 通用兜底回答 |
| 认证 | ❌ 无需认证 |
| 流式 | ❌ 非流式（同步返回完整结果） |
| 超时 | 60s |
| Token 预估 | 每次请求约 500~2000 tokens（含 RAG prompt） |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| message | String | ✅ | 用户问题 | "我的订单什么时候发货" |
| userId | Long | 否 | 用户ID（用于查询订单/物流/售后上下文） | 1 |

**响应 (data: String)** — AI 生成的回答文本，可能包含 Markdown 格式化内容。

**五级级联处理逻辑:**

| 级 | 处理 | 触发条件 | 响应来源 |
|:--:|------|----------|----------|
| 1 | 订单查询 | 问题匹配订单关键词+用户有订单 | 数据库订单信息 |
| 2 | 物流查询 | 问题匹配物流关键词+订单已发货 | 数据库物流信息 |
| 3 | 售后处理 | 问题匹配售后关键词+有售后记录 | 数据库售后信息 |
| 4 | RAG 检索 | 知识库有匹配文档 | Top-3 知识库文档 |
| 5 | AI 兜底 | 以上均不命中 | qwen-turbo 通用回答 |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/ai/customer-service \
  -H "Content-Type: application/json" \
  -d '{"message":"我的订单什么时候到货","userId":1}'
```

---

### 3.2 AI 智能导购

| 项目 | 说明 |
|------|------|
| 接口名称 | AI 智能导购 |
| 请求方法 | POST |
| URL | `/ai/shop-guide` |
| 功能描述 | 基于用户自然语言需求智能推荐商品，多维度加权评分（关键词×品类×价格×时效），无匹配时 Fallback 至 LLM 生成导购建议 |
| 认证 | ❌ 无需认证 |
| 流式 | ❌ 非流式 |
| 超时 | 60s |
| 响应时间 | <50ms（纯关键词匹配场景） |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| message | String | ✅ | 用户购物需求描述 | "想买一个2000元左右的拍照手机" |
| userId | String | 否 | 用户ID | "1" |

**响应 (data: ShopGuideResponse)**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| replyMessage | String | AI 导购回复文本 | "为您找到以下适合拍照的手机..." |
| totalCount | Integer | 找到的商品数量 | 5 |
| intentAnalysis | String | 用户意图分析 | "偏好:拍照,预算:1500-2500,品类:手机" |
| searchSummary | String | 搜索结果总结 | "匹配度前5商品" |
| products | Array\<ShopGuideProductVo\> | 推荐商品列表（最多10个） | — |
| products[].id | Long | 商品ID | 100 |
| products[].productName | String | 商品名称 | "OPPO Reno 12" |
| products[].categoryName | String | 分类名称 | "智能手机" |
| products[].price | BigDecimal | 价格 | 2299.00 |
| products[].pic | String | 商品图片 | "https://..." |
| products[].shortDesc | String | 简短描述 | "5000万像素" |
| products[].isHot | Integer | 是否热门 | 0 |
| products[].matchReason | String | 匹配原因（AI生成） | "符合您2000元预算+拍照需求" |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/ai/shop-guide \
  -H "Content-Type: application/json" \
  -d '{"message":"帮我推荐一款适合学生用的轻薄笔记本，预算5000以内"}'
```

---

### 3.3 AI 文案生成

| 项目 | 说明 |
|------|------|
| 接口名称 | AI 生成商品文案 |
| 请求方法 | POST |
| URL | `/ai/generate-goods-desc` |
| 功能描述 | 调用通义千问生成商品标题、详情页HTML、营销话术、SEO关键词，支持6种风格×4种文案类型 |
| 认证 | ❌ 无需认证（但建议后台使用） |
| 流式 | ❌ 非流式 |
| 超时 | 90s（生成内容较长） |
| Token 预估 | 每次约 800~3000 tokens |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| productName | String | ✅ | @NotBlank | 商品名称 | "iPhone 15 Pro Max 256G" |
| categoryId | Long | ✅ | @NotNull | 分类ID | 1001 |
| categoryName | String | 否 | — | 分类名称（建议传） | "智能手机" |
| price | BigDecimal | ✅ | @NotNull | 售价 | 8999.00 |
| originalPrice | BigDecimal | 否 | — | 原价 | 9999.00 |
| sellingPoints | String | 否 | — | 核心卖点（逗号分隔） | "A17芯片,钛金属,4800万像素" |
| spec | String | 否 | — | 规格参数 | "屏幕:6.7英寸,存储:256G" |
| targetUser | String | 否 | — | 适用人群 | "商务人士,摄影爱好者" |
| brand | String | 否 | — | 品牌 | "Apple" |
| tags | String | 否 | — | 商品标签（逗号分隔） | "新品,旗舰机,5G" |
| stock | Integer | 否 | — | 库存 | 100 |
| pic | String | 否 | — | 商品图片URL | "https://oss.xxx.com/pic.jpg" |
| descType | String | 否 | 枚举见下表 | 文案类型 | "ALL" |
| style | String | 否 | 枚举见下表 | 文案风格 | "高端" |
| platform | String | 否 | — | 目标平台 | "淘宝" |
| extraRequirement | String | 否 | — | 额外需求 | "突出性价比和学生优惠" |

**descType 枚举:**

| 值 | 含义 |
|------|------|
| TITLE | 仅生成商品标题 |
| DETAIL | 仅生成详情页HTML |
| MARKETING | 仅生成营销话术 |
| SEO | 仅生成SEO关键词 |
| ALL | 生成全部（默认） |

**style 枚举:**

| 值 | 含义 |
|------|------|
| 专业 | 专业化、技术流 |
| 活泼 | 轻松活泼、年轻化 |
| 简洁 | 简洁明了、核心突出 |
| 高端 | 高端大气、品质感 |
| 接地气 | 口语化、接地气 |
| 种草 | 种草文案、小红书风 |

**响应 (data: GoodsDescResponse)**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| title | String | 商品标题 | "【新品】Apple iPhone 15 Pro Max 256G 黑色..." |
| subtitle | String | 副标题 | "A17 Pro 芯片 | 钛金属设计 | 4800万像素" |
| detailHtml | String | 详情页HTML | "\<div\>...\<\/div\>" |
| shortDesc | String | 简短描述 | "A17 Pro芯片+钛金属边框" |
| marketingCopy | String | 营销话术 | "限时特惠！下单立减..." |
| keywords | List\<String\> | SEO关键词 | ["iPhone15","5G手机","旗舰机"] |
| tags | List\<String\> | 商品标签 | ["新品","爆款","旗舰机","5G手机"] |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/ai/generate-goods-desc \
  -H "Content-Type: application/json" \
  -d '{
    "productName":"iPhone 15 Pro Max 256G 黑色",
    "categoryId":1001,
    "categoryName":"智能手机",
    "price":8999.00,
    "originalPrice":9999.00,
    "sellingPoints":"A17Pro芯片,钛金属边框,4800万像素,超长续航",
    "brand":"Apple",
    "style":"高端",
    "descType":"ALL",
    "platform":"淘宝"
  }'
```

---

### 3.4 AI 智能搜索

| 项目 | 说明 |
|------|------|
| 接口名称 | AI 智能搜索 |
| 请求方法 | POST |
| URL | `/ai/intelligent-search` |
| 功能描述 | 对用户输入进行拼写纠错+同义词扩展+相关推荐，辅助 ES 搜索 |
| 认证 | ❌ 无需认证 |
| 超时 | 30s |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | ✅ | 用户原始搜索词 | "苹手"（拼写错误） |

**响应 (data: SmartSearchResponse)**

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| original | String | 原始输入 | "苹手" |
| corrected | String | 纠错结果 | "苹果手机" |
| synonyms | List\<String\> | 同义词扩展 | ["iPhone","Apple手机","智能手机"] |
| related | List\<String\> | 相关推荐词 | ["平板电脑","智能手表","手机壳"] |

**cURL 示例**

```bash
curl -X POST http://localhost:8080/ai/intelligent-search \
  -H "Content-Type: application/json" \
  -d '{"keyword":"苹手"}'
```

---

### 3.5 AI 通用对话

| 项目 | 说明 |
|------|------|
| 接口名称 | AI 通用对话 |
| 请求方法 | POST |
| URL | `/ai/chat` |
| 功能描述 | 直接调用 qwen-turbo 通用对话，无 RAG 增强 |
| 认证 | ❌ 无需认证 |

**请求参数 (Body JSON)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| message | String | ✅ | 用户消息 |
| userId | Long | 否 | 用户ID |

**响应 (data: String)** — AI 回复文本。

---

### 3.6 RAG 知识库检索

| 项目 | 说明 |
|------|------|
| 接口名称 | 知识库检索增强问答 |
| 请求方法 | POST |
| URL | `/ai/knowledge-search` |
| 功能描述 | 基于用户问题检索知识库 Top-3 相似文档，增强 Prompt 后调用 LLM 回答 |
| 认证 | ❌ 无需认证 |

**请求参数:** 同 [3.5 AI通用对话](#35-ai-通用对话)

---

### 3.7 知识库管理（后台）

#### 3.7.1 知识库 CRUD

| 操作 | 方法 | URL | 认证 | 关键参数 |
|------|------|------|------|----------|
| 添加 | POST | `/ai/knowledge/add` | — | `{title, content, type, keywords, status}` |
| 更新 | PUT | `/ai/knowledge/update/{id}` | — | 同上 |
| 详情 | GET | `/ai/knowledge/detail/{id}` | — | — |
| 启用列表 | GET | `/ai/knowledge/list` | — | — |
| 按类型查 | GET | `/ai/knowledge/list/type/{type}` | — | type 见枚举表 |
| 搜索 | GET | `/ai/knowledge/search` | — | `?keyword=售后` |
| 分页列表 | GET | `/ai/knowledge/admin/list` | — | `?pageNum=1&pageSize=10` |
| 删除 | DELETE | `/ai/knowledge/delete/{id}` | — | — |
| 匹配 | POST | `/ai/knowledge/match` | — | Body: raw string `"退货流程"` |

**知识库实体字段:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| title | String | 标题 |
| content | String | 内容（用于向量检索和 Prompt 增强） |
| type | String | 类型枚举：goods/after_sale/service/payment/order/logistics/promotion |
| keywords | String | 关键词 |
| status | Integer | 0-禁用 1-启用（禁用不参与向量检索） |
| createTime | String | 创建时间 |
| updateTime | String | 更新时间 |

#### 3.7.2 刷新向量索引

| 项目 | 说明 |
|------|------|
| 接口名称 | 刷新知识库向量索引 |
| 请求方法 | POST |
| URL | `/ai/knowledge-refresh` |
| 功能描述 | 从 DB 重新加载所有启用的知识库条目 → 向量化 → 写入内存 VectorStore。用于知识库数据变更后重建索引 |
| 认证 | — |

---

## 附录 A: 通用错误码

| code | msg | 说明 |
|:---:|------|------|
| 200 | 操作成功 | 正常响应 |
| 400 | 参数错误 | `@Valid` 校验失败 / `IllegalArgumentException` |
| 401 | 未登录或Token已过期 | JWT 解析失败 / Redis 中 Token 不存在 |
| 403 | 无权限 | `@PreAuthorize` 鉴权不通过 |
| 500 | （动态消息） | 业务异常 / 服务器内部错误 |

## 附录 B: 时间格式约定

所有时间字段统一使用以下格式：

```
yyyy-MM-dd HH:mm:ss
```

示例: `"2026-06-26 15:30:00"`

响应中的时间字段由 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 序列化。

## 附录 C: 金额字段约定

所有金额字段使用 `BigDecimal` 类型，JSON 序列化为数字（非字符串），保留两位小数：

```json
{"price": 8999.00, "totalAmount": 17998.00}
```

前端应使用 `json-bigint` 或等效方案处理精度。
