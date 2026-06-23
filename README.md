# SmartShop 智慧电商系统

一个完整的 B2C 电商平台，包含用户端商城与管理后台，集成 AI 智能导购、客服与 RAG 知识库。

## 线上体验

| 端     | 地址                             | 用户名   | 密码   |
| ------ | -------------------------------- | -------- | ------ |
| 用户端 | http://8.163.124.200             | user001  | 123456 |
| 管理端 | http://8.163.124.200/admin/login | admin001 | 123456 |

## 技术栈

### 后端（e_commerce）

| 技术                 | 说明                                         |
| -------------------- | -------------------------------------------- |
| Spring Boot 3.2.0    | Java 17 基础框架                             |
| MyBatis-Plus 3.5.7   | MySQL ORM + 分页                             |
| Spring Security 6    | 无状态 JWT 认证 + RBAC 权限                  |
| Redis                | Token 存储、验证码、密码版本控制             |
| Elasticsearch        | 商品全文搜索                                 |
| Spring AI + 通义千问 | 智能导购、智能客服、RAG 知识库、商品文案生成 |
| Aliyun OSS           | 图片上传与存储                               |
| SpringDoc OpenAPI    | Swagger UI 接口文档                          |

### 前端（e_commerce_front）

| 技术         | 说明                         |
| ------------ | ---------------------------- |
| Vue 3        | 前端框架                     |
| Vite 8       | 构建工具                     |
| Element Plus | UI 组件库                    |
| Pinia        | 状态管理（持久化）           |
| Axios        | HTTP 请求（前台/后台双实例） |
| ECharts      | 后台数据图表                 |

## 功能模块

### 用户端

- 用户注册/登录、个人信息管理、收货地址
- 商品浏览（分类、搜索、详情）、购物车、收藏、足迹
- 订单流程：确认 → 支付 → 物流跟踪 → 确认收货
- 售后申请与进度查询
- 商品评价与回复
- AI 智能导购、智能客服、智能搜索

### 管理后台

- 控制台仪表板（ECharts 数据概览）
- 商品管理（列表、分类、规格、SKU）、库存预警
- 订单管理、售后处理、物流管理
- 用户与管理员的 RBAC 权限体系
- 轮播图管理、通知公告
- 操作日志审计
- AI 知识库管理（RAG）

## 项目结构

```
SmartShop 智慧电商系统/
├── e_commerce/                # Spring Boot 后端
│   └── src/main/java/com/e_commerce/
│       ├── module/user/       # 用户模块
│       ├── module/product/    # 商品模块
│       ├── module/oms/        # 订单模块
│       ├── module/payment/    # 支付模块
│       ├── module/marketing/  # 营销模块
│       ├── module/comment/    # 评价模块
│       ├── module/notice/     # 通知模块
│       ├── module/system/     # 后台管理模块
│       ├── module/ai/         # AI/RAG 模块
│       ├── module/search/     # 搜索历史模块
│       ├── module/operlog/    # 操作日志模块
│       ├── common/            # 公共组件
│       └── security/          # 安全配置
├── e_commerce_front/          # Vue 3 前端
│   └── src/
│       ├── views/user/        # 前台页面
│       ├── views/admin/       # 后台页面
│       ├── api/               # API 接口层
│       ├── router/            # 路由配置
│       ├── stores/            # Pinia 状态
│       └── utils/             # Axios 封装
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis
- Elasticsearch 7.x+

### 后端启动

1. 导入 `e_commerce/src/main/resources/sql/` 下的 SQL 脚本初始化数据库
2. 修改 `application.yml` 中的数据库、Redis、ES 连接信息
3. 配置阿里云 OSS 及通义千问 API Key

```bash
cd e_commerce
mvn spring-boot:run
```

服务启动后访问 http://localhost:8080

### 前端启动

```bash
cd e_commerce_front
npm install
npm run dev
```

开发服务器默认运行在 http://localhost:5173，API 请求自动代理到后端 8080 端口。
