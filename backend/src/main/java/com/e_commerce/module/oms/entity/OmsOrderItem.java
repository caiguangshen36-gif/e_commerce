package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项实体类
 * 用于存储订单中的商品信息
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("oms_order_item")
public class OmsOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;          // 订单项ID
    private Long orderId;     // 所属订单ID
    private String orderSn;   // 订单编号
    private Long productId;   // 商品ID
    private Long skuId;       // SKU ID
    private String productName; // 商品名称
    private String skuSpecs;  // SKU规格
    private String pic;       // 商品图片
    private BigDecimal price; // 商品单价
    private Integer quantity; // 购买数量
    private BigDecimal totalPrice; // 总价
}