package com.e_commerce.security.config;

public final class SecurityWhitePaths {

    // 不需要登录、不需要Token的放行路径
    public static final String[] WHITE_LIST = {
            // ========== 系统管理 ==========
            "/sys/user/login",
            "/sys/user/register",
            "/sys/user/list",
            "/sys/user/add",
            "/sys/user/updatePassword",
            "/sys/role/user/assign",
            "/sys/role/list",
            "/sys/menu/list",

            // ========== 用户模块 ==========
            "/ums/user/login",
            "/ums/user/register",

            // ========== 验证码和上传 ==========
            "/captcha/get",
            "/upload",

            // ========== 商品模块（公开浏览） ==========
            "/product/list",
            "/product/search",
            "/product/hotList",
            "/product/detail",
            "/product/listByCategory",
            "/product/category/list",
            "/product/category/updateStatus",
            "/product/category/listByParentId",
            "/product/category/detail",
            "/product/sku/list",
            "/product/sku/allList",
            "/product/sku/detail",
            "/product/attribute/list",
            "/product/attribute/listByCategory",
            "/product/attribute/value/listByAttr",
            "/product/attribute/value/detail",

            // ========== 营销模块 ==========
            "/marketing/carousel",
            "/marketing/carousel/list",
            "/marketing/carousel/detail",

            // ========== 订单模块 ==========
            "/order/admin/detail",
            "/order/admin/list",
            "/order/admin/productSalesTop5",

            // ========== 物流模块（公开查询） ==========
            "/oms/logistics/detail",
            "/oms/logistics/user/trace/list",

            // ========== 售后模块（公开查询） ==========
            "/oms/after-sale/delivery",

            // ========== 评论模块（公开浏览） ==========
            "/comment/list",
            "/comment/detail/*",
            "/comment/reply/list/*",

            // ========== AI模块（公开接口） ==========
            "/ai/chat",
            "/ai/customer-service",
            "/ai/intelligent-search",
            "/ai/shop-guide",
            "/ai/generate-goods-desc",
            "/ai/knowledge/list",
            "/ai/knowledge/list/type/*",
            "/ai/knowledge/search",
            "/ai/knowledge/match",
            
            // ========== AI模块（管理接口） ==========
            "/ai/knowledge/add",
            "/ai/knowledge/update/*",
            "/ai/knowledge/detail/*",
            "/ai/knowledge/delete/*",
            "/ai/knowledge/admin/list",

            // ========== 测试接口 ==========
            "/test/importProducts"
    };
}