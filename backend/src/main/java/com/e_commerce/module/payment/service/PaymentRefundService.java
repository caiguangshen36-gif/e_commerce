package com.e_commerce.module.payment.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.payment.dto.PaymentRefundDto;
import com.e_commerce.module.payment.entity.PaymentRefund;
import com.e_commerce.module.payment.mapper.PaymentRefundMapper;
import com.e_commerce.module.payment.vo.PaymentRefundVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentRefundService {

    @Autowired
    private PaymentRefundMapper refundMapper;

    @Transactional
    public String createRefund(PaymentRefundDto dto) {
        String refundNo = "REF" + System.currentTimeMillis();
        PaymentRefund refund = new PaymentRefund();
        refund.setTransactionId(dto.getTransactionId());
        refund.setRefundNo(refundNo);
        refund.setRefundAmount(dto.getRefundAmount());
        refund.setReason(dto.getReason());
        refund.setStatus(0);
        refundMapper.insert(refund);
        return refundNo;
    }

    public PaymentRefundVo getById(Long id) {
        PaymentRefund refund = refundMapper.selectById(id);
        if (refund == null) return null;
        return convert(refund);
    }

    public PaymentRefundVo getByRefundNo(String refundNo) {
        PaymentRefund refund = refundMapper.selectOne(
                new LambdaQueryWrapper<PaymentRefund>().eq(PaymentRefund::getRefundNo, refundNo));
        if (refund == null) return null;
        return convert(refund);
    }

    public List<PaymentRefundVo> getByTransactionId(Long transactionId) {
        return refundMapper.selectList(
                new LambdaQueryWrapper<PaymentRefund>().eq(PaymentRefund::getTransactionId, transactionId))
                .stream().map(this::convert).toList();
    }

    @Transactional
    public void refundSuccess(Long id) {
        refundMapper.update(null,
                new LambdaUpdateWrapper<PaymentRefund>()
                        .eq(PaymentRefund::getId, id)
                        .set(PaymentRefund::getStatus, 1)
                        .set(PaymentRefund::getRefundTime, LocalDateTime.now()));
    }

    @Transactional
    public void refundFail(Long id) {
        refundMapper.update(null,
                new LambdaUpdateWrapper<PaymentRefund>()
                        .eq(PaymentRefund::getId, id)
                        .set(PaymentRefund::getStatus, 2));
    }

    public PageVo<PaymentRefundVo> getAllList(Long pageNum, Long pageSize) {
        Page<PaymentRefund> mpPage = new Page<>(pageNum, pageSize);
        Page<PaymentRefund> result = refundMapper.selectPage(mpPage,
                new LambdaQueryWrapper<PaymentRefund>().orderByDesc(PaymentRefund::getCreateTime));

        List<PaymentRefundVo> voList = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        PageVo<PaymentRefundVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public PageVo<PaymentRefundVo> getListByStatus(Long pageNum, Long pageSize, Integer status) {
        Page<PaymentRefund> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PaymentRefund> wrapper = new LambdaQueryWrapper<PaymentRefund>()
                .orderByDesc(PaymentRefund::getCreateTime);
        if (status != null) {
            wrapper.eq(PaymentRefund::getStatus, status);
        }
        Page<PaymentRefund> result = refundMapper.selectPage(mpPage, wrapper);

        List<PaymentRefundVo> voList = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        PageVo<PaymentRefundVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    private PaymentRefundVo convert(PaymentRefund refund) {
        PaymentRefundVo vo = new PaymentRefundVo();
        BeanUtil.copyProperties(refund, vo);
        vo.setStatusText(getStatus(refund.getStatus()));
        return vo;
    }

    private String getStatus(Integer status) {
        return switch (status) {
            case 0 -> "申请中";
            case 1 -> "退款成功";
            case 2 -> "退款失败";
            default -> "未知";
        };
    }
}