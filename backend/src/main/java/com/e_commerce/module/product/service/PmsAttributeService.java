package com.e_commerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.product.dto.PmsAttributeDto;
import com.e_commerce.module.product.dto.PmsAttributeValueDto;
import com.e_commerce.module.product.entity.PmsAttribute;
import com.e_commerce.module.product.entity.PmsAttributeValue;
import com.e_commerce.module.product.mapper.PmsAttributeMapper;
import com.e_commerce.module.product.mapper.PmsAttributeValueMapper;
import com.e_commerce.module.product.mapper.PmsCategoryMapper;
import com.e_commerce.module.product.vo.PmsAttributeValueVo;
import com.e_commerce.module.product.vo.PmsAttributeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品属性服务实现类
 */
@Service
public class PmsAttributeService {

    @Autowired
    private PmsAttributeMapper attributeMapper;

    @Autowired
    private PmsAttributeValueMapper attributeValueMapper;

    @Autowired
    private PmsCategoryMapper categoryMapper;

    /**
     * 添加商品属性
     */
    public void addAttribute(PmsAttributeDto attributeDto) {
        PmsAttribute attribute = new PmsAttribute();
        BeanUtils.copyProperties(attributeDto, attribute);
        attributeMapper.insert(attribute);

        if (attributeDto.getValueList() != null && !attributeDto.getValueList().isEmpty()) {
            for (PmsAttributeValueDto valueDto : attributeDto.getValueList()) {
                PmsAttributeValue value = new PmsAttributeValue();
                BeanUtils.copyProperties(valueDto, value);
                value.setAttrId(attribute.getId());
                attributeValueMapper.insert(value);
            }
        }
    }

    /**
     * 更新商品属性
     */
    public void updateAttribute(PmsAttributeDto attributeDto) {
        PmsAttribute attribute = new PmsAttribute();
        BeanUtils.copyProperties(attributeDto, attribute);
        attributeMapper.updateById(attribute);

        // 删除原有规格值
        LambdaQueryWrapper<PmsAttributeValue> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PmsAttributeValue::getAttrId, attribute.getId());
        attributeValueMapper.delete(deleteWrapper);

