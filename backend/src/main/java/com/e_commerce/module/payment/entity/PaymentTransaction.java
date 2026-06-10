package com.e_commerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付交易实体类
 * 对应数据库表 payment_transaction
 * 使用@Data注解自动生成getter、setter等方法
 */
@Data
@TableName("payment_transaction")
public class PaymentTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String transactionNo;
    private Integer paymentMethod;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime callbackTime;
    private String callbackData;
}