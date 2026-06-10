-- 创建数据库
CREATE DATABASE IF NOT EXISTS e_commerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE e_commerce;

-- =============================================
-- 一、系统权限模块
-- =============================================

-- 后台管理员账号
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    password_version INT DEFAULT 0 COMMENT '密码版本号'
) COMMENT '后台管理员账号表';

-- 角色信息
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '角色信息表';

INSERT INTO sys_role (role_name, description) VALUES
('超级管理员', '超级管理员'),
('订单管理员', '管理订单'),
('物流管理员', '管理物流'),
('商品管理员', '管理商品'),
('售后管理员', '管理售后');

-- 菜单/权限资源
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示根菜单',
    path VARCHAR(200) COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序号',
    type TINYINT NOT NULL COMMENT '类型：1-菜单，2-按钮',
    INDEX idx_parent_id (parent_id)
) COMMENT '菜单权限资源表';

INSERT INTO sys_menu (id, menu_name, parent_id, path, icon, sort, type) VALUES
(1, '控制台', 0, 'home', 'house', 1, 1),
(2, '消息通知', 0, 'notice', 'message', 2, 1),
(3, '订单管理', 0, 'order', 'document', 3, 1),
(4, '售后管理', 0, 'after-sale', 'edit', 4, 1),
(5, '商品管理', 0, 'product', 'box', 5, 1),
(6, '商品列表', 5, 'list', 'document', 1, 1),
(7, '商品分类', 5, 'category', 'collection', 2, 1),
(8, '商品规格', 5, 'attr', 'operation', 3, 1),
(9, 'sku管理', 5, 'sku', 'goodsfilled', 4, 1),
(10, '权限管理', 0, 'admin', 'userfilled', 7, 1),
(11, '角色管理', 10, 'role', 'userfilled', 2, 1),
(12, '菜单管理', 10, 'menu', 'menu', 3, 1),
(13, '轮播图管理', 0, 'carousel', 'picture', 7, 1),
(14, '修改密码', 0, 'update-password', 'lock', 8, 1),
(15, '日志记录', 0, 'log', 'clock', 9, 1),
(16, '物流管理', 0, 'logistics', 'van', 5, 1),
(17, '管理员管理', 10, 'admin', 'user', 1, 1);

-- 角色-菜单权限关联
CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) COMMENT '角色菜单权限关联表';

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13),
(1, 14),
(1, 15),
(1, 16),
(1, 17);

-- 用户-角色关联
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) COMMENT '用户角色关联表';

-- 操作日志
CREATE TABLE sys_oper_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    operation VARCHAR(50) COMMENT '操作内容',
    method VARCHAR(100) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id)
) COMMENT '操作日志表';

-- =============================================
-- 二、前台用户模块
-- =============================================

-- 前台普通用户
CREATE TABLE ums_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    avatar VARCHAR(255) COMMENT '头像地址',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    password_version INT DEFAULT 0 COMMENT '密码版本号',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额（元）'
) COMMENT '前台用户表';

-- 用户收货地址
CREATE TABLE ums_address (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver VARCHAR(50) NOT NULL COMMENT '收货人',
    phone VARCHAR(20) NOT NULL COMMENT '收货电话',
    province VARCHAR(20) NOT NULL COMMENT '省份',
    city VARCHAR(20) NOT NULL COMMENT '城市',
    area VARCHAR(20) NOT NULL COMMENT '区县',
    detail VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
    INDEX idx_user_id (user_id)
) COMMENT '用户收货地址表';

-- =============================================
-- 三、商品模块（SPU + SKU + 规格）
-- =============================================

-- 商品分类
CREATE TABLE pms_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示根分类',
    level INT NOT NULL COMMENT '分类层级',
    sort INT DEFAULT 0 COMMENT '排序号',
    icon VARCHAR(100) COMMENT '分类图标',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id)
) COMMENT '商品分类表';

