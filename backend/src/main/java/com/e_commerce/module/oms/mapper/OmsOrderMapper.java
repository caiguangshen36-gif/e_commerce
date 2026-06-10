package com.e_commerce.module.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.vo.DailyStatsVo;
import com.e_commerce.module.oms.vo.ProductSalesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

    @Select("<script>" +
            "SELECT oi.product_id AS productId, oi.product_name AS productName, oi.pic AS pic, " +
            "SUM(oi.quantity) AS totalQuantity, SUM(oi.total_price) AS totalSales " +
            "FROM oms_order_item oi " +
            "INNER JOIN oms_order o ON o.id = oi.order_id " +
            "WHERE o.status = 3 " +
            "<if test='startTime != null and startTime != \"\"'>AND DATE(o.confirm_time) &gt;= #{startTime}</if>" +
            "<if test='endTime != null and endTime != \"\"'>AND DATE(o.confirm_time) &lt;= #{endTime}</if>" +
            "GROUP BY oi.product_id, oi.product_name, oi.pic " +
            "ORDER BY totalSales DESC " +
            "LIMIT 5" +
            "</script>")
    List<ProductSalesVo> getProductSalesTop5(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as orderCount, " +
            "SUM(CASE WHEN status IN (1,2,3) THEN pay_amount ELSE 0 END) as sales " +
            "FROM oms_order " +
            "WHERE create_time >= #{startTime} AND create_time <= #{endTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC")
    List<DailyStatsVo> getDailyStats(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
