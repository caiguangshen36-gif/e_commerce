package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_sku")
public class PmsSku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer stockWarning;
    private String pic;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}