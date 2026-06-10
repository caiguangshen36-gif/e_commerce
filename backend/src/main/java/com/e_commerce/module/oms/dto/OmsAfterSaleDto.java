package com.e_commerce.module.oms.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 售后数据传输对象(DTO)
 * 用于封装订单售后相关的数据信息
 */
@Data
public class OmsAfterSaleDto {
    private Long orderId;        // 订单ID，标识所属订单
    private Long orderItemId;    // 订单项ID，标识订单中的具体商品项
    private Integer type;        // 售后类型，如退货、换货等
    private String reason;       // 售后原因，申请售后的具体理由
    private String description;  // 售后描述，对售后情况的详细说明
    private BigDecimal refundAmount; // 退款金额，涉及的具体退款数额
}
