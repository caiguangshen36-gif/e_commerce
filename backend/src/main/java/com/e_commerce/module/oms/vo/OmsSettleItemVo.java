package com.e_commerce.module.oms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算单明细VO
 */
@Data
public class OmsSettleItemVo {
    private Long id;
    private Long settleId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuSpecs;
    private String pic;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}