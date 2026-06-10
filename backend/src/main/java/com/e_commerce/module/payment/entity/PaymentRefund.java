package com.e_commerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_refund")
public class PaymentRefund {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long transactionId;
    private String refundNo;
    private BigDecimal refundAmount;
    private Integer status;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime refundTime;
}