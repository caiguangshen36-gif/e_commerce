package com.e_commerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PmsSkuDto {
    private Long id;
    @NotBlank(message = "商品编码不能为空")
    private String skuCode;
    @NotNull(message = "销售价不能为空")
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer stockWarning;
    private String pic;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer status;
    private List<PmsSkuAttrDto> skuAttrList;
}