        if (attributeDto.getValueList() != null && !attributeDto.getValueList().isEmpty()) {
            for (PmsAttributeValueDto valueDto : attributeDto.getValueList()) {
                PmsAttributeValue value = new PmsAttributeValue();
                BeanUtils.copyProperties(valueDto, value);
                value.setAttrId(attribute.getId());
                attributeValueMapper.insert(value);
            }
        }
    }

    /**
     * 根据ID获取商品属性
     */
    public PmsAttributeVo getAttributeById(Long id) {
        PmsAttribute attribute = attributeMapper.selectById(id);
        if (attribute == null) {
            return null;
        }
        PmsAttributeVo attributeVo = new PmsAttributeVo();
        BeanUtils.copyProperties(attribute, attributeVo);

        if (attribute.getCategoryId() != null) {
            String categoryName = categoryMapper.selectCategoryNameById(attribute.getCategoryId());
            attributeVo.setCategoryName(categoryName);
        }

        LambdaQueryWrapper<PmsAttributeValue> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(PmsAttributeValue::getAttrId, id);
        List<PmsAttributeValue> valueList = attributeValueMapper.selectList(valueWrapper);

        List<PmsAttributeValueVo> valueVoList = new ArrayList<>();
        for (PmsAttributeValue value : valueList) {
            PmsAttributeValueVo valueVo = new PmsAttributeValueVo();
            BeanUtils.copyProperties(value, valueVo);
            valueVo.setAttrName(attribute.getAttrName());
            valueVoList.add(valueVo);
        }
        attributeVo.setValueList(valueVoList);

        return attributeVo;
    }

    /**
     * 获取所有商品属性列表
     */
    public List<PmsAttributeVo> getAttributeList() {
        List<PmsAttribute> attributeList = attributeMapper.selectList(null);
        List<PmsAttributeVo> attributeVoList = new ArrayList<>();
        for (PmsAttribute attribute : attributeList) {
            PmsAttributeVo attributeVo = new PmsAttributeVo();
            BeanUtils.copyProperties(attribute, attributeVo);

            if (attribute.getCategoryId() != null) {
                String categoryName = categoryMapper.selectCategoryNameById(attribute.getCategoryId());
                attributeVo.setCategoryName(categoryName);
            }

            LambdaQueryWrapper<PmsAttributeValue> valueWrapper = new LambdaQueryWrapper<>();
            valueWrapper.eq(PmsAttributeValue::getAttrId, attribute.getId());
            List<PmsAttributeValue> valueList = attributeValueMapper.selectList(valueWrapper);

            List<PmsAttributeValueVo> valueVoList = new ArrayList<>();
            for (PmsAttributeValue value : valueList) {
                PmsAttributeValueVo valueVo = new PmsAttributeValueVo();
                BeanUtils.copyProperties(value, valueVo);
                valueVo.setAttrName(attribute.getAttrName());
                valueVoList.add(valueVo);
            }
            attributeVo.setValueList(valueVoList);
            attributeVoList.add(attributeVo);
        }
        return attributeVoList;
    }

    public List<PmsAttributeVo> getAttributeListByCondition(String attrName, Long categoryId, Integer status) {
        LambdaQueryWrapper<PmsAttribute> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(attrName != null && !attrName.isEmpty(), PmsAttribute::getAttrName, attrName)
               .eq(categoryId != null, PmsAttribute::getCategoryId, categoryId)
               .eq(status != null, PmsAttribute::getStatus, status)
               .orderByAsc(PmsAttribute::getSort)
               .orderByDesc(PmsAttribute::getCreateTime);
        List<PmsAttribute> attributeList = attributeMapper.selectList(wrapper);

        List<PmsAttributeVo> attributeVoList = new ArrayList<>();
        for (PmsAttribute attribute : attributeList) {
            PmsAttributeVo attributeVo = new PmsAttributeVo();
            BeanUtils.copyProperties(attribute, attributeVo);

            if (attribute.getCategoryId() != null) {
                String categoryName = categoryMapper.selectCategoryNameById(attribute.getCategoryId());
                attributeVo.setCategoryName(categoryName);
            }

            LambdaQueryWrapper<PmsAttributeValue> valueWrapper = new LambdaQueryWrapper<>();
            valueWrapper.eq(PmsAttributeValue::getAttrId, attribute.getId());
            List<PmsAttributeValue> valueList = attributeValueMapper.selectList(valueWrapper);

            List<PmsAttributeValueVo> valueVoList = new ArrayList<>();
            for (PmsAttributeValue value : valueList) {
                PmsAttributeValueVo valueVo = new PmsAttributeValueVo();
                BeanUtils.copyProperties(value, valueVo);
                valueVo.setAttrName(attribute.getAttrName());
                valueVoList.add(valueVo);
            }
            attributeVo.setValueList(valueVoList);
            attributeVoList.add(attributeVo);
        }
        return attributeVoList;
    }

    /**
     * 根据分类ID获取商品属性列表
     */
    public List<PmsAttributeVo> getAttributesByCategoryId(Long categoryId) {
        LambdaQueryWrapper<PmsAttribute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsAttribute::getCategoryId, categoryId);
        List<PmsAttribute> attributeList = attributeMapper.selectList(wrapper);

        List<PmsAttributeVo> attributeVoList = new ArrayList<>();
        for (PmsAttribute attribute : attributeList) {
            PmsAttributeVo attributeVo = new PmsAttributeVo();
            BeanUtils.copyProperties(attribute, attributeVo);

            String categoryName = categoryMapper.selectCategoryNameById(categoryId);
            attributeVo.setCategoryName(categoryName);

            LambdaQueryWrapper<PmsAttributeValue> valueWrapper = new LambdaQueryWrapper<>();
            valueWrapper.eq(PmsAttributeValue::getAttrId, attribute.getId());
            List<PmsAttributeValue> valueList = attributeValueMapper.selectList(valueWrapper);

            List<PmsAttributeValueVo> valueVoList = new ArrayList<>();
            for (PmsAttributeValue value : valueList) {
                PmsAttributeValueVo valueVo = new PmsAttributeValueVo();
                BeanUtils.copyProperties(value, valueVo);
                valueVo.setAttrName(attribute.getAttrName());
                valueVoList.add(valueVo);
            }
            attributeVo.setValueList(valueVoList);
            attributeVoList.add(attributeVo);
        }
        return attributeVoList;
    }

    /**
     * 更新商品属性状态
     */
    public void updateAttributeStatus(Long id, Integer status) {
        LambdaUpdateWrapper<PmsAttribute> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsAttribute::getId, id)
               .set(PmsAttribute::getStatus, status);
        attributeMapper.update(null, wrapper);
    }

    /**
     * 删除商品属性
     */
    public void deleteAttribute(Long id) {
        LambdaQueryWrapper<PmsAttributeValue> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PmsAttributeValue::getAttrId, id);
        attributeValueMapper.delete(deleteWrapper);

        attributeMapper.deleteById(id);
    }

    // ==================== 规格值操作 ====================

    /**
     * 添加规格值
     */
    public void addAttributeValue(PmsAttributeValueDto attributeValueDto) {
        if (attributeValueDto.getAttrId() == null) {
            throw new RuntimeException("规格ID不能为空");
        }
        PmsAttributeValue value = new PmsAttributeValue();
        BeanUtils.copyProperties(attributeValueDto, value);
        if (value.getSort() == null) {
            value.setSort(0);
        }
        if (value.getStatus() == null) {
            value.setStatus(1);
        }
        attributeValueMapper.insert(value);
    }

    /**
     * 更新规格值
     */
    public void updateAttributeValue(PmsAttributeValueDto attributeValueDto) {
        PmsAttributeValue value = new PmsAttributeValue();
        BeanUtils.copyProperties(attributeValueDto, value);
        attributeValueMapper.updateById(value);
    }

    /**
     * 获取规格值详情
     */
    public PmsAttributeValueVo getAttributeValueById(Long id) {
        PmsAttributeValue value = attributeValueMapper.selectById(id);
        if (value == null) {
            return null;
        }
        PmsAttributeValueVo vo = new PmsAttributeValueVo();
        BeanUtils.copyProperties(value, vo);
        PmsAttribute attribute = attributeMapper.selectById(value.getAttrId());
        if (attribute != null) {
            vo.setAttrName(attribute.getAttrName());
        }
        return vo;
    }

    /**
     * 根据规格ID获取规格值列表
     */
    public List<PmsAttributeValueVo> getAttributeValuesByAttrId(Long attrId) {
        LambdaQueryWrapper<PmsAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsAttributeValue::getAttrId, attrId);
        List<PmsAttributeValue> valueList = attributeValueMapper.selectList(wrapper);

        PmsAttribute attribute = attributeMapper.selectById(attrId);
        String attrName = attribute != null ? attribute.getAttrName() : "";

        List<PmsAttributeValueVo> voList = new ArrayList<>();
        for (PmsAttributeValue value : valueList) {
            PmsAttributeValueVo vo = new PmsAttributeValueVo();
            BeanUtils.copyProperties(value, vo);
            vo.setAttrName(attrName);
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 更新规格值状态
     */
    public void updateAttributeValueStatus(Long id, Integer status) {
        LambdaUpdateWrapper<PmsAttributeValue> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsAttributeValue::getId, id)
               .set(PmsAttributeValue::getStatus, status);
        attributeValueMapper.update(null, wrapper);
    }

    /**
     * 删除规格值
     */
    public void deleteAttributeValue(Long id) {
        attributeValueMapper.deleteById(id);
    }
}
