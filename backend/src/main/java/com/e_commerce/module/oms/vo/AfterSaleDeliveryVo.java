package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 售后配送信息值对象
 * 用于封装和传输售后配送相关的数据
 */
@Data
public class AfterSaleDeliveryVo {
    private String deliveryCompany;  // 配送公司名称
    private String deliveryNo;      // 配送单号
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;  // 发货时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime; // 收货时间
    private Integer status;         // 配送状态码
    private String statusText;      // 配送状态文本描述
}