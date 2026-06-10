package com.e_commerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PmsAttributeValueDto {
    private Long id;
    private Long attrId;
    @NotBlank
    private String attrValue;
    private Integer sort;
    private Integer status;
}
