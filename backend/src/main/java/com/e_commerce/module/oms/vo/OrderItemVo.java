package com.e_commerce.module.oms.vo;

import lombok.Data;

import java.math.BigDecimal;

import lombok.Data;
import java.math.BigDecimal;
/**
 * 订单项视图对象
 * 用于封装订单项相关的数据，通常用于前端展示或数据传输
 */
@Data
public class OrderItemVo {
    // 订单项ID，唯一标识一个订单项
    private Long id;
    // 所属订单的ID，关联到订单主表
    private Long orderId;
    // 订单编号，用于标识特定的订单
    private String orderSn;
    // 商品ID，关联到商品主表
    private Long productId;
    // SKU ID，关联到商品SKU表
    private Long skuId;
    // 商品名称，用于展示商品信息
    private String productName;
    // SKU规格，如颜色、尺寸等
    private String skuSpecs;
    // 商品图片URL，用于展示商品图片
    private String pic;
    // 商品单价，使用BigDecimal确保精度
    private BigDecimal  price;
    // 购买数量
    private Integer quantity;
    // 该订单项总金额，单价×数量
    private BigDecimal totalPrice;
}