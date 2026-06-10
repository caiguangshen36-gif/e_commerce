package com.e_commerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.product.entity.PmsProductBrowse;
import com.e_commerce.module.product.mapper.PmsProductBrowseMapper;
import com.e_commerce.module.product.vo.PmsBrowseVo;
import com.e_commerce.module.product.vo.PmsSkuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmsProductBrowseService {
    @Autowired
    private PmsProductBrowseMapper browseMapper;

    @Autowired
    private PmsProductService productService;

    @Autowired
    private PmsSkuService skuService;

    public void addBrowse(Long userId, Long productId) {
        // 先删除该用户对该商品的旧浏览记录
        LambdaQueryWrapper<PmsProductBrowse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PmsProductBrowse::getUserId, userId)
                    .eq(PmsProductBrowse::getProductId, productId);
        PmsProductBrowse existing = browseMapper.selectOne(queryWrapper);
        if (existing != null) {
            browseMapper.deleteById(existing.getId());
        }

        // 添加新的浏览记录
        PmsProductBrowse browse = new PmsProductBrowse();
        browse.setUserId(userId);
        browse.setProductId(productId);
        browseMapper.insert(browse);
    }

    public void deleteBrowse(Long userId, Long productId) {
        LambdaQueryWrapper<PmsProductBrowse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PmsProductBrowse::getUserId, userId)
                    .eq(PmsProductBrowse::getProductId, productId);
        PmsProductBrowse browse = browseMapper.selectOne(queryWrapper);
        if (browse != null) {
            browseMapper.deleteById(browse.getId());
        }
    }

    public List<PmsBrowseVo> getBrowseList(Long userId) {
        List<PmsBrowseVo> browseList = browseMapper.getBrowseListWithProduct(userId);

        for (PmsBrowseVo browseVo : browseList) {
            List<PmsSkuVo> skus = skuService.getSkusByProductId(browseVo.getProductId());
            if (skus != null) {
                browseVo.setSkuList(skus);
            }
        }

        return browseList;
    }

    public PageVo<PmsBrowseVo> getBrowseListByCondition(Long pageNum, Long pageSize, Long userId, String productName, Long categoryId) {
        List<PmsBrowseVo> browseList = browseMapper.getBrowseListWithProductByCondition(userId, productName, categoryId);
        for (PmsBrowseVo browseVo : browseList) {
            List<PmsSkuVo> skus = skuService.getSkusByProductId(browseVo.getProductId());
            if (skus != null) {
                browseVo.setSkuList(skus);
            }
        }

        // 手动分页
        long total = browseList.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize.intValue(), browseList.size());
        if (fromIndex >= browseList.size()) {
            fromIndex = 0;
            toIndex = 0;
        }
        List<PmsBrowseVo> pageList = browseList.subList(fromIndex, toIndex);

        PageVo<PmsBrowseVo> pageVo = new PageVo<>();
        pageVo.setList(pageList);
        pageVo.setTotal(total);
        return pageVo;
    }

    public void clearBrowse(Long userId) {
        LambdaQueryWrapper<PmsProductBrowse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductBrowse::getUserId, userId);
        browseMapper.delete(wrapper);
    }

    public void batchDeleteBrowse(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要删除的足迹");
        }
        LambdaQueryWrapper<PmsProductBrowse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductBrowse::getUserId, userId)
               .in(PmsProductBrowse::getId, ids);
        browseMapper.delete(wrapper);
    }
}
