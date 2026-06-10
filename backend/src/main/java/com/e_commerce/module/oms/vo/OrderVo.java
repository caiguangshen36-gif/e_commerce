package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象，用于封装订单相关的数据
 * 包含订单基本信息、状态、收货信息、时间节点以及订单项和物流信息
 */
@Data
public class OrderVo {
    private Long id;                    // 订单ID
    private String orderSn;            // 订单编号
    private Long userId;               // 用户ID
    private BigDecimal totalAmount;    // 订单总金额
    private BigDecimal payAmount;      // 实付金额
    private Integer status;            // 订单状态
    private String statusText;         // 订单状态文本描述
    private String receiver;           // 收货人姓名
    private String phone;              // 收货人电话
    private String address;            // 收货地址
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;      // 支付时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime; // 发货时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;  // 确认收货时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime commentTime;  // 评价时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;   // 创建时间
    private List<OrderItemVo> orderItems; // 订单项列表，包含订单中商品的具体信息
    private LogisticsVo logistics;      // 物流信息
}