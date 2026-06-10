package com.e_commerce.module.product.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.oms.entity.OmsOrderItem;
import com.e_commerce.module.oms.mapper.OmsOrderItemMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import com.e_commerce.module.oms.vo.OrderItemVo;
import com.e_commerce.module.product.dto.PmsSkuAttrDto;
import com.e_commerce.module.product.dto.PmsSkuDto;
import com.e_commerce.module.product.entity.PmsSku;
import com.e_commerce.module.product.entity.PmsSkuAttr;
import com.e_commerce.module.product.entity.PmsSkuStockLock;
import com.e_commerce.module.product.mapper.PmsSkuAttrMapper;
import com.e_commerce.module.product.mapper.PmsSkuMapper;
import com.e_commerce.module.product.mapper.PmsSkuStockLockMapper;
import com.e_commerce.module.product.vo.PmsSkuAttrVo;
import com.e_commerce.module.product.vo.PmsSkuVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SKU服务类
 */
@Service
@Slf4j
public class PmsSkuService {

    @Autowired
    private PmsSkuMapper skuMapper;

    @Autowired
    private PmsSkuAttrMapper skuAttrMapper;

    @Autowired
    private PmsSkuStockLockMapper stockLockMapper;

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    public List<PmsSkuVo> getSkusByProductId(Long productId) {
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSku::getProductId, productId);
        List<PmsSku> skuList = skuMapper.selectList(wrapper);
        return convertToVoList(skuList);
    }

    public PmsSkuVo getSkuById(Long id) {
        PmsSku sku = skuMapper.selectById(id);
        if (sku == null) {
            return null;
        }
        return convertToVo(sku);
    }

    public List<PmsSkuVo> listOfAll() {
        List<PmsSku> skuList = skuMapper.selectList(null);
        return convertToVoList(skuList);
    }

    public PageVo<PmsSkuVo> listByCondition(Long pageNum, Long pageSize, String skuCode, Long productId, Integer status) {
        Page<PmsSku> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(skuCode != null && !skuCode.isEmpty(), PmsSku::getSkuCode, skuCode)
               .eq(productId != null, PmsSku::getProductId, productId)
               .eq(status != null, PmsSku::getStatus, status)
               .orderByDesc(PmsSku::getCreateTime);
        Page<PmsSku> result = skuMapper.selectPage(mpPage, wrapper);

        List<PmsSkuVo> voList = result.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        PageVo<PmsSkuVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    @Transactional
    public void addSku(Long productId, PmsSkuDto skuDto) {
        PmsSku sku = new PmsSku();
        BeanUtils.copyProperties(skuDto, sku);
        sku.setProductId(productId);
        sku.setCreateTime(LocalDateTime.now());
        sku.setUpdateTime(LocalDateTime.now());

        if (sku.getStockWarning() == null) {
            sku.setStockWarning(0);
        }
        if (sku.getCostPrice() == null) {
            sku.setCostPrice(BigDecimal.ZERO);
        }
        if (sku.getWeight() == null) {
            sku.setWeight(BigDecimal.ZERO);
        }
        if (sku.getVolume() == null) {
            sku.setVolume(BigDecimal.ZERO);
        }
        if (sku.getStatus() == null) {
            sku.setStatus(1);
        }

        skuMapper.insert(sku);

        if (CollUtil.isNotEmpty(skuDto.getSkuAttrList())) {
            for (PmsSkuAttrDto dto : skuDto.getSkuAttrList()) {
                PmsSkuAttr attr = new PmsSkuAttr();
                attr.setSkuId(sku.getId());
                attr.setProductId(productId);
                attr.setAttrId(dto.getAttrId());
                attr.setAttrValueId(dto.getAttrValueId());
                attr.setAttrName(dto.getAttrName());
                attr.setAttrValue(dto.getAttrValue());
                skuAttrMapper.insert(attr);
            }
        }
    }

    @Transactional
    public void updateSku(PmsSkuDto skuDto) {
        PmsSku sku = new PmsSku();
        BeanUtils.copyProperties(skuDto, sku);
        sku.setUpdateTime(LocalDateTime.now());
        skuMapper.updateById(sku);

        // 更新SKU属性关联（先删除再添加）
        LambdaQueryWrapper<PmsSkuAttr> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PmsSkuAttr::getSkuId, sku.getId());
        skuAttrMapper.delete(deleteWrapper);

        if (CollUtil.isNotEmpty(skuDto.getSkuAttrList())) {
            PmsSku existingSku = skuMapper.selectById(sku.getId());
            for (PmsSkuAttrDto skuAttrDto : skuDto.getSkuAttrList()) {
                PmsSkuAttr skuAttr = new PmsSkuAttr();
                skuAttr.setSkuId(sku.getId());
                skuAttr.setProductId(existingSku.getProductId());
                skuAttr.setAttrId(skuAttrDto.getAttrId());
                skuAttr.setAttrValueId(skuAttrDto.getAttrValueId());
                skuAttr.setAttrName(skuAttrDto.getAttrName());
                skuAttr.setAttrValue(skuAttrDto.getAttrValue());
                skuAttrMapper.insert(skuAttr);
            }
        }
    }

    public void updateSkuStatus(Long id, Integer status) {
        LambdaUpdateWrapper<PmsSku> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsSku::getId, id)
               .set(PmsSku::getStatus, status)
               .set(PmsSku::getUpdateTime, LocalDateTime.now());
        skuMapper.update(null, wrapper);
    }

    @Transactional
    public void deleteSku(Long id) {
        LambdaQueryWrapper<PmsSkuAttr> deleteAttrWrapper = new LambdaQueryWrapper<>();
        deleteAttrWrapper.eq(PmsSkuAttr::getSkuId, id);
        skuAttrMapper.delete(deleteAttrWrapper);
        skuMapper.deleteById(id);
    }

    @Transactional
    public void deleteSkusByProductId(Long productId) {
        LambdaQueryWrapper<PmsSkuAttr> deleteAttrWrapper = new LambdaQueryWrapper<>();
        deleteAttrWrapper.eq(PmsSkuAttr::getProductId, productId);
        skuAttrMapper.delete(deleteAttrWrapper);

        LambdaQueryWrapper<PmsSku> deleteSkuWrapper = new LambdaQueryWrapper<>();
        deleteSkuWrapper.eq(PmsSku::getProductId, productId);
        skuMapper.delete(deleteSkuWrapper);
    }

    /**
     * 根据订单ID扣减SKU库存
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateSkuStocksByOrderId(Long orderId) {
        log.info("开始处理订单库存扣减，orderId: {}", orderId);

        if (orderMapper.selectById(orderId) == null) {
            throw new RuntimeException("订单不存在，orderId: " + orderId);
        }

        List<OmsOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("订单没有商品项，orderId: " + orderId);
        }

        int successCount = 0;
        int failCount = 0;
        for (OmsOrderItem item : orderItems) {
            Long skuId = item.getSkuId();
            Integer quantity = item.getQuantity();

            if (skuId == null || quantity == null || quantity <= 0) {
                continue;
            }

            try {
                int affectedRows = deductStock(skuId, quantity);
                if (affectedRows > 0) {
                    successCount++;
                    log.info("库存扣减成功: SKU ID={}, 数量={}", skuId, quantity);
                } else {
                    failCount++;
                    log.warn("库存扣减失败（库存不足）: SKU ID={}, 扣减数量={}", skuId, quantity);
                }
            } catch (Exception e) {
                failCount++;
                log.error("库存扣减异常: SKU ID={}, 错误: {}", skuId, e.getMessage());
            }
        }

        log.info("订单 {} 库存扣减完成，成功: {} 项，失败: {} 项", orderId, successCount, failCount);
        return successCount;
    }

    /**
     * 根据订单ID回滚SKU库存
     */
    @Transactional(rollbackFor = Exception.class)
    public int rollbackSkuStocksByOrderId(Long orderId) {
        log.info("开始处理订单库存回滚，orderId: {}", orderId);

        if (orderMapper.selectById(orderId) == null) {
            throw new RuntimeException("订单不存在，orderId: " + orderId);
        }

        List<OmsOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("订单没有商品项，orderId: " + orderId);
        }

        int successCount = 0;
        int failCount = 0;
        for (OmsOrderItem item : orderItems) {
            Long skuId = item.getSkuId();
            Integer quantity = item.getQuantity();

            if (skuId == null || quantity == null || quantity <= 0) {
                continue;
            }

            try {
                int affectedRows = increaseStock(skuId, quantity);
                if (affectedRows > 0) {
                    successCount++;
                    log.info("库存恢复成功: SKU ID={}, 数量={}", skuId, quantity);
                } else {
                    failCount++;
                    log.warn("库存恢复失败: SKU ID={}, 恢复数量={}", skuId, quantity);
                }
            } catch (Exception e) {
                failCount++;
                log.error("库存恢复异常: SKU ID={}, 错误: {}", skuId, e.getMessage());
            }
        }

        log.info("订单 {} 库存恢复完成，成功: {} 项，失败: {} 项", orderId, successCount, failCount);
        return successCount;
    }

    // 库存预警列表
    public List<PmsSkuVo> getStockWarningList() {
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("stock <= stock_warning")
               .gt(PmsSku::getStockWarning, 0)
               .orderByAsc(PmsSku::getStock);
        List<PmsSku> skuList = skuMapper.selectList(wrapper);
        return convertToVoList(skuList);
    }

    // ==================== 库存锁定核心方法（防超卖） ====================

    /**
     * 预扣库存（创建订单时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public int preLockStock(String orderSn, List<OrderItemVo> items) {
        log.info("预扣库存开始，orderSn: {}, items: {}", orderSn, items.size());

        if (items == null || items.isEmpty()) {
            log.warn("预扣库存失败，商品列表为空，orderSn: {}", orderSn);
            return 0;
        }

        int successCount = 0;
        for (OrderItemVo item : items) {
            Long skuId = item.getSkuId();
            Integer quantity = item.getQuantity();

            if (skuId == null || quantity == null || quantity <= 0) {
                continue;
            }

            PmsSku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                throw new RuntimeException("预扣库存失败，SKU不存在，skuId: " + skuId);
            }

            int lockedStock = stockLockMapper.selectLockedStockExcludeOrder(skuId, orderSn);
            int availableStock = sku.getStock() - lockedStock;

            if (availableStock < quantity) {
                throw new RuntimeException(String.format("预扣库存失败，%s 库存不足，当前可售: %d, 需要: %d",
                        sku.getSkuCode(), availableStock, quantity));
            }

            PmsSkuStockLock stockLock = new PmsSkuStockLock();
            stockLock.setOrderSn(orderSn);
            stockLock.setSkuId(skuId);
            stockLock.setLockNum(quantity);
            stockLock.setStatus(0);
            stockLockMapper.insert(stockLock);

            successCount++;
            log.info("预扣库存成功，orderSn: {}, skuId: {}, 数量: {}", orderSn, skuId, quantity);
        }

        log.info("预扣库存完成，orderSn: {}, 成功: {} 项", orderSn, successCount);
        return successCount;
    }

    /**
     * 确认扣减库存（支付成功时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public int confirmStockDeduction(String orderSn) {
        log.info("确认扣减库存开始，orderSn: {}", orderSn);

        LambdaQueryWrapper<PmsSkuStockLock> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(PmsSkuStockLock::getOrderSn, orderSn);
        List<PmsSkuStockLock> lockRecords = stockLockMapper.selectList(lockWrapper);

        if (lockRecords.isEmpty()) {
            log.warn("确认扣减库存失败，没有找到锁定记录，orderSn: {}", orderSn);
            return 0;
        }

        int successCount = 0;
        for (PmsSkuStockLock lock : lockRecords) {
            if (lock.getStatus() != 0) {
                continue;
            }

            int affectedRows = deductStock(lock.getSkuId(), lock.getLockNum());
            if (affectedRows > 0) {
                // 更新锁定记录状态为已扣减
                LambdaUpdateWrapper<PmsSkuStockLock> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(PmsSkuStockLock::getOrderSn, orderSn)
                             .eq(PmsSkuStockLock::getStatus, 0)
                             .set(PmsSkuStockLock::getStatus, 1);
                stockLockMapper.update(null, updateWrapper);
                successCount++;
                log.info("确认扣减库存成功，orderSn: {}, skuId: {}, 数量: {}",
                        orderSn, lock.getSkuId(), lock.getLockNum());
            } else {
                log.error("确认扣减库存失败，库存不足，orderSn: {}, skuId: {}",
                        orderSn, lock.getSkuId());
            }
        }

        log.info("确认扣减库存完成，orderSn: {}, 成功: {} 项", orderSn, successCount);
        return successCount;
    }

    /**
     * 释放锁定库存（订单取消/超时/退款时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public int releaseStockLock(String orderSn) {
        log.info("释放锁定库存开始，orderSn: {}", orderSn);

        LambdaQueryWrapper<PmsSkuStockLock> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(PmsSkuStockLock::getOrderSn, orderSn);
        List<PmsSkuStockLock> lockRecords = stockLockMapper.selectList(lockWrapper);

        if (lockRecords.isEmpty()) {
            log.warn("释放锁定库存失败，没有找到锁定记录，orderSn: {}", orderSn);
            return 0;
        }

        int successCount = 0;
        for (PmsSkuStockLock lock : lockRecords) {
            if (lock.getStatus() != 0) {
                continue;
            }

            LambdaUpdateWrapper<PmsSkuStockLock> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PmsSkuStockLock::getOrderSn, orderSn)
                         .eq(PmsSkuStockLock::getStatus, 0)
                         .set(PmsSkuStockLock::getStatus, 2);
            stockLockMapper.update(null, updateWrapper);
            successCount++;
            log.info("释放锁定库存成功，orderSn: {}, skuId: {}, 数量: {}",
                    orderSn, lock.getSkuId(), lock.getLockNum());
        }

        log.info("释放锁定库存完成，orderSn: {}, 成功: {} 项", orderSn, successCount);
        return successCount;
    }

    /**
     * 查询SKU可用库存（总库存 - 已锁定库存）
     */
    public int getAvailableStock(Long skuId) {
        PmsSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            return 0;
        }
        int lockedStock = stockLockMapper.selectLockedStockBySkuId(skuId);
        return Math.max(0, sku.getStock() - lockedStock);
    }

    // ==================== 库存原子操作 ====================

    /**
     * 扣减SKU库存（使用LambdaUpdateWrapper + setSql实现原子操作）
     */
    private int deductStock(Long skuId, Integer quantity) {
        LambdaUpdateWrapper<PmsSku> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsSku::getId, skuId)
               .ge(PmsSku::getStock, quantity)
               .setSql("stock = stock - " + quantity)
               .set(PmsSku::getUpdateTime, LocalDateTime.now());
        return skuMapper.update(null, wrapper);
    }

    /**
     * 增加SKU库存（使用LambdaUpdateWrapper + setSql实现原子操作）
     */
    private int increaseStock(Long skuId, Integer quantity) {
        LambdaUpdateWrapper<PmsSku> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsSku::getId, skuId)
               .setSql("stock = stock + " + quantity)
               .set(PmsSku::getUpdateTime, LocalDateTime.now());
        return skuMapper.update(null, wrapper);
    }

    private List<PmsSkuVo> convertToVoList(List<PmsSku> skuList) {
        List<PmsSkuVo> skuVoList = new ArrayList<>();
        for (PmsSku sku : skuList) {
            skuVoList.add(convertToVo(sku));
        }
        return skuVoList;
    }

    private PmsSkuVo convertToVo(PmsSku sku) {
        PmsSkuVo skuVo = new PmsSkuVo();
        BeanUtils.copyProperties(sku, skuVo);

        LambdaQueryWrapper<PmsSkuAttr> attrWrapper = new LambdaQueryWrapper<>();
        attrWrapper.eq(PmsSkuAttr::getSkuId, sku.getId());
        List<PmsSkuAttr> skuAttrList = skuAttrMapper.selectList(attrWrapper);

        List<PmsSkuAttrVo> skuAttrVoList = new ArrayList<>();
        for (PmsSkuAttr skuAttr : skuAttrList) {
            PmsSkuAttrVo skuAttrVo = new PmsSkuAttrVo();
            BeanUtils.copyProperties(skuAttr, skuAttrVo);
            skuAttrVoList.add(skuAttrVo);
        }
        skuVo.setSkuAttrList(skuAttrVoList);

        return skuVo;
    }
}
