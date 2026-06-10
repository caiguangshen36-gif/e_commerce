package com.e_commerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.product.entity.PmsProductCollect;
import com.e_commerce.module.product.mapper.PmsProductCollectMapper;
import com.e_commerce.module.product.vo.PmsCollectVo;
import com.e_commerce.module.product.vo.PmsSkuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品收藏服务类
 */
@Service
public class PmsProductCollectService {

    @Autowired
    private PmsProductCollectMapper collectMapper;

    @Autowired
    private PmsProductService productService;
    @Autowired
    private PmsSkuService pmsSkuService;

    /**
     * 添加商品收藏
     */
    public void addCollect(Long userId, Long productId) {
        // 检查是否已经收藏
        LambdaQueryWrapper<PmsProductCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PmsProductCollect::getUserId, userId)
                    .eq(PmsProductCollect::getProductId, productId);
        PmsProductCollect existing = collectMapper.selectOne(queryWrapper);
        if (existing == null) {
            PmsProductCollect collect = new PmsProductCollect();
            collect.setUserId(userId);
            collect.setProductId(productId);
            collectMapper.insert(collect);
        }
    }

    /**
     * 取消商品收藏
     */
    public void removeCollect(Long userId, Long productId) {
        LambdaQueryWrapper<PmsProductCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductCollect::getUserId, userId)
               .eq(PmsProductCollect::getProductId, productId);
        collectMapper.delete(wrapper);
    }

    /**
     * 获取用户收藏列表（分页）
     */
    public PageVo<PmsCollectVo> getCollectList(Long pageNum, Long pageSize, Long userId) {
        List<PmsCollectVo> collectList = collectMapper.getCollectsByUserId(userId);

        for (PmsCollectVo collect : collectList) {
            List<PmsSkuVo> skus = pmsSkuService.getSkusByProductId(collect.getProductId());
            if (skus != null) {
                collect.setSkuList(skus);
            }
        }

        // 手动分页
        long total = collectList.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize.intValue(), collectList.size());
        if (fromIndex >= collectList.size()) {
            fromIndex = 0;
            toIndex = 0;
        }
        List<PmsCollectVo> pageList = collectList.subList(fromIndex, toIndex);

        PageVo<PmsCollectVo> pageVo = new PageVo<>();
        pageVo.setList(pageList);
        pageVo.setTotal(total);
        return pageVo;
    }

    public PageVo<PmsCollectVo> getCollectListByCondition(Long pageNum, Long pageSize, Long userId, String productName, Long categoryId) {
        List<PmsCollectVo> collectList = collectMapper.getCollectsByUserIdWithCondition(userId, productName, categoryId);
        for (PmsCollectVo collect : collectList) {
            List<PmsSkuVo> skus = pmsSkuService.getSkusByProductId(collect.getProductId());
            if (skus != null) {
                collect.setSkuList(skus);
            }
        }

        // 手动分页
        long total = collectList.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize.intValue(), collectList.size());
        if (fromIndex >= collectList.size()) {
            fromIndex = 0;
            toIndex = 0;
        }
        List<PmsCollectVo> pageList = collectList.subList(fromIndex, toIndex);

        PageVo<PmsCollectVo> pageVo = new PageVo<>();
        pageVo.setList(pageList);
        pageVo.setTotal(total);
        return pageVo;
    }

    /**
     * 检查商品是否已收藏
     */
    public boolean isCollected(Long userId, Long productId) {
        LambdaQueryWrapper<PmsProductCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PmsProductCollect::getUserId, userId)
                    .eq(PmsProductCollect::getProductId, productId);
        PmsProductCollect collect = collectMapper.selectOne(queryWrapper);
        return collect != null;
    }

    @Transactional
    public void clear(Long userId) {
        LambdaQueryWrapper<PmsProductCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductCollect::getUserId, userId);
        collectMapper.delete(wrapper);
    }

    public void batchRemoveCollect(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new RuntimeException("请选择要取消收藏的商品");
        }
        LambdaQueryWrapper<PmsProductCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductCollect::getUserId, userId)
               .in(PmsProductCollect::getProductId, productIds);
        collectMapper.delete(wrapper);
    }
}
