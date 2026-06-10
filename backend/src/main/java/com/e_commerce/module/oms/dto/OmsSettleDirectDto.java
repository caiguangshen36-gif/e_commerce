package com.e_commerce.module.oms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OmsSettleDirectDto {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotNull(message = "商品不能为空")
    private Long productId;

    @NotNull(message = "规格不能为空")
    private Long skuId;

    @Min(value = 1, message = "数量最少1件")
    private Integer quantity;
}
