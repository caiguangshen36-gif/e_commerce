package com.e_commerce.module.oms.dto;

import lombok.Data;

/**
 * 购物车数据传输对象
 * 用于封装购物车相关的数据信息
 */
@Data
public class OmsCartDto {
    private Long productId;    // 商品ID
    private Long skuId;       // SKU ID，用于标识具体商品规格
    private Integer quantity; // 商品数量
}

