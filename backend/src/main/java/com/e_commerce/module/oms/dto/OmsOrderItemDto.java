package com.e_commerce.module.oms.dto;

import lombok.Data;

/**
 * 订单项数据传输对象
 * 用于封装订单项相关的数据信息
 */
@Data
public class OmsOrderItemDto {
    private Long skuId;          // 必须：SKU ID，商品库存单位的唯一标识
    private Integer quantity;    // 必须：购买数量，用户购买该商品的数量
}