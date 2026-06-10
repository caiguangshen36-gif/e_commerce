package com.e_commerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SKU库存锁定表实体
 * 用于防止超卖，记录订单占用库存的情况
 */
@Data
@TableName("pms_sku_stock_lock")
public class PmsSkuStockLock {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderSn;
    
    /**
     * SKU_ID
     */
    private Long skuId;
    
    /**
     * 锁定库存数量
     */
    private Integer lockNum;
    
    /**
     * 状态：0-锁定 1-已扣减 2-已释放
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
