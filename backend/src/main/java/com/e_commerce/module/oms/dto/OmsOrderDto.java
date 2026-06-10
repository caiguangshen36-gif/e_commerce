package com.e_commerce.module.oms.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单数据传输对象(DTO)
 * 用于封装订单相关的数据，在系统各层之间传递
 */
@Data
public class OmsOrderDto {
    private Long addressId;           // 收货地址ID（通过ID查完整地址）
    private List<OmsOrderItemDto> orderItemList; // 购物车/商品项列表
    private String remark;            // 订单备注（可选加）
    private Integer payType;          // 支付方式 1-微信 2-支付宝（可选）
}