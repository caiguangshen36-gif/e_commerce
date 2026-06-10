package com.e_commerce.module.oms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSalesVo {
    private Long productId;
    private String productName;
    private String pic;
    private Integer totalQuantity;
    private BigDecimal totalSales;
}