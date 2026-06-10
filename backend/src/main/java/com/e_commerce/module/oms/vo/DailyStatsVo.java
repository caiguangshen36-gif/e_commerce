package com.e_commerce.module.oms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyStatsVo {
    private String date;
    private Long orderCount;
    private BigDecimal sales;
}