-- 商品信息（SPU）
CREATE TABLE pms_product (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    pic VARCHAR(255) COMMENT '商品主图',
    detail_html TEXT COMMENT '商品详情富文本',
    status TINYINT DEFAULT 1 COMMENT '上下架状态：0-下架，1-上架',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门：0-否 1-是',
    hot_sort INT DEFAULT 0 COMMENT '热门排序，数字越小越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category_id (category_id)
) COMMENT '商品信息表';

-- 商品SKU表
CREATE TABLE pms_sku (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_code VARCHAR(100) NOT NULL COMMENT 'SKU编码（唯一）',
    price DECIMAL(10,2) NOT NULL COMMENT '销售价',
    cost_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本价',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前库存',
    stock_warning INT DEFAULT 0 COMMENT '库存预警值',
    pic VARCHAR(255) COMMENT 'SKU图片',
    weight DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品重量(kg)',
    volume DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品体积(m³)',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_sku_code (sku_code),
    INDEX idx_product_id (product_id)
) COMMENT '商品SKU信息表';

-- 规格名称表
CREATE TABLE pms_attribute (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '商品分类ID',
    attr_name VARCHAR(50) NOT NULL COMMENT '规格名称（如颜色）',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category_id (category_id)
) COMMENT '商品规格名称表';

-- 规格值表
CREATE TABLE pms_attribute_value (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    attr_id BIGINT NOT NULL COMMENT '规格名称ID',
    attr_value VARCHAR(50) NOT NULL COMMENT '规格值（如红色）',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_attr_id (attr_id)
) COMMENT '商品规格值表';

-- SKU规格关联表
CREATE TABLE pms_sku_attr (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    attr_id BIGINT NOT NULL COMMENT '规格名称ID',
    attr_value_id BIGINT NOT NULL COMMENT '规格值ID',
    attr_name VARCHAR(50) COMMENT '冗余存储规格名',
    attr_value VARCHAR(50) COMMENT '冗余存储规格值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_sku_attr (sku_id, attr_id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_product_id (product_id)
) COMMENT 'SKU规格关联表';

-- 商品收藏
CREATE TABLE pms_product_collect (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_product (user_id, product_id)
) COMMENT '商品收藏表';

-- 商品浏览足迹
CREATE TABLE pms_product_browse (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) COMMENT '商品浏览记录表';

-- =============================================
-- 四、购物车 & 库存锁定
-- =============================================

-- 购物车
CREATE TABLE oms_cart (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU_ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    INDEX idx_user_id (user_id)
) COMMENT '购物车表';

-- SKU库存锁定表（防超卖）
CREATE TABLE pms_sku_stock_lock (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_sn VARCHAR(64) NOT NULL COMMENT '订单编号',
    sku_id BIGINT NOT NULL COMMENT 'SKU_ID',
    lock_num INT NOT NULL COMMENT '锁定库存数量',
    status TINYINT DEFAULT 0 COMMENT '状态：0-锁定 1-已扣减 2-已释放',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_sn (order_sn),
    INDEX idx_sku_id (sku_id)
) COMMENT 'SKU库存锁定表(防超卖)';

-- =============================================
-- 五、订单模块
-- =============================================

-- 订单主表
CREATE TABLE oms_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_sn VARCHAR(64) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    status TINYINT NOT NULL COMMENT '订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消',
    receiver VARCHAR(50) NOT NULL COMMENT '收货人',
    phone VARCHAR(20) NOT NULL COMMENT '收货电话',
    address VARCHAR(255) NOT NULL COMMENT '收货地址',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    confirm_time DATETIME COMMENT '确认收货时间',
    comment_time DATETIME COMMENT '评价时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_sn (order_sn)
) COMMENT '订单主表';

