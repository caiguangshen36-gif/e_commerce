package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算单明细实体类
 */
@Data
@TableName("oms_settle_item")
public class OmsSettleItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long settleId;
    private Long userId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuSpecs;
    private String pic;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}