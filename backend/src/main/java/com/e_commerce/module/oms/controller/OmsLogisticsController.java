package com.e_commerce.module.oms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.oms.dto.OmsLogisticsDto;
import com.e_commerce.module.oms.dto.OmsLogisticsTraceDto;
import com.e_commerce.module.oms.entity.OmsLogistics;
import com.e_commerce.module.oms.mapper.OmsLogisticsMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import com.e_commerce.module.oms.service.OmsLogisticsService;
import com.e_commerce.module.oms.vo.LogisticsTraceVo;
import com.e_commerce.module.oms.vo.LogisticsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物流管理控制器
 * 提供物流相关的API接口，包括创建物流、查看物流列表、查看物流详情、更新物流信息等功能
 */
@RestController
@RequestMapping("/oms/logistics")
@Slf4j
public class OmsLogisticsController {

    @Autowired
    private OmsLogisticsService logisticsService;

    @Autowired
    private OmsOrderMapper omsOrderMapper;
    
    @Autowired
    private OmsLogisticsMapper omsLogisticsMapper;

    // 管理员：创建物流
    @PostMapping("/create")
    public Result<String> createLogistics(@RequestBody OmsLogisticsDto dto) {
        logisticsService.createLogistics(dto);
        return Result.success("物流创建成功");
    }

    // 用户：查看自己的物流列表
    @GetMapping("/user/list")
    public Result<List<LogisticsVo>> userList() {
        Long userId = ThreadLocalUtil.getUserId();
        return Result.success(logisticsService.getUserLogisticsList(userId));
    }

    // 用户：物流列表（条件查询）
//    @PostMapping("/user/list")
//    public Result<List<LogisticsVo>> userListByCondition(@RequestBody Map<String, Object> params) {
//        Long userId = ThreadLocalUtil.getUserId();
//        Integer status = (Integer) params.get("status");
//        return Result.success(logisticsService.getUserLogisticsListByCondition(userId, status));
//    }

    // 用户支付成功：自动添加物流轨迹
    @PostMapping("/user/addTrace")
    public Result<String> addAutoTrace(@RequestBody Map<String, Long> params) {
        Long orderId = params.get("orderId");
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }
        logisticsService.addAutoTrace(orderId);
        return Result.success("轨迹添加成功");
    }

    //签收
    @PostMapping("/user/sign")
    public Result<String> sign(@RequestParam Long orderId) {
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }
        OmsLogistics logistics = omsLogisticsMapper.selectOne(
                new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getOrderId, orderId));
        if (logistics == null) {
            return Result.error("该订单暂无物流信息");
        }
        logisticsService.sign(logistics.getId());
        return Result.success("签收成功");
    }

    // 查看物流详情（含轨迹）
    @GetMapping("/detail")
    public Result<LogisticsVo> detail(@RequestParam Long orderId) {
        Long userId = ThreadLocalUtil.getUserId();
        return Result.success(logisticsService.getLogisticsDetail(userId, orderId));
    }

    // 管理员：更新物流信息（快递公司）
    @PostMapping("/admin/updateDelivery")
    public Result<String> updateDelivery(@RequestBody Map<String, Object> param) {
        // 安全地转成 Long
        Object orderIdObj = param.get("orderId");
        Long orderId = Long.valueOf(orderIdObj.toString());

        String deliveryCompany = (String) param.get("deliveryCompany");
        logisticsService.updateDeliveryInfo(orderId, deliveryCompany);
        return Result.success("更新成功");
    }

    // 管理员：更新物流状态
    @PostMapping("/admin/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Integer status = (Integer) param.get("status");
        logisticsService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    // 管理员：添加物流轨迹
    @PostMapping("/admin/addTrace")
    public Result<String> addTrace(@RequestBody OmsLogisticsTraceDto dto) {
        logisticsService.addTrace(dto);
        return Result.success("轨迹添加成功");
    }

    // 管理员：全部物流（支持条件查询）
    @PostMapping("/admin/listAll")
    public Result<List<LogisticsVo>> listAll(@RequestBody Map<String, Object> params) {
        String orderSn = (String) params.get("orderSn");
        String deliveryNo = (String) params.get("deliveryNo");
        String deliveryCompany = (String) params.get("deliveryCompany");
        Integer status = (Integer) params.get("status");
//        System.out.println("前端传过来的 orderSn：" + orderSn);
//        System.out.println("前端传过来的 deliveryNo：" + deliveryNo);
//        System.out.println("前端传过来的 deliveryCompany：" + deliveryCompany);
//        System.out.println("前端传过来的 status：" + status);

        return Result.success(logisticsService.getListByCondition(orderSn, deliveryNo, deliveryCompany, status));
    }

    // 用户：根据订单ID查看物流轨迹列表
    @GetMapping("/user/trace/list")
    public Result<List<LogisticsTraceVo>> userTraceList(@RequestParam Long orderId) {
        Long userId = ThreadLocalUtil.getUserId();
        return Result.success(logisticsService.getTraceListByOrderId(userId, orderId));
    }


    // 管理员：根据物流ID查看轨迹列表
    @GetMapping("/admin/trace/list")
    public Result<List<LogisticsTraceVo>> adminTraceList(@RequestParam Long logisticsId) {
        return Result.success(logisticsService.getTraceListByLogisticsId(logisticsId));
    }

    // 管理员：删除物流轨迹
    @PostMapping("/admin/trace/delete")
    public Result<String> deleteTrace(@RequestBody Map<String, Long> param) {
        Long id = param.get("id");
        logisticsService.deleteTrace(id);
        return Result.success("轨迹删除成功");
    }
}