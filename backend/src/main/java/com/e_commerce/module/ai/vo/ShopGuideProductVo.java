package com.e_commerce.module.ai.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 智能导购商品 VO
 */
@Data
public class ShopGuideProductVo {
    private Long id;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private String pic;
    private String shortDesc;
    private Integer isHot;
    private String matchReason;
}
