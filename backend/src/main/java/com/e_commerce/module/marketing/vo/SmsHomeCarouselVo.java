package com.e_commerce.module.marketing.vo;

import lombok.Data;

@Data
public class SmsHomeCarouselVo {
    private Long id;
    private String pic;      // 图片地址
    private String url;      // 跳转地址
    private Integer sort;    // 排序
    private Integer status;  // 状态 0禁用 1启用
}
