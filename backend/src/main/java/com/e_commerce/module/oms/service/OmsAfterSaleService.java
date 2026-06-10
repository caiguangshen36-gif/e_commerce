package com.e_commerce.module.oms.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.oms.dto.OmsAfterSaleDeliveryDto;
import com.e_commerce.module.oms.dto.OmsAfterSaleDto;
import com.e_commerce.module.oms.entity.OmsAfterSale;
import com.e_commerce.module.oms.entity.OmsAfterSaleDelivery;
import com.e_commerce.module.oms.entity.OmsOrderItem;
import com.e_commerce.module.oms.mapper.OmsAfterSaleDeliveryMapper;
import com.e_commerce.module.oms.mapper.OmsAfterSaleMapper;
import com.e_commerce.module.oms.mapper.OmsOrderItemMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import com.e_commerce.module.oms.vo.AfterSaleDeliveryVo;
import com.e_commerce.module.oms.vo.AfterSaleVo;
import com.e_commerce.module.user.service.UmsUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OmsAfterSaleService {

    @Autowired
    private OmsAfterSaleMapper afterSaleMapper;

    @Autowired
    private OmsAfterSaleDeliveryMapper afterSaleDeliveryMapper;

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private UmsUserService umsUserService;

    @Transactional
    public String createAfterSale(Long userId, OmsAfterSaleDto dto) {
        OmsOrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        if (item == null) throw new RuntimeException("订单项不存在");

        String sn = "AF" + System.currentTimeMillis();

        OmsAfterSale afterSale = new OmsAfterSale();
        afterSale.setUserId(userId);
        afterSale.setOrderId(dto.getOrderId());
        afterSale.setOrderItemId(dto.getOrderItemId());
        afterSale.setProductId(item.getProductId());
        afterSale.setAfterSaleSn(sn);
        afterSale.setType(dto.getType());
        afterSale.setReason(dto.getReason());
        afterSale.setDescription(dto.getDescription());
        afterSale.setRefundAmount(dto.getRefundAmount());
        afterSale.setStatus(0);

        afterSaleMapper.insert(afterSale);
        return sn;
    }

    public List<AfterSaleVo> getUserAfterSaleList(Long userId) {
        List<OmsAfterSale> list = afterSaleMapper.selectList(
                new LambdaQueryWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getUserId, userId)
                        .orderByDesc(OmsAfterSale::getCreateTime));
        return list.stream().map(this::convert).toList();
    }

    public PageVo<AfterSaleVo> getUserAfterSaleListByCondition(Long pageNum, Long pageSize, Long userId, String afterSaleSn, Integer status) {
        Page<OmsAfterSale> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsAfterSale> wrapper = new LambdaQueryWrapper<OmsAfterSale>()
                .eq(OmsAfterSale::getUserId, userId);
        if (afterSaleSn != null && !afterSaleSn.isEmpty()) {
            wrapper.like(OmsAfterSale::getAfterSaleSn, afterSaleSn);
        }
        if (status != null) {
            wrapper.eq(OmsAfterSale::getStatus, status);
        }
        wrapper.orderByDesc(OmsAfterSale::getCreateTime);
        Page<OmsAfterSale> result = afterSaleMapper.selectPage(mpPage, wrapper);

        List<AfterSaleVo> voList = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        PageVo<AfterSaleVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public AfterSaleVo getAfterSaleDetail(Long userId, Long id) {
        OmsAfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null || !afterSale.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        return convert(afterSale);
    }

    @Transactional
    public void adminApprove(Long id) {
        OmsAfterSale as = afterSaleMapper.selectById(id);
        if (as == null || as.getStatus() != 0) throw new RuntimeException("状态错误");
        afterSaleMapper.update(null,
                new LambdaUpdateWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getId, id)
                        .set(OmsAfterSale::getStatus, 1)
                        .set(OmsAfterSale::getAuditTime, LocalDateTime.now()));
    }

    @Transactional
    public void adminReject(Long id, String reason) {
        OmsAfterSale as = afterSaleMapper.selectById(id);
        if (as == null || as.getStatus() != 0) throw new RuntimeException("状态错误");
        afterSaleMapper.update(null,
                new LambdaUpdateWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getId, id)
                        .set(OmsAfterSale::getStatus, 3)
                        .set(OmsAfterSale::getAuditTime, LocalDateTime.now())
                        .set(OmsAfterSale::getRejectReason, reason));
    }

    @Transactional
    public void adminRefund(Long id) {
        OmsAfterSale as = afterSaleMapper.selectById(id);
        if (as == null) throw new RuntimeException("需先审核通过");
        BigDecimal refundAmount = as.getRefundAmount();
        Long userId = as.getUserId();
        umsUserService.updateAddBalance(userId, refundAmount);
        afterSaleMapper.update(null,
                new LambdaUpdateWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getId, id)
                        .set(OmsAfterSale::getStatus, 2)
                        .set(OmsAfterSale::getRefundTime, LocalDateTime.now()));
    }

    @Transactional
    public void userReturnGoods(OmsAfterSaleDeliveryDto dto) {
        OmsAfterSale as = afterSaleMapper.selectById(dto.getAfterSaleId());
        if (as == null) {
            throw new RuntimeException("售后单不存在");
        }
        if (as.getStatus() != 1) {
            throw new RuntimeException("仅审核通过的售后单可退货");
        }

        afterSaleMapper.update(null,
                new LambdaUpdateWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getId, dto.getAfterSaleId())
                        .set(OmsAfterSale::getStatus, 4));

        OmsAfterSaleDelivery delivery = new OmsAfterSaleDelivery();
        delivery.setAfterSaleId(dto.getAfterSaleId());
        delivery.setDeliveryCompany(dto.getDeliveryCompany());

        String autoDeliveryNo = "WL" + System.currentTimeMillis();
        delivery.setDeliveryNo(autoDeliveryNo);

        delivery.setStatus(1);
        delivery.setSendTime(LocalDateTime.now());

        afterSaleDeliveryMapper.insert(delivery);
    }

    public void userRefund(Long id) {
    }

    @Transactional
    public void merchantReceive(Long id) {
        OmsAfterSale as = afterSaleMapper.selectById(id);
        if (as == null || as.getStatus() != 4) throw new RuntimeException("未退货");

        afterSaleMapper.update(null,
                new LambdaUpdateWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getId, id)
                        .set(OmsAfterSale::getStatus, 5));

        OmsAfterSaleDelivery delivery = afterSaleDeliveryMapper.selectOne(
                new LambdaQueryWrapper<OmsAfterSaleDelivery>().eq(OmsAfterSaleDelivery::getAfterSaleId, id));
        if (delivery != null) {
            afterSaleDeliveryMapper.update(null,
                    new LambdaUpdateWrapper<OmsAfterSaleDelivery>()
                            .eq(OmsAfterSaleDelivery::getId, delivery.getId())
                            .set(OmsAfterSaleDelivery::getReceiveTime, LocalDateTime.now())
                            .set(OmsAfterSaleDelivery::getStatus, 2));
        }
    }

    public List<AfterSaleVo> getAllList() {
        return afterSaleMapper.selectList(
                new LambdaQueryWrapper<OmsAfterSale>().orderByDesc(OmsAfterSale::getCreateTime))
                .stream().map(this::convert).toList();
    }

    public PageVo<AfterSaleVo> getListByCondition(Long pageNum, Long pageSize, String afterSaleSn, Integer status) {
        Page<OmsAfterSale> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsAfterSale> wrapper = new LambdaQueryWrapper<>();
        if (afterSaleSn != null && !afterSaleSn.isEmpty()) {
            wrapper.like(OmsAfterSale::getAfterSaleSn, afterSaleSn);
        }
        if (status != null) {
            wrapper.eq(OmsAfterSale::getStatus, status);
        }
        wrapper.orderByDesc(OmsAfterSale::getCreateTime);
        Page<OmsAfterSale> result = afterSaleMapper.selectPage(mpPage, wrapper);

        List<AfterSaleVo> voList = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        PageVo<AfterSaleVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public List<AfterSaleVo> getPendingReviewList() {
        return afterSaleMapper.selectList(
                new LambdaQueryWrapper<OmsAfterSale>()
                        .eq(OmsAfterSale::getStatus, 0)
                        .orderByDesc(OmsAfterSale::getCreateTime))
                .stream().map(this::convert).toList();
    }

    public AfterSaleDeliveryVo getDeliveryByAfterSaleId(Long afterSaleId) {
        OmsAfterSaleDelivery delivery = afterSaleDeliveryMapper.selectOne(
                new LambdaQueryWrapper<OmsAfterSaleDelivery>().eq(OmsAfterSaleDelivery::getAfterSaleId, afterSaleId));
        if (delivery == null) return null;
        return convertDelivery(delivery);
    }

    private AfterSaleVo convert(OmsAfterSale as) {
        AfterSaleVo vo = new AfterSaleVo();
        BeanUtil.copyProperties(as, vo);
        vo.setStatusText(getStatus(as.getStatus()));
        vo.setTypeText(as.getType() == 1 ? "仅退款" : "退货退款");

        if (as.getStatus() >= 4) {
            OmsAfterSaleDelivery delivery = afterSaleDeliveryMapper.selectOne(
                    new LambdaQueryWrapper<OmsAfterSaleDelivery>().eq(OmsAfterSaleDelivery::getAfterSaleId, as.getId()));
            if (delivery != null) {
                vo.setDelivery(convertDelivery(delivery));
            }
        }
        return vo;
    }

    private AfterSaleDeliveryVo convertDelivery(OmsAfterSaleDelivery delivery) {
        AfterSaleDeliveryVo vo = new AfterSaleDeliveryVo();
        BeanUtil.copyProperties(delivery, vo);
        vo.setStatusText(getDeliveryStatus(delivery.getStatus()));
        return vo;
    }

    private String getDeliveryStatus(Integer s) {
        return switch (s) {
            case 0 -> "未发货";
            case 1 -> "已发货";
            case 2 -> "已签收";
            default -> "未知";
        };
    }

    private String getStatus(Integer s) {
        return switch (s) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "已退款";
            case 3 -> "已驳回";
            case 4 -> "用户已退货";
            case 5 -> "商家已收货";
            default -> "未知";
        };
    }
}
