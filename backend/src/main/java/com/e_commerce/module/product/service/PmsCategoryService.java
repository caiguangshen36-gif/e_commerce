package com.e_commerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.product.entity.PmsCategory;
import com.e_commerce.module.product.mapper.PmsCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务实现类
 * 提供商品分类的增删改查等业务逻辑处理
 */
@Service
public class PmsCategoryService {
    @Autowired
    private PmsCategoryMapper pmsCategoryMapper;

    /**
     * 获取所有商品分类列表
     */
    public List<PmsCategory> list() {
        return pmsCategoryMapper.selectList(null);
    }

    public List<PmsCategory> listByCondition(String categoryName, Long parentId, Integer status) {
        LambdaQueryWrapper<PmsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(categoryName != null && !categoryName.isEmpty(), PmsCategory::getCategoryName, categoryName)
               .eq(parentId != null, PmsCategory::getParentId, parentId)
               .eq(status != null, PmsCategory::getStatus, status)
               .orderByAsc(PmsCategory::getSort);
        return pmsCategoryMapper.selectList(wrapper);
    }

    /**
     * 添加新的商品分类
     */
    public void add(PmsCategory pmsCategory) {
        pmsCategoryMapper.insert(pmsCategory);
    }

    /**
     * 更新商品分类信息
     */
    public void update(PmsCategory pmsCategory) {
        pmsCategoryMapper.updateById(pmsCategory);
    }

    /**
     * 根据ID删除商品分类
     */
    public void deleteById(Long id) {
        pmsCategoryMapper.deleteById(id);
    }

    /**
     * 根据父ID查询子分类
     */
    public List<PmsCategory> getByParentId(Long parentId) {
        LambdaQueryWrapper<PmsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsCategory::getParentId, parentId);
        return pmsCategoryMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询分类详情
     */
    public PmsCategory getById(Long id) {
        return pmsCategoryMapper.selectById(id);
    }

    /**
     * 更新状态
     */
    public void updateStatus(Long id, Integer status) {
        LambdaUpdateWrapper<PmsCategory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsCategory::getId, id)
               .set(PmsCategory::getStatus, status);
        pmsCategoryMapper.update(null, wrapper);
    }
}