-- 订单商品明细表
CREATE TABLE oms_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_sn VARCHAR(64) NOT NULL COMMENT '订单编号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU_ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    sku_specs VARCHAR(255) COMMENT 'SKU规格描述',
    pic VARCHAR(255) COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计',
    INDEX idx_order_id (order_id),
    INDEX idx_sku_id (sku_id)
) COMMENT '订单商品明细表';

-- 取消订单记录表
CREATE TABLE oms_order_cancel (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_sn VARCHAR(64) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',

    cancel_reason VARCHAR(255) NOT NULL COMMENT '取消原因',
    cancel_description VARCHAR(512) COMMENT '取消详细描述（选填）',

    cancel_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '取消时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_sn (order_sn)
) COMMENT '订单取消记录表';

-- 结算单主表
CREATE TABLE oms_settle (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    address_id BIGINT COMMENT '收货地址ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待确认 1-已转订单 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '结算单（临时订单）表';

-- 结算单明细表
CREATE TABLE oms_settle_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    settle_id BIGINT NOT NULL COMMENT '结算单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU_ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    sku_specs VARCHAR(255) COMMENT '规格',
    pic VARCHAR(255) COMMENT '图片',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计'
) COMMENT '结算单明细表';

-- =============================================
-- 六、支付 & 退款模块
-- =============================================

-- 支付交易记录表
CREATE TABLE payment_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    transaction_no VARCHAR(100) NOT NULL UNIQUE COMMENT '交易编号',
    payment_method TINYINT NOT NULL COMMENT '支付方式：1-支付宝，2-微信支付',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status TINYINT NOT NULL COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    pay_time DATETIME COMMENT '支付时间',
    callback_time DATETIME COMMENT '回调时间',
    callback_data TEXT COMMENT '回调数据',
    INDEX idx_order_id (order_id),
    INDEX idx_transaction_no (transaction_no)
) COMMENT '支付交易记录表';

-- 退款记录表
CREATE TABLE payment_refund (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    transaction_id BIGINT NOT NULL COMMENT '交易ID',
    refund_no VARCHAR(100) NOT NULL UNIQUE COMMENT '退款编号',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    status TINYINT NOT NULL COMMENT '退款状态：0-申请中，1-退款成功，2-退款失败',
    reason VARCHAR(255) COMMENT '退款原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    refund_time DATETIME COMMENT '退款时间',
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_refund_no (refund_no)
) COMMENT '退款记录表';

-- =============================================
-- 七、物流模块
-- =============================================

-- 订单物流信息表
CREATE TABLE oms_logistics (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_sn VARCHAR(64) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    delivery_company VARCHAR(50) COMMENT '物流公司',
    delivery_no VARCHAR(50) COMMENT '物流单号',
    status TINYINT DEFAULT 0 COMMENT '物流状态：0-待发货 1-已发货 2-运输中 3-已签收 4-异常',
    receiver VARCHAR(50) COMMENT '收货人',
    phone VARCHAR(20) COMMENT '收货电话',
    address VARCHAR(255) COMMENT '收货地址',
    take_time DATETIME COMMENT '签收时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_delivery_no (delivery_no)
) COMMENT '订单物流信息表';

-- 物流轨迹详情表
CREATE TABLE oms_logistics_trace (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    logistics_id BIGINT NOT NULL COMMENT '物流ID',
    content VARCHAR(255) NOT NULL COMMENT '物流描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    INDEX idx_logistics_id (logistics_id)
) COMMENT '物流轨迹详情表';

-- =============================================
-- 八、售后模块
-- =============================================

-- 售后申请表
CREATE TABLE oms_after_sale (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_item_id BIGINT NOT NULL COMMENT '订单商品项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    after_sale_sn VARCHAR(64) NOT NULL UNIQUE COMMENT '售后单号',
    type TINYINT NOT NULL COMMENT '售后类型：1-仅退款 2-退货退款',
    reason VARCHAR(255) NOT NULL COMMENT '退款原因',
    description TEXT COMMENT '问题描述',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-审核通过 2-已退款 3-驳回 4-用户已退货 5-商家收货完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    audit_time DATETIME COMMENT '审核时间',
    refund_time DATETIME COMMENT '退款完成时间',
    reject_reason VARCHAR(255) COMMENT '驳回原因',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_after_sale_sn (after_sale_sn)
) COMMENT '售后申请表';

