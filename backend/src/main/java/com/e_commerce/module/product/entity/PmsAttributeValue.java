package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_attribute_value")
public class PmsAttributeValue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long attrId;
    private String attrValue;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
