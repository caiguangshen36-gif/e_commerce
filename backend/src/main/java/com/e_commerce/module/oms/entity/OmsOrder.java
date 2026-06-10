package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
@TableName("oms_order")
public class OmsOrder {
    @TableId(type = IdType.AUTO)
    private Long id;                // 订单ID
    private String orderSn;         // 订单编号
    private Long userId;            // 用户ID
    private BigDecimal totalAmount; // 订单总金额
    private BigDecimal payAmount;   // 实际支付金额
    private Integer status;         // 订单状态
    private String receiver;        // 收货人姓名
    private String phone;           // 收货人电话
    private String address;        // 收货地址
    private LocalDateTime payTime;  // 支付时间
    private LocalDateTime deliveryTime; // 发货时间
    private LocalDateTime confirmTime; // 确认收货时间
    private LocalDateTime commentTime; // 评价时间
    private LocalDateTime createTime; // 创建时间
}