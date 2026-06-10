package com.e_commerce.module.oms.dto;

import lombok.Data;

@Data
public class OrderDeliverDto {
    private Long orderId;
    private String trackingNo;
    private String logisticsCompany;
}
