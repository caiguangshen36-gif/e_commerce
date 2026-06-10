package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 使用@Data注解来自动生成getter、setter、toString等方法
 */
@Data
@TableName("oms_cart")
public class OmsCart {
    @TableId(type = IdType.AUTO)
    // 购物车ID
    private Long id;
    // 用户ID，关联到用户表
    private Long userId;
    // 商品ID，关联到商品表
    private Long productId;
    // SKU ID，关联到商品SKU表
    private Long skuId;
    // 商品数量
    private Integer quantity;
    // 创建时间，记录购物项添加时间
    private LocalDateTime createTime;
}
