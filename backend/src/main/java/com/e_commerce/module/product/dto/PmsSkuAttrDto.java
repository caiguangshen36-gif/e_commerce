package com.e_commerce.module.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PmsSkuAttrDto {
    @NotNull
    private Long attrId;
    @NotNull
    private Long attrValueId;
    private String attrName;
    private String attrValue;
}
