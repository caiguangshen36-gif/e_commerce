package com.e_commerce.module.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentTransactionVo {
    private Long id;
    private Long orderId;
    private String transactionNo;
    private Integer paymentMethod;
    private String paymentMethodText;
    private BigDecimal amount;
    private Integer status;
    private String statusText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callbackTime;
}