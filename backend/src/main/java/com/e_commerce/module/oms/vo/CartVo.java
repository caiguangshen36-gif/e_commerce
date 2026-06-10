package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车数据传输对象
 * 用于封装购物车相关的数据，用于前后端数据交互
 */
@Data
public class CartVo {
    private Long id;                // 购物车项ID
    private Long userId;            // 用户ID
    private Long productId;         // 商品ID
    private Long skuId;             // SKU ID（库存保存单位）
    private Integer quantity;       // 商品数量
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    private String productName;     // 商品名称
    private String skuSpecs;        // SKU规格描述
    private String pic;            // 商品图片URL
    private BigDecimal price;      // 商品价格
}