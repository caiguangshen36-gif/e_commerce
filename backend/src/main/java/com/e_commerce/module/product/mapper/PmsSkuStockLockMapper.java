package com.e_commerce.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.product.entity.PmsSkuStockLock;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * SKU库存锁定Mapper
 */
@Mapper
public interface PmsSkuStockLockMapper extends BaseMapper<PmsSkuStockLock> {

    /**
     * 查询某个SKU的已锁定库存数量（status=0的）
     */
    @Select("SELECT COALESCE(SUM(lock_num), 0) FROM pms_sku_stock_lock " +
            "WHERE sku_id = #{skuId} AND status = 0")
    int selectLockedStockBySkuId(@Param("skuId") Long skuId);

    /**
     * 查询某个SKU的已锁定库存数量（排除指定订单）
     */
    @Select("SELECT COALESCE(SUM(lock_num), 0) FROM pms_sku_stock_lock " +
            "WHERE sku_id = #{skuId} AND status = 0 AND order_sn != #{excludeOrderSn}")
    int selectLockedStockExcludeOrder(@Param("skuId") Long skuId, @Param("excludeOrderSn") String excludeOrderSn);
}
