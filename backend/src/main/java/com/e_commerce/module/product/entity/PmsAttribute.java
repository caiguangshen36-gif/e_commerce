package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_attribute")
public class PmsAttribute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String attrName;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
