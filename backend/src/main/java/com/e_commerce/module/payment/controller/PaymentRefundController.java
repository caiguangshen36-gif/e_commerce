package com.e_commerce.module.payment.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.payment.dto.PaymentRefundDto;
import com.e_commerce.module.payment.service.PaymentRefundService;
import com.e_commerce.module.payment.vo.PaymentRefundVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment/refund")
@Slf4j
public class PaymentRefundController {

    @Autowired
    private PaymentRefundService refundService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody PaymentRefundDto dto) {
        String refundNo = refundService.createRefund(dto);
        return Result.success("创建成功，退款单号：" + refundNo);
    }

    @PostMapping("/detail")
    public Result<PaymentRefundVo> getById(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        return Result.success(refundService.getById(id));
    }

    @PostMapping("/getByNo")
    public Result<PaymentRefundVo> getByRefundNo(@RequestBody Map<String, String> params) {
        String refundNo = params.get("refundNo");
        return Result.success(refundService.getByRefundNo(refundNo));
    }

    @PostMapping("/getByTransactionId")
    public Result<List<PaymentRefundVo>> getByTransactionId(@RequestBody Map<String, Long> params) {
        Long transactionId = params.get("transactionId");
        return Result.success(refundService.getByTransactionId(transactionId));
    }

    @PostMapping("/success")
    public Result<String> refundSuccess(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        refundService.refundSuccess(id);
        return Result.success("退款成功");
    }

    @PostMapping("/fail")
    public Result<String> refundFail(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        refundService.refundFail(id);
        return Result.success("退款失败");
    }

    @GetMapping("/list")
    public Result<PageVo<PaymentRefundVo>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                 @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(refundService.getAllList(pageNum, pageSize));
    }

    @PostMapping("/listByStatus")
    public Result<PageVo<PaymentRefundVo>> listByStatus(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;
        return Result.success(refundService.getListByStatus(pageNum, pageSize, status));
    }
}