-- 退货物流信息表
CREATE TABLE oms_after_sale_delivery (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    after_sale_id BIGINT NOT NULL COMMENT '售后ID',
    delivery_company VARCHAR(50) COMMENT '退货物流公司',
    delivery_no VARCHAR(50) COMMENT '退货物流单号',
    send_time DATETIME COMMENT '用户退货发货时间',
    receive_time DATETIME COMMENT '商家收货时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未发货 1-已发货 2-已签收',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_after_sale_id (after_sale_id)
) COMMENT '退货物流信息表';

-- =============================================
-- 九、评论模块
-- =============================================

-- 商品评论表
CREATE TABLE product_comment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_item_id BIGINT NOT NULL COMMENT '订单商品项ID',  -- 保留，逻辑关联
    content TEXT NOT NULL COMMENT '评论内容',
    score TINYINT NOT NULL COMMENT '评分：1-5星',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_item_id (order_item_id)   -- 加索引，不加强制外键
) COMMENT '商品评论表';

-- 评论回复表
CREATE TABLE comment_reply (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    reply_content TEXT NOT NULL COMMENT '回复内容',
    reply_type TINYINT NOT NULL COMMENT '回复类型：1-用户回复，2-商家回复',
    reply_user_id BIGINT NOT NULL COMMENT '回复用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_comment_id (comment_id)
) COMMENT '评论回复表';

-- =============================================
-- 十、营销模块
-- =============================================

-- 首页轮播图表
CREATE TABLE sms_home_carousel (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    pic VARCHAR(255) NOT NULL COMMENT '轮播图片地址',
    url VARCHAR(255) COMMENT '跳转链接',
    sort INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用'
) COMMENT '首页轮播图表';

-- =============================================
-- 十一、用户消息通知表
-- =============================================
-- 消息类型（最简）
DROP TABLE IF EXISTS sys_notice_type;
CREATE TABLE sys_notice_type (
    id TINYINT NOT NULL PRIMARY KEY COMMENT '类型ID',
    type_name VARCHAR(50) NOT NULL COMMENT '类型名称',
    module VARCHAR(20) NOT NULL COMMENT 'admin-管理端'
) COMMENT '消息类型字典表';
-- 消息类型（详细）
INSERT INTO sys_notice_type (id, type_name, module) VALUES
(1, '新订单通知', 'admin'),
(2, '退款申请通知', 'admin'),
(3, '售后申请通知', 'admin'),
(4, '系统公告', 'admin'),
(5, '库存预警', 'admin'),
(6, '发货通知', 'user'),
(7, '下单成功', 'user'),
(8, '运输途中', 'user'),
(9, '签收成功', 'user'),
(10, '售后审核通知', 'user'),
(11, '退款到账', 'user');

-- 通知主体（发给角色）
CREATE TABLE ums_user_notice (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notice_type TINYINT NOT NULL COMMENT '消息类型ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content VARCHAR(500) NOT NULL COMMENT '内容',
    biz_id VARCHAR(64) COMMENT '关联业务单号',
    is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) COMMENT '用户消息通知表';

DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    notice_type TINYINT NOT NULL COMMENT '消息类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content VARCHAR(500) NOT NULL COMMENT '内容',
    biz_id VARCHAR(64) COMMENT '业务单号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_type(notice_type),
    INDEX idx_create_time(create_time)
) COMMENT '系统通知表';

-- 通知 <-> 角色 关联（核心！RBAC 消息必须用）
DROP TABLE IF EXISTS sys_notice_role;
CREATE TABLE sys_notice_role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_notice_role(notice_id, role_id),

    INDEX idx_notice_id(notice_id),
    INDEX idx_role_id(role_id)
) COMMENT '通知-角色关联表';

