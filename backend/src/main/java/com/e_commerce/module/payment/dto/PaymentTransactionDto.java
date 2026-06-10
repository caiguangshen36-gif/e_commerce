package com.e_commerce.module.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentTransactionDto {
    private Long orderId;
    private Integer paymentMethod;
    private BigDecimal amount;
}