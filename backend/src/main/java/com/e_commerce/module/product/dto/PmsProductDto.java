package com.e_commerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PmsProductDto {
    private Long id;
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;
    private String pic;
    private Integer status;
    private List<PmsSkuDto> skuList;
    private String detailHtml;
}
