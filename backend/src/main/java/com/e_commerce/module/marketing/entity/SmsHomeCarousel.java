package com.e_commerce.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_home_carousel")
public class SmsHomeCarousel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pic;
    private String url;
    private Integer sort;
    private Integer status;
}