-- 已读记录（谁读过）
DROP TABLE IF EXISTS sys_admin_notice_read;
CREATE TABLE sys_admin_notice_read (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY uk_admin_notice(admin_id, notice_id),

    INDEX idx_admin_id(admin_id),
    INDEX idx_notice_id(notice_id)
) COMMENT '管理员已读记录表';

-- =============================================
-- 十二、AI知识库
-- =============================================

-- AI知识库表
CREATE TABLE ai_knowledge_base (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '规则标题',
    content TEXT NOT NULL COMMENT '规则内容',
    type VARCHAR(20) NOT NULL COMMENT '规则类型：goods-商品规则，after_sale-售后规则',
    keywords VARCHAR(255) COMMENT '关键词（逗号分隔）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_status (status)
) COMMENT 'AI知识库表';

-- =============================================
-- 十三、用户搜索历史表
-- =============================================
CREATE TABLE ums_user_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(255) NOT NULL COMMENT '搜索关键词',
    search_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    INDEX idx_user_id (user_id)
) COMMENT '用户搜索历史表';

-- =============================================
-- 添加外键约束（在所有基础表创建完成后）
-- =============================================

-- 重新添加之前移除的外键约束
ALTER TABLE pms_product ADD CONSTRAINT fk_pms_product_category FOREIGN KEY (category_id) REFERENCES pms_category(id);
ALTER TABLE pms_sku ADD CONSTRAINT fk_pms_sku_product FOREIGN KEY (product_id) REFERENCES pms_product(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE pms_attribute ADD CONSTRAINT fk_pms_attribute_category FOREIGN KEY (category_id) REFERENCES pms_category(id);
ALTER TABLE pms_attribute_value ADD CONSTRAINT fk_pms_attribute_value_attr FOREIGN KEY (attr_id) REFERENCES pms_attribute(id);
ALTER TABLE pms_sku_attr ADD CONSTRAINT fk_pms_sku_attr_sku FOREIGN KEY (sku_id) REFERENCES pms_sku(id);
ALTER TABLE pms_sku_attr ADD CONSTRAINT fk_pms_sku_attr_product FOREIGN KEY (product_id) REFERENCES pms_product(id);
ALTER TABLE pms_sku_attr ADD CONSTRAINT fk_pms_sku_attr_attr FOREIGN KEY (attr_id) REFERENCES pms_attribute(id);
ALTER TABLE pms_sku_attr ADD CONSTRAINT fk_pms_sku_attr_attr_value FOREIGN KEY (attr_value_id) REFERENCES pms_attribute_value(id);
ALTER TABLE pms_product_collect ADD CONSTRAINT fk_pms_product_collect_user FOREIGN KEY (user_id) REFERENCES ums_user(id) ON DELETE CASCADE;
ALTER TABLE pms_product_collect ADD CONSTRAINT fk_pms_product_collect_product FOREIGN KEY (product_id) REFERENCES pms_product(id) ON DELETE CASCADE;
ALTER TABLE pms_product_browse ADD CONSTRAINT fk_pms_product_browse_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE pms_product_browse ADD CONSTRAINT fk_pms_product_browse_product FOREIGN KEY (product_id) REFERENCES pms_product(id);
ALTER TABLE oms_cart ADD CONSTRAINT fk_oms_cart_user FOREIGN KEY (user_id) REFERENCES ums_user(id) ON DELETE CASCADE;
ALTER TABLE oms_cart ADD CONSTRAINT fk_oms_cart_product FOREIGN KEY (product_id) REFERENCES pms_product(id);
ALTER TABLE oms_cart ADD CONSTRAINT fk_oms_cart_sku FOREIGN KEY (sku_id) REFERENCES pms_sku(id);
ALTER TABLE oms_order ADD CONSTRAINT fk_oms_order_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE oms_order_item ADD CONSTRAINT fk_oms_order_item_order FOREIGN KEY (order_id) REFERENCES oms_order(id) ON DELETE CASCADE;
ALTER TABLE oms_order_item ADD CONSTRAINT fk_oms_order_item_sku FOREIGN KEY (sku_id) REFERENCES pms_sku(id);
ALTER TABLE oms_order_cancel ADD CONSTRAINT fk_oms_order_cancel_order FOREIGN KEY (order_id) REFERENCES oms_order(id);
ALTER TABLE oms_order_cancel ADD CONSTRAINT fk_oms_order_cancel_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE oms_settle ADD CONSTRAINT fk_oms_settle_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE oms_settle_item ADD CONSTRAINT fk_oms_settle_item_settle FOREIGN KEY (settle_id) REFERENCES oms_settle(id);
ALTER TABLE oms_settle_item ADD CONSTRAINT fk_oms_settle_item_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE oms_settle_item ADD CONSTRAINT fk_oms_settle_item_product FOREIGN KEY (product_id) REFERENCES pms_product(id);
ALTER TABLE oms_settle_item ADD CONSTRAINT fk_oms_settle_item_sku FOREIGN KEY (sku_id) REFERENCES pms_sku(id);
ALTER TABLE payment_transaction ADD CONSTRAINT fk_payment_transaction_order FOREIGN KEY (order_id) REFERENCES oms_order(id) ON DELETE CASCADE;
ALTER TABLE payment_refund ADD CONSTRAINT fk_payment_refund_transaction FOREIGN KEY (transaction_id) REFERENCES payment_transaction(id) ON DELETE CASCADE;
ALTER TABLE oms_logistics ADD CONSTRAINT fk_oms_logistics_order FOREIGN KEY (order_id) REFERENCES oms_order(id) ON DELETE CASCADE;
ALTER TABLE oms_logistics_trace ADD CONSTRAINT fk_oms_logistics_trace_logistics FOREIGN KEY (logistics_id) REFERENCES oms_logistics(id) ON DELETE CASCADE;
ALTER TABLE oms_after_sale ADD CONSTRAINT fk_oms_after_sale_order FOREIGN KEY (order_id) REFERENCES oms_order(id) ON DELETE CASCADE;
ALTER TABLE oms_after_sale ADD CONSTRAINT fk_oms_after_sale_order_item FOREIGN KEY (order_item_id) REFERENCES oms_order_item(id) ON DELETE CASCADE;
ALTER TABLE oms_after_sale_delivery ADD CONSTRAINT fk_oms_after_sale_delivery_after_sale FOREIGN KEY (after_sale_id) REFERENCES oms_after_sale(id) ON DELETE CASCADE;
ALTER TABLE product_comment ADD CONSTRAINT fk_product_comment_product FOREIGN KEY (product_id) REFERENCES pms_product(id);
ALTER TABLE product_comment ADD CONSTRAINT fk_product_comment_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE comment_reply ADD CONSTRAINT fk_comment_reply_comment FOREIGN KEY (comment_id) REFERENCES product_comment(id);
ALTER TABLE ums_user_notice ADD CONSTRAINT fk_ums_user_notice_user FOREIGN KEY (user_id) REFERENCES ums_user(id);
ALTER TABLE sys_notice_role ADD CONSTRAINT fk_sys_notice_role_notice FOREIGN KEY (notice_id) REFERENCES sys_notice(id);
ALTER TABLE sys_notice_role ADD CONSTRAINT fk_sys_notice_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id);
ALTER TABLE sys_admin_notice_read ADD CONSTRAINT fk_sys_admin_notice_read_admin FOREIGN KEY (admin_id) REFERENCES sys_user(id);
ALTER TABLE sys_admin_notice_read ADD CONSTRAINT fk_sys_admin_notice_read_notice FOREIGN KEY (notice_id) REFERENCES sys_notice(id);