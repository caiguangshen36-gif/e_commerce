package com.e_commerce.module.oms.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.oms.dto.OmsOrderDto;
import com.e_commerce.module.oms.dto.OmsOrderItemDto;
import com.e_commerce.module.oms.dto.OmsSettleDirectDto;
import com.e_commerce.module.oms.dto.OmsSettleDto;
import com.e_commerce.module.oms.entity.OmsCart;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.entity.OmsSettle;
import com.e_commerce.module.oms.entity.OmsSettleItem;
import com.e_commerce.module.oms.mapper.OmsCartMapper;
import com.e_commerce.module.oms.mapper.OmsSettleItemMapper;
import com.e_commerce.module.oms.mapper.OmsSettleMapper;
import com.e_commerce.module.oms.vo.OmsSettleItemVo;
import com.e_commerce.module.oms.vo.OmsSettleVo;
import com.e_commerce.module.product.entity.PmsProduct;
import com.e_commerce.module.product.entity.PmsSku;
import com.e_commerce.module.product.mapper.PmsProductMapper;
import com.e_commerce.module.product.mapper.PmsSkuMapper;
import com.e_commerce.module.user.entity.UmsAddress;
import com.e_commerce.module.user.mapper.UmsAddressMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OmsSettleService {

    @Autowired
    private OmsSettleMapper settleMapper;

    @Autowired
    private OmsSettleItemMapper settleItemMapper;

    @Autowired
    private OmsCartMapper cartMapper;

    @Autowired
    private PmsSkuMapper skuMapper;

    @Autowired
    private PmsProductMapper productMapper;

    @Autowired
    private UmsAddressMapper addressMapper;

    @Autowired
    private OmsOrderService orderService;

    @Transactional
    public OmsSettleVo createSettle(Long userId, OmsSettleDto dto) {
        List<OmsCart> cartList = cartMapper.selectList(
                new LambdaQueryWrapper<OmsCart>()
                        .eq(OmsCart::getUserId, userId)
                        .in(OmsCart::getId, dto.getCartIds()));

        if (CollUtil.isEmpty(cartList)) {
            throw new RuntimeException("购物车为空或商品不存在");
        }

        UmsAddress address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OmsSettleItem> settleItems = new ArrayList<>();

        for (OmsCart cart : cartList) {
            PmsSku sku = skuMapper.selectById(cart.getSkuId());
            if (sku == null) {
                throw new RuntimeException("商品SKU不存在");
            }

            PmsProduct product = productMapper.selectById(sku.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            if (sku.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足：" + product.getProductName());
            }

            BigDecimal itemTotal = sku.getPrice().multiply(new BigDecimal(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OmsSettleItem item = new OmsSettleItem();
            item.setUserId(userId);
            item.setProductId(sku.getProductId());
            item.setSkuId(sku.getId());
            item.setProductName(product.getProductName());
            item.setSkuSpecs(sku.getSkuCode());
            item.setPic(sku.getPic());
            item.setPrice(sku.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(itemTotal);
            settleItems.add(item);
        }

        OmsSettle settle = new OmsSettle();
        settle.setUserId(userId);
        settle.setAddressId(dto.getAddressId());
        settle.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        settle.setStatus(0);
        settleMapper.insert(settle);

        for (OmsSettleItem item : settleItems) {
            item.setSettleId(settle.getId());
            settleItemMapper.insert(item);
        }

        return getSettleDetail(settle.getId());
    }

    @Transactional
    public OmsSettleVo createSettleDirect(Long userId, OmsSettleDirectDto dto) {
        PmsProduct product = productMapper.selectById(dto.getProductId());
        PmsSku sku = skuMapper.selectById(dto.getSkuId());

        BigDecimal totalAmount = sku.getPrice().multiply(new BigDecimal(dto.getQuantity()));

        OmsSettle settle = new OmsSettle();
        settle.setUserId(userId);
        settle.setAddressId(dto.getAddressId());
        settle.setTotalAmount(totalAmount);
        settle.setStatus(0);
        settleMapper.insert(settle);

        OmsSettleItem item = new OmsSettleItem();
        item.setSettleId(settle.getId());
        item.setUserId(userId);
        item.setProductId(product.getId());
        item.setSkuId(sku.getId());
        item.setProductName(product.getProductName());
        item.setSkuSpecs(sku.getSkuCode());
        item.setPic(product.getPic());
        item.setPrice(sku.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setTotalPrice(totalAmount);

        settleItemMapper.insert(item);

        OmsSettleVo vo = new OmsSettleVo();
        vo.setId(settle.getId());
        vo.setTotalAmount(settle.getTotalAmount());
        return vo;
    }

    public OmsSettleVo getSettleDetail(Long settleId) {
        OmsSettle settle = settleMapper.selectById(settleId);
        if (settle == null) {
            throw new RuntimeException("结算单不存在");
        }

        OmsSettleVo vo = new OmsSettleVo();
        BeanUtil.copyProperties(settle, vo);
        vo.setStatusText(getStatusText(settle.getStatus()));

        UmsAddress address = addressMapper.selectById(settle.getAddressId());
        if (address != null) {
            vo.setReceiver(address.getReceiver());
            vo.setPhone(address.getPhone());
            vo.setAddress(address.getProvince() + address.getCity() + address.getArea() + address.getDetail());
        }

        List<OmsSettleItem> items = settleItemMapper.selectList(
                new LambdaQueryWrapper<OmsSettleItem>().eq(OmsSettleItem::getSettleId, settleId));
        List<OmsSettleItemVo> itemVos = new ArrayList<>();
        for (OmsSettleItem item : items) {
            OmsSettleItemVo itemVo = new OmsSettleItemVo();
            BeanUtil.copyProperties(item, itemVo);
            itemVos.add(itemVo);
        }
        vo.setItems(itemVos);

        return vo;
    }

    @Transactional
    public OmsOrder confirmSettle(Long settleId) {
        OmsSettle settle = settleMapper.selectById(settleId);
        if (settle == null) {
            throw new RuntimeException("结算单不存在");
        }

        List<OmsSettleItem> items = settleItemMapper.selectList(
                new LambdaQueryWrapper<OmsSettleItem>().eq(OmsSettleItem::getSettleId, settleId));
        if (items.isEmpty()) {
            throw new RuntimeException("结算单明细为空");
        }

        OmsOrderDto orderDto = new OmsOrderDto();
        orderDto.setAddressId(settle.getAddressId());

        List<OmsOrderItemDto> orderItemList = new ArrayList<>();
        for (OmsSettleItem item : items) {
            OmsOrderItemDto itemDto = new OmsOrderItemDto();
            itemDto.setSkuId(item.getSkuId());
            itemDto.setQuantity(item.getQuantity());
            orderItemList.add(itemDto);
        }
        orderDto.setOrderItemList(orderItemList);

        int updateCount = settleMapper.update(null,
                new LambdaUpdateWrapper<OmsSettle>()
                        .eq(OmsSettle::getId, settleId)
                        .eq(OmsSettle::getStatus, 0)
                        .set(OmsSettle::getStatus, 1));
        if (updateCount == 0) {
            throw new RuntimeException("结算单状态已变更，请刷新页面后重试");
        }

        try {
            OmsOrder order = orderService.createOrder(settle.getUserId(), orderDto);
            return order;
        } catch (Exception e) {
            throw new RuntimeException("订单创建失败：" + e.getMessage());
        }
    }

    @Transactional
    public void cancelSettle(Long settleId) {
        OmsSettle settle = settleMapper.selectById(settleId);
        if (settle == null) {
            throw new RuntimeException("结算单不存在");
        }

        if (settle.getStatus() != 0) {
            throw new RuntimeException("结算单已处理，无法取消");
        }

        settleMapper.update(null,
                new LambdaUpdateWrapper<OmsSettle>()
                        .eq(OmsSettle::getId, settleId)
                        .set(OmsSettle::getStatus, 2));
    }

    public List<OmsSettleVo> getSettleList(Long userId) {
        List<OmsSettle> settleList = settleMapper.selectList(
                new LambdaQueryWrapper<OmsSettle>()
                        .eq(OmsSettle::getUserId, userId)
                        .orderByDesc(OmsSettle::getCreateTime));
        if (CollUtil.isEmpty(settleList)) {
            return new ArrayList<>();
        }

        List<OmsSettleVo> vos = new ArrayList<>();
        for (OmsSettle settle : settleList) {
            OmsSettleVo vo = new OmsSettleVo();
            BeanUtil.copyProperties(settle, vo);
            vo.setStatusText(getStatusText(settle.getStatus()));
            vos.add(vo);
        }
        return vos;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待确认";
            case 1: return "已转订单";
            case 2: return "已取消";
            default: return "未知状态";
        }
    }
}
