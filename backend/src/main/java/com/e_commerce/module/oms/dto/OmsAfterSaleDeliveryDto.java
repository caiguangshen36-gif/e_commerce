package com.e_commerce.module.oms.dto;

import lombok.Data;

/**
 * 售后发货数据传输对象(DTO)
 * 用于封装和传输售后发货相关的数据信息
 */
@Data
public class OmsAfterSaleDeliveryDto {
    private Long afterSaleId;    // 售后订单ID，用于标识唯一的售后记录
    private String deliveryCompany;   // 物流公司名称，如顺丰、圆通等
    private String deliveryNo;    // 物流运单号，用于跟踪包裹的配送状态
}