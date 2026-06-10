package com.e_commerce.module.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRefundDto {
    private Long transactionId;
    private BigDecimal refundAmount;
    private String reason;
}