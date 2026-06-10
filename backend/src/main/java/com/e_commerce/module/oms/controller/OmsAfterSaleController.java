package com.e_commerce.module.oms.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.oms.dto.OmsAfterSaleDeliveryDto;
import com.e_commerce.module.oms.dto.OmsAfterSaleDto;
import com.e_commerce.module.oms.service.OmsAfterSaleService;
import com.e_commerce.module.oms.vo.AfterSaleDeliveryVo;
import com.e_commerce.module.oms.vo.AfterSaleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 售后管理控制器
 * 处理售后相关的HTTP请求，包括创建售后、查询售后、审核售后等功能
 */
@RestController
@RequestMapping("/oms/after-sale")
@Slf4j
public class OmsAfterSaleController {

    @Autowired
    private OmsAfterSaleService afterSaleService;

    // 创建售后
    @PostMapping("/create")
    public Result<String> createAfterSale(@RequestBody OmsAfterSaleDto dto) {
        Long userId = ThreadLocalUtil.getUserId();
        String afterSaleSn = afterSaleService.createAfterSale(userId, dto);
        return Result.success("申请成功，售后单号：" + afterSaleSn);
    }


    // 我的售后列表（条件查询）
    @PostMapping("/user/list")
    public Result<PageVo<AfterSaleVo>> userListByCondition(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String afterSaleSn = (String) params.get("afterSaleSn");
        Integer status = (Integer) params.get("status");
        return Result.success(afterSaleService.getUserAfterSaleListByCondition(pageNum, pageSize, userId, afterSaleSn, status));
    }

    // 售后详情
    @GetMapping("/detail")
    public Result<AfterSaleVo> detail(@RequestParam Long id) {
        Long userId = ThreadLocalUtil.getUserId();
        return Result.success(afterSaleService.getAfterSaleDetail(userId, id));
    }


    // 管理员 - 审核通过
    @PostMapping("/admin/approve")
    public Result<String> approve(@RequestBody Map<String, Long> param) {
        afterSaleService.adminApprove(param.get("id"));
        return Result.success("审核通过");
    }

    // 管理员 - 驳回
    @PostMapping("/admin/reject")
    public Result<String> reject(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        String reason = param.get("rejectReason").toString();
        afterSaleService.adminReject(id, reason);
        return Result.success("已驳回");
    }

    // 管理员 - 退款
    @PostMapping("/admin/refund")
    public Result<String> refund(@RequestBody Map<String, Long> param) {
        afterSaleService.adminRefund(param.get("id"));
        return Result.success("退款成功");
    }

    // 用户 - 退货提交物流
    @PostMapping("/user/return")
    public Result<String> userReturn(@RequestBody OmsAfterSaleDeliveryDto dto) {
        afterSaleService.userReturnGoods(dto);
        return Result.success("退货成功");
    }

    // 管理员 - 确认收货
    @PostMapping("/admin/receive")
    public Result<String> receive(@RequestBody Map<String, Long> param) {
        afterSaleService.merchantReceive(param.get("id"));
        return Result.success("已收货");
    }

    // 查询物流信息
    @GetMapping("/delivery")
    public Result<AfterSaleDeliveryVo> getDelivery(@RequestParam Long afterSaleId) {
        return Result.success(afterSaleService.getDeliveryByAfterSaleId(afterSaleId));
    }

    // 管理员 - 所有售后（支持条件查询）
    @PostMapping("/admin/list")
    public Result<PageVo<AfterSaleVo>> listAll(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String afterSaleSn = (String) params.get("afterSaleSn");
        Integer status = (Integer) params.get("status");

        System.out.println("前端传过来的 afterSaleSn：" + afterSaleSn);
        System.out.println("前端传过来的 status：" + status);

        return Result.success(afterSaleService.getListByCondition(pageNum, pageSize, afterSaleSn, status));
    }

    // 管理员 - 待审核售后列表
    @GetMapping("/admin/pendingReview")
    public Result<List<AfterSaleVo>> pendingReview() {
        return Result.success(afterSaleService.getPendingReviewList());
    }
}