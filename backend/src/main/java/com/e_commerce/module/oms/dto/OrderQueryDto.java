package com.e_commerce.module.oms.dto;

import lombok.Data;

@Data
public class OrderQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderSn;
    private Integer status;
    private String startTime;
    private String endTime;
}