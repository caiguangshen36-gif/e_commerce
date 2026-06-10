package com.e_commerce.module.user.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private BigDecimal balance;
}
