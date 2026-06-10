package com.e_commerce.module.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserBalanceDto {
    @NotNull
    private BigDecimal balance;
}
