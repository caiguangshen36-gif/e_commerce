package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * SKU规格关联表
 */
@Data
@TableName("pms_sku_attr")
public class PmsSkuAttr {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    private Long productId;
    private Long attrId;
    private Long attrValueId;
    private String attrName;
    private String attrValue;
    private LocalDateTime createTime;
}