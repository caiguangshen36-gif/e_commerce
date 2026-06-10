package com.e_commerce.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.product.entity.PmsProductCollect;
import com.e_commerce.module.product.vo.PmsCollectVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PmsProductCollectMapper extends BaseMapper<PmsProductCollect> {

    List<PmsCollectVo> getCollectsByUserId(Long userId);

    @Select("<script>" +
            "SELECT pc.id AS collectId, pc.product_id AS productId, pc.create_time AS createTime, " +
            "p.product_name AS productName, p.pic AS pic, s.id AS sku_id, s.price, s.sku_code " +
            "FROM pms_product_collect pc " +
            "LEFT JOIN pms_product p ON pc.product_id = p.id " +
            "LEFT JOIN pms_sku s ON p.id = s.product_id " +
            "WHERE pc.user_id = #{userId} " +
            "<if test='productName != null and productName != \"\"'>AND p.product_name LIKE CONCAT('%', #{productName}, '%')</if> " +
            "<if test='categoryId != null'>AND p.category_id = #{categoryId}</if> " +
            "ORDER BY pc.create_time DESC" +
            "</script>")
    List<PmsCollectVo> getCollectsByUserIdWithCondition(
            @Param("userId") Long userId,
            @Param("productName") String productName,
            @Param("categoryId") Long categoryId
    );
}
