package com.e_commerce.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.product.entity.PmsProductBrowse;
import com.e_commerce.module.product.vo.PmsBrowseVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PmsProductBrowseMapper extends BaseMapper<PmsProductBrowse> {

    List<PmsBrowseVo> getBrowseListWithProduct(Long userId);

    @Select("<script>" +
            "SELECT pb.id AS browseId, pb.product_id AS productId, pb.create_time AS createTime, " +
            "p.product_name AS productName, p.pic AS pic, c.category_name AS categoryName " +
            "FROM pms_product_browse pb " +
            "LEFT JOIN pms_product p ON pb.product_id = p.id " +
            "LEFT JOIN pms_category c ON p.category_id = c.id " +
            "WHERE pb.user_id = #{userId} " +
            "<if test='productName != null and productName != \"\"'>AND p.product_name LIKE CONCAT('%', #{productName}, '%')</if> " +
            "<if test='categoryId != null'>AND p.category_id = #{categoryId}</if> " +
            "ORDER BY pb.create_time DESC" +
            "</script>")
    List<PmsBrowseVo> getBrowseListWithProductByCondition(
            @Param("userId") Long userId,
            @Param("productName") String productName,
            @Param("categoryId") Long categoryId
    );

    List<PmsProductBrowse> getBrowsesByUserId(Long userId);
}
