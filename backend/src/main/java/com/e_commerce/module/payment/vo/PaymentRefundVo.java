package com.e_commerce.module.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRefundVo {
    private Long id;
    private Long transactionId;
    private String refundNo;
    private BigDecimal refundAmount;
    private Integer status;
    private String statusText;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;
}