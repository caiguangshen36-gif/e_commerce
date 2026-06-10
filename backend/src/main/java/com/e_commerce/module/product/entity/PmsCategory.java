package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@TableName("pms_category")
public class PmsCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryName;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private String icon;
    private Integer status;
}
