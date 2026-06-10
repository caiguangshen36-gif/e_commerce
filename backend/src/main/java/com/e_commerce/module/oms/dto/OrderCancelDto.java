package com.e_commerce.module.oms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCancelDto {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotBlank(message = "请选择取消原因")
    private String cancelReason;
    private String cancelDescription;
}