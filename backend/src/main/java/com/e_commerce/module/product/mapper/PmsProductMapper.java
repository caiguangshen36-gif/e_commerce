package com.e_commerce.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.product.entity.PmsProduct;
import com.e_commerce.module.product.entity.PmsSkuAttr;
import com.e_commerce.module.product.vo.PmsProductVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmsProductMapper extends BaseMapper<PmsProduct> {
    // 商品列表（支持关键词、状态、分类筛选）- 4表LEFT JOIN，XML中
    List<PmsProductVo> selectProductList(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("categoryId") Long categoryId
    );

    // 根据ID查询详情 - 4表LEFT JOIN，XML中
    PmsProductVo selectProductById(@Param("id") Long id);

    @Select("SELECT category_name FROM pms_category WHERE id = #{categoryId}")
    String selectCategoryNameById(@Param("categoryId") Long categoryId);

    // 查商品所有SKU的属性（用于冗余到ES）
    @Select("SELECT attr_name, attr_value FROM pms_sku_attr WHERE product_id = #{productId}")
    List<PmsSkuAttr> selectAttrsByProductId(@Param("productId") Long productId);

    // 按ID列表查商品VO - 4表LEFT JOIN，XML中
    List<PmsProductVo> selectProductVoByIds(@Param("ids") List<Long> ids);

    // 查询热门商品列表（is_hot=1）- 4表LEFT JOIN，XML中
    List<PmsProductVo> selectHotProductList();

}
