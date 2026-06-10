package com.e_commerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PmsAttributeDto {
    private Long id;
    @NotNull
    private Long categoryId;
    @NotBlank
    private String attrName;
    private Integer sort;
    private Integer status;
    private List<PmsAttributeValueDto> valueList;
}
