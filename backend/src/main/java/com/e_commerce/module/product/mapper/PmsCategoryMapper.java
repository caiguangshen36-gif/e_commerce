package com.e_commerce.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.product.entity.PmsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmsCategoryMapper extends BaseMapper<PmsCategory> {

    @Select("select category_name from pms_category where id = #{id}")
    String selectCategoryNameById(Long id);

    @Select("select * from pms_category where parent_id = #{parentId}")
    List<PmsCategory> selectByParentId(Long parentId);
}
