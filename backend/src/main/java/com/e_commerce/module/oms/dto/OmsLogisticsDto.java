package com.e_commerce.module.oms.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流数据传输对象(DTO)
 * 用于封装订单物流相关信息
 */
@Data
public class OmsLogisticsDto {
    private Long orderId;        // 订单ID，标识唯一订单
    private String deliveryCompany;   // 物流公司名称，如"顺丰速运"、"中通快递"等
    private String deliveryNo;    // 物流运单号，用于查询物流信息
}

