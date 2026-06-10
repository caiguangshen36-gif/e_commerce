package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_product")
public class PmsProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String productName;
    private Long categoryId;
    private String pic;
    private String detailHtml;
    private Integer status;
    private Integer isHot;
    private Integer hotSort;
    private LocalDateTime createTime;
}