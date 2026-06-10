package com.e_commerce.module.payment.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.oms.service.OmsOrderService;
import com.e_commerce.module.oms.vo.OrderVo;
import com.e_commerce.module.payment.dto.PaymentTransactionDto;
import com.e_commerce.module.payment.entity.PaymentTransaction;
import com.e_commerce.module.payment.mapper.PaymentTransactionMapper;
import com.e_commerce.module.payment.vo.PaymentTransactionVo;
import com.e_commerce.module.product.service.PmsSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentTransactionService {

    @Autowired
    private PaymentTransactionMapper transactionMapper;

    @Autowired
    private PmsSkuService pmsSkuService;

    @Autowired
    private OmsOrderService omsOrderService;

    @Transactional
    public String createTransaction(PaymentTransactionDto dto) {
        String transactionNo = "PAY" + System.currentTimeMillis();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(dto.getOrderId());
        transaction.setTransactionNo(transactionNo);
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setAmount(dto.getAmount());
        transaction.setStatus(0);
        transactionMapper.insert(transaction);
        return transactionNo;
    }

    public PaymentTransactionVo getById(Long id) {
        PaymentTransaction transaction = transactionMapper.selectById(id);
        if (transaction == null) return null;
        return convert(transaction);
    }

    public PaymentTransactionVo getByOrderId(Long orderId) {
        PaymentTransaction transaction = transactionMapper.selectOne(
                new LambdaQueryWrapper<PaymentTransaction>().eq(PaymentTransaction::getOrderId, orderId));
        if (transaction == null) return null;
        return convert(transaction);
    }

    public PaymentTransactionVo getByTransactionNo(String transactionNo) {
        PaymentTransaction transaction = transactionMapper.selectOne(
                new LambdaQueryWrapper<PaymentTransaction>().eq(PaymentTransaction::getTransactionNo, transactionNo));
        if (transaction == null) return null;
        return convert(transaction);
    }

    @Transactional
    public void paySuccess(Long id) {
        transactionMapper.update(null,
                new LambdaUpdateWrapper<PaymentTransaction>()
                        .eq(PaymentTransaction::getId, id)
                        .set(PaymentTransaction::getStatus, 1)
                        .set(PaymentTransaction::getPayTime, LocalDateTime.now()));
    }

    @Transactional
    public void payFail(Long id, String callbackData) {
        transactionMapper.update(null,
                new LambdaUpdateWrapper<PaymentTransaction>()
                        .eq(PaymentTransaction::getId, id)
                        .set(PaymentTransaction::getStatus, 2)
                        .set(PaymentTransaction::getCallbackTime, LocalDateTime.now())
                        .set(PaymentTransaction::getCallbackData, callbackData));
    }

    @Transactional
    public void payCallbackSuccess(Long id, String callbackData) {
        // 获取支付记录
        PaymentTransaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            log.error("支付回调处理失败，找不到支付记录，id: {}", id);
            return;
        }

        // 获取订单信息
        OrderVo orderVo = omsOrderService.getOrderDetail(transaction.getOrderId());
        if (orderVo == null) {
            log.error("支付回调处理失败，找不到订单，orderId: {}", transaction.getOrderId());
            return;
        }

        // 更新支付状态
        transactionMapper.update(null,
                new LambdaUpdateWrapper<PaymentTransaction>()
                        .eq(PaymentTransaction::getId, id)
                        .set(PaymentTransaction::getStatus, 1)
                        .set(PaymentTransaction::getPayTime, LocalDateTime.now())
                        .set(PaymentTransaction::getCallbackTime, LocalDateTime.now())
                        .set(PaymentTransaction::getCallbackData, callbackData));

        // 确认扣减库存
        int deductedCount = pmsSkuService.confirmStockDeduction(orderVo.getOrderSn());
        log.info("支付成功，订单 {} 确认扣减库存 {} 项", orderVo.getOrderSn(), deductedCount);
    }

    public PaymentTransactionVo getSuccessByOrderId(Long orderId) {
        PaymentTransaction transaction = transactionMapper.selectOne(
                new LambdaQueryWrapper<PaymentTransaction>()
                        .eq(PaymentTransaction::getOrderId, orderId)
                        .eq(PaymentTransaction::getStatus, 1));
        if (transaction == null) return null;
        return convert(transaction);
    }

    public PageVo<PaymentTransactionVo> getAllList(Long pageNum, Long pageSize) {
        Page<PaymentTransaction> mpPage = new Page<>(pageNum, pageSize);
        Page<PaymentTransaction> result = transactionMapper.selectPage(mpPage,
                new LambdaQueryWrapper<PaymentTransaction>().orderByDesc(PaymentTransaction::getCreateTime));

        List<PaymentTransactionVo> voList = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        PageVo<PaymentTransactionVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public PageVo<PaymentTransactionVo> getListByStatus(Long pageNum, Long pageSize, Integer status) {
        Page<PaymentTransaction> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<PaymentTransaction>()
                .orderByDesc(PaymentTransaction::getCreateTime);
        if (status != null) {
            wrapper.eq(PaymentTransaction::getStatus, status);
        }
        Page<PaymentTransaction> result = transactionMapper.selectPage(mpPage, wrapper);

        List<PaymentTransactionVo> voList = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        PageVo<PaymentTransactionVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    private PaymentTransactionVo convert(PaymentTransaction transaction) {
        PaymentTransactionVo vo = new PaymentTransactionVo();
        BeanUtil.copyProperties(transaction, vo);
        vo.setPaymentMethodText(getPaymentMethod(transaction.getPaymentMethod()));
        vo.setStatusText(getStatus(transaction.getStatus()));
        return vo;
    }

    private String getPaymentMethod(Integer method) {
        return "余额支付";
    }

    private String getStatus(Integer status) {
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "支付成功";
            case 2 -> "支付失败";
            default -> "未知";
        };
    }
}