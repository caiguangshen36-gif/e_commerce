package com.e_commerce.module.oms.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.oms.dto.OmsSettleDirectDto;
import com.e_commerce.module.oms.dto.OmsSettleDto;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.service.OmsSettleService;
import com.e_commerce.module.oms.vo.OmsSettleVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 结算单控制器
 */
@RestController
@RequestMapping("/settle")
public class OmsSettleController {

    @Autowired
    private OmsSettleService settleService;

    /**
     * 创建结算单
     */
    @PostMapping("/create")
    public Result<OmsSettleVo> createSettle(@RequestBody @Valid OmsSettleDto dto) {
        Long userId = ThreadLocalUtil.getUserId();
        OmsSettleVo vo = settleService.createSettle(userId, dto);
        return Result.success(vo);
    }

    @PostMapping("/createDirect")
    public Result<OmsSettleVo> createDirect(@RequestBody @Valid OmsSettleDirectDto dto) {
        Long userId = ThreadLocalUtil.getUserId();
        OmsSettleVo vo = settleService.createSettleDirect(userId, dto);
        return Result.success(vo);
    }

    /**
     * 获取结算单详情
     */
    @PostMapping("/detail")
    public Result<OmsSettleVo> getSettleDetail(@RequestBody Map<String, Long> request) {
        Long settleId = request.get("id");
        OmsSettleVo vo = settleService.getSettleDetail(settleId);
        return Result.success(vo);
    }

    /**
     * 确认结算单（生成正式订单）
     */
    @PostMapping("/confirm")
    public Result<OmsOrder> confirmSettle(@RequestBody Map<String, Long> request) {
        Long settleId = request.get("id");
        OmsOrder omsOrder = settleService.confirmSettle(settleId);
        return Result.success(omsOrder);
    }

    /**
     * 取消结算单
     */
    @PostMapping("/cancel")
    public Result<String> cancelSettle(@RequestBody Map<String, Long> request) {
        Long settleId = request.get("id");
        settleService.cancelSettle(settleId);
        return Result.success("取消成功");
    }

    /**
     * 获取我的结算单列表
     */
    @PostMapping("/list")
    public Result<List<OmsSettleVo>> getSettleList() {
        Long userId = ThreadLocalUtil.getUserId();
        List<OmsSettleVo> list = settleService.getSettleList(userId);
        return Result.success(list);
    }
}