package com.e_commerce.module.oms.controller;

import cn.hutool.core.collection.CollUtil;
import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.oms.dto.OmsOrderDto;
import com.e_commerce.module.oms.dto.OrderCancelDto;
import com.e_commerce.module.oms.dto.OrderDeliverDto;
import com.e_commerce.module.oms.entity.OmsLogistics;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.entity.OmsOrderCancel;
import com.e_commerce.module.oms.mapper.OmsLogisticsMapper;
import com.e_commerce.module.oms.mapper.OmsOrderCancelMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import com.e_commerce.module.oms.service.OmsOrderService;
import com.e_commerce.module.oms.vo.OrderVo;
import com.e_commerce.module.oms.vo.ProductSalesVo;
import com.e_commerce.module.user.service.UmsUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OmsOrderController {

    @Autowired
    private OmsOrderService orderService;

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private UmsUserService umsUserService;

    @Autowired
    private OmsOrderCancelMapper omsOrderCancelMapper;


    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody OmsOrderDto orderDto) {
        Long userId = ThreadLocalUtil.getUserId();

        if (orderDto.getAddressId() == null) {
            return Result.error("收货地址不能为空");
        }
        if (CollUtil.isEmpty(orderDto.getOrderItemList())) {
            return Result.error("订单商品不能为空");
        }

        OmsOrder order = orderService.createOrder(userId, orderDto);

        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("orderSn", order.getOrderSn());

        return Result.success(data);
    }

//    @GetMapping("/list")
//    public Result<List<OrderVo>> getOrderList() {
//        Long userId = ThreadLocalUtil.getUserId();
//        List<OrderVo> list = orderService.getOrderVoList(userId);
//        return Result.success(list);
//    }

    @PostMapping("/list")
    public Result<PageVo<OrderVo>> getOrderListByCondition(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String orderSn = (String) params.get("orderSn");
        Integer status = (Integer) params.get("status");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        PageVo<OrderVo> page = orderService.getUserOrderListByCondition(pageNum, pageSize, userId, orderSn, status, startTime, endTime);
        return Result.success(page);
    }

    @GetMapping("/detail")
    public Result<OrderVo> orderDetail(@RequestParam Long orderId) {
        Long userId = ThreadLocalUtil.getUserId();
        OrderVo orderVo = orderService.getOrderDetail(userId, orderId);
        return Result.success(orderVo);
    }

    @PostMapping("/cancel")
    public Result<String> cancelOrder(@RequestBody OrderCancelDto dto) {
        Long userId = ThreadLocalUtil.getUserId();
        Long orderId = dto.getOrderId();

        // 1. 校验订单是否存在
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 2. 校验订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权限操作此订单");
        }

        // 3. 校验订单状态是否可取消（0待付款 /1待发货 /2待收货）
        if (order.getStatus() != 0 && order.getStatus() != 1 && order.getStatus() != 2) {
            return Result.error("该订单状态不可取消");
        }

        // 4. 执行取消订单（修改状态）
        orderService.cancelOrder(userId, orderId);

        // 5. 插入取消记录表
        OmsOrderCancel orderCancel = new OmsOrderCancel();
        orderCancel.setOrderId(orderId);
        orderCancel.setOrderSn(order.getOrderSn());
        orderCancel.setUserId(userId);
        orderCancel.setCancelReason(dto.getCancelReason());
        orderCancel.setCancelDescription(dto.getCancelDescription());
        omsOrderCancelMapper.insert(orderCancel);

        if(order.getStatus() == 1 || order.getStatus() == 2){
            //  退款
            umsUserService.updateAddBalance(userId, order.getTotalAmount());
        }
        return Result.success("取消成功");
    }

    @PostMapping("/confirm")
    public Result<String> confirmOrder(@RequestBody Map<String, Long> param) {
        Long userId = ThreadLocalUtil.getUserId();
        Long orderId = param.get("orderId");
        orderService.confirmOrder(userId, orderId);
        return Result.success("确认收货成功");
    }

    @PostMapping("/delete")
    public Result<String> deleteOrder(@RequestBody Map<String, Long> param) {
        Long userId = ThreadLocalUtil.getUserId();
        Long orderId = param.get("orderId");
        orderService.deleteOrder(userId, orderId);
        return Result.success("删除成功");
    }

    // 后端示例：专门给 Dashboard 用的统计接口
    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> getDashboardStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        Map<String, Object> stats = orderService.getDashboardStats(startTime, endTime);
        // stats 包含: todaySales, todayOrderCount, todayPaidOrder,
        //           weekTrend(List), statusDistribution(Map)
        return Result.success(stats);
    }

    @PostMapping("/admin/list")
    public Result<PageVo<OrderVo>> adminOrderList(@RequestBody Map<String, Object> params) {

        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String orderSn = (String) params.get("orderSn");
        Integer status = (Integer) params.get("status");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        System.out.println("前端传过来的 orderSn：" + orderSn);
        System.out.println("前端传过来的 status：" + status);

        PageVo<OrderVo> page = orderService.getAdminOrderList(pageNum, pageSize, orderSn, status, startTime, endTime);
        return Result.success(page);
    }

    @GetMapping("/admin/neverDeliver")
    public Result<List<OrderVo>> adminNeverDeliver(){
        return Result.success(orderService.getOrderNeverDelivery());
    }

    @GetMapping("/admin/detail")
    public Result<OrderVo> adminOrderDetail(@RequestParam Long orderId) {
        OrderVo orderVo = orderService.getOrderDetail(orderId);
        return Result.success(orderVo);
    }

    @PostMapping("/admin/deliver")
    public Result<String> deliverOrder(@RequestParam Long orderId) {
        orderService.deliverOrder(orderId);
        return Result.success("发货成功");
    }
    @PostMapping("/updateStatus")
    public Result<String> updateOrderStatus(@RequestBody Map<String, Object> params) {
        if (params == null || !params.containsKey("orderId") || !params.containsKey("status")) {
            return Result.error("参数缺失：orderId 和 status 必填");
        }
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        orderService.updateOrderStatus(orderId, status);
        return Result.success("状态更新成功");
    }

    @PostMapping("/admin/productSalesTop5")
    public Result<List<ProductSalesVo>> getProductSalesTop5(@RequestBody Map<String, Object> params) {
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        System.out.println("前端传过来的 startTime：" + startTime);
        System.out.println("前端传过来的 endTime：" + endTime);

        return Result.success(orderService.getProductSalesTop5(startTime, endTime));
    }
}