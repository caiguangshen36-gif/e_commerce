package com.e_commerce.module.payment.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.payment.dto.PaymentTransactionDto;
import com.e_commerce.module.payment.service.PaymentTransactionService;
import com.e_commerce.module.payment.vo.PaymentTransactionVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment/transaction")
@Slf4j
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionService transactionService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody PaymentTransactionDto dto) {
        String transactionNo = transactionService.createTransaction(dto);
        return Result.success("创建成功，交易单号：" + transactionNo);
    }

    @PostMapping("/detail")
    public Result<PaymentTransactionVo> getById(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        return Result.success(transactionService.getById(id));
    }

    @PostMapping("/getByOrderId")
    public Result<PaymentTransactionVo> getByOrderId(@RequestBody Map<String, Long> params) {
        Long orderId = params.get("orderId");
        return Result.success(transactionService.getByOrderId(orderId));
    }

    @PostMapping("/getByNo")
    public Result<PaymentTransactionVo> getByTransactionNo(@RequestBody Map<String, String> params) {
        String transactionNo = params.get("transactionNo");
        return Result.success(transactionService.getByTransactionNo(transactionNo));
    }

    @PostMapping("/success")
    public Result<String> paySuccess(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        transactionService.paySuccess(id);
        return Result.success("支付成功");
    }

    @PostMapping("/fail")
    public Result<String> payFail(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? ((Number) params.get("id")).longValue() : null;
        String callbackData = params.get("callbackData") != null ? params.get("callbackData").toString() : null;
        transactionService.payFail(id, callbackData);
        return Result.success("支付失败");
    }

    @PostMapping("/callback")
    public Result<String> callback(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? ((Number) params.get("id")).longValue() : null;
        String callbackData = params.get("callbackData") != null ? params.get("callbackData").toString() : null;
        transactionService.payCallbackSuccess(id, callbackData);
        return Result.success("回调成功");
    }

    @PostMapping("/getSuccessByOrderId")
    public Result<PaymentTransactionVo> getSuccessByOrderId(@RequestBody Map<String, Long> params) {
        Long orderId = params.get("orderId");
        return Result.success(transactionService.getSuccessByOrderId(orderId));
    }

    @GetMapping("/list")
    public Result<PageVo<PaymentTransactionVo>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                      @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(transactionService.getAllList(pageNum, pageSize));
    }

    @PostMapping("/listByStatus")
    public Result<PageVo<PaymentTransactionVo>> listByStatus(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;
        return Result.success(transactionService.getListByStatus(pageNum, pageSize, status));
    }
}