package com.e_commerce.module.oms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 结算单DTO
 */
@Data
public class OmsSettleDto {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    
    @NotEmpty(message = "请选择要结算的商品")
    private List<Long> cartIds;
}