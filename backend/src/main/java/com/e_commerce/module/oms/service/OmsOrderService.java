package com.e_commerce.module.oms.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.oms.dto.OmsOrderDto;
import com.e_commerce.module.oms.dto.OmsOrderItemDto;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.entity.OmsOrderItem;
import com.e_commerce.module.oms.mapper.OmsOrderItemMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import com.e_commerce.module.oms.vo.DailyStatsVo;
import com.e_commerce.module.oms.vo.OrderItemVo;
import com.e_commerce.module.oms.vo.OrderVo;
import com.e_commerce.module.oms.vo.ProductSalesVo;
import com.e_commerce.module.product.entity.PmsProduct;
import com.e_commerce.module.product.entity.PmsSku;
import com.e_commerce.module.product.mapper.PmsProductMapper;
import com.e_commerce.module.product.mapper.PmsSkuMapper;
import com.e_commerce.module.product.service.PmsSkuService;
import com.e_commerce.module.user.entity.UmsAddress;
import com.e_commerce.module.user.mapper.UmsAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OmsOrderService {

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private UmsAddressMapper umsAddressMapper;

    @Autowired
    private PmsSkuMapper pmsSkuMapper;

    @Autowired
    private PmsProductMapper pmsProductMapper;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private PmsSkuService pmsSkuService;

    @Transactional
    public OmsOrder createOrder(Long userId, OmsOrderDto orderDto) {
        UmsAddress address = umsAddressMapper.selectById(orderDto.getAddressId());
        if (address == null) throw new RuntimeException("收货地址不存在");

        String orderSn = "ORD" + System.currentTimeMillis();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OmsOrderItem> itemList = new ArrayList<>();

        for (OmsOrderItemDto dto : orderDto.getOrderItemList()) {
            PmsSku sku = pmsSkuMapper.selectById(dto.getSkuId());
            if (sku == null) continue;

            PmsProduct product = pmsProductMapper.selectById(sku.getProductId());
            OmsOrderItem item = new OmsOrderItem();
            item.setProductId(sku.getProductId());
            item.setSkuId(sku.getId());
            item.setProductName(product.getProductName());
            item.setSkuSpecs(sku.getSkuCode());
            item.setPic(sku.getPic());
            item.setPrice(sku.getPrice());
            item.setQuantity(dto.getQuantity());
            item.setTotalPrice(sku.getPrice().multiply(new BigDecimal(dto.getQuantity())));

            totalAmount = totalAmount.add(item.getTotalPrice());
            itemList.add(item);
        }

        OmsOrder order = new OmsOrder();
        order.setUserId(userId);
        order.setOrderSn(orderSn);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddress(address.getProvince() + address.getCity() + address.getArea() + address.getDetail());

        orderMapper.insert(order);

        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for (OmsOrderItem item : itemList) {
            item.setOrderId(order.getId());
            item.setOrderSn(orderSn);
            orderItemMapper.insert(item);

            OrderItemVo vo = new OrderItemVo();
            vo.setSkuId(item.getSkuId());
            vo.setQuantity(item.getQuantity());
            orderItemVoList.add(vo);
        }

        pmsSkuService.preLockStock(orderSn, orderItemVoList);

        return order;
    }

    public List<OrderVo> getOrderVoList(Long userId) {
        List<OmsOrder> orderList = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getUserId, userId)
                        .orderByDesc(OmsOrder::getCreateTime));
        List<OrderVo> result = new ArrayList<>();

        for (OmsOrder order : orderList) {
            OrderVo vo = new OrderVo();
            BeanUtil.copyProperties(order, vo);
            vo.setStatusText(getStatusText(order.getStatus()));
            List<OmsOrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, order.getId()));
            List<OrderItemVo> itemVoList = new ArrayList<>();

            for (OmsOrderItem item : orderItems) {
                OrderItemVo itemVo = new OrderItemVo();
                BeanUtil.copyProperties(item, itemVo);
                itemVoList.add(itemVo);
            }
            vo.setOrderItems(itemVoList);

            result.add(vo);
        }
        return result;
    }

    public PageVo<OrderVo> getUserOrderListByCondition(Long pageNum, Long pageSize, Long userId, String orderSn, Integer status, String startTime, String endTime) {
        Page<OmsOrder> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getUserId, userId);
        if (orderSn != null && !orderSn.isEmpty()) {
            wrapper.like(OmsOrder::getOrderSn, orderSn);
        }
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(OmsOrder::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(OmsOrder::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        Page<OmsOrder> result = orderMapper.selectPage(mpPage, wrapper);

        List<OrderVo> voList = result.getRecords().stream().map(order -> {
            OrderVo vo = new OrderVo();
            BeanUtil.copyProperties(order, vo);
            vo.setStatusText(getStatusText(order.getStatus()));
            List<OmsOrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, order.getId()));
            List<OrderItemVo> itemVoList = orderItems.stream().map(item -> {
                OrderItemVo itemVo = new OrderItemVo();
                BeanUtil.copyProperties(item, itemVo);
                return itemVo;
            }).collect(Collectors.toList());
            vo.setOrderItems(itemVoList);
            return vo;
        }).collect(Collectors.toList());

        PageVo<OrderVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public OrderVo getOrderDetail(Long userId, Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");

        OrderVo vo = new OrderVo();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusText(getStatusText(order.getStatus()));

        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        List<OrderItemVo> itemVos = BeanUtil.copyToList(items, OrderItemVo.class);
        vo.setOrderItems(itemVos);
        return vo;
    }

    public void cancelOrder(Long userId, Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        if (order.getStatus() == 3) throw new RuntimeException("已完成订单不可取消");

        pmsSkuService.releaseStockLock(order.getOrderSn());

        orderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .set(OmsOrder::getStatus, 4)
                .set(OmsOrder::getPayTime, LocalDateTime.now()));
    }

    public void confirmOrder(Long userId, Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 2) throw new RuntimeException("不能确认收货");
        orderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .set(OmsOrder::getStatus, 3)
                .set(OmsOrder::getConfirmTime, LocalDateTime.now()));
    }

    public void deleteOrder(Long userId, Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 3 && order.getStatus() != 4) throw new RuntimeException("只能删除已完成/已取消订单");
        orderItemMapper.delete(new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        orderMapper.deleteById(orderId);
    }

    public PageVo<OrderVo> getAdminOrderList(Long pageNum, Long pageSize, String orderSn, Integer status, String startTime, String endTime) {
        System.out.println("收到的参数：orderSn=" + orderSn + ", status=" + status);
        Page<OmsOrder> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderSn != null && !orderSn.isEmpty()) {
            wrapper.like(OmsOrder::getOrderSn, orderSn);
        }
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(OmsOrder::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(OmsOrder::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        Page<OmsOrder> result = orderMapper.selectPage(mpPage, wrapper);

        List<OrderVo> voList = result.getRecords().stream().map(order -> {
            OrderVo vo = new OrderVo();
            BeanUtil.copyProperties(order, vo);
            vo.setStatusText(getStatusText(order.getStatus()));
            return vo;
        }).collect(Collectors.toList());

        PageVo<OrderVo> pageVo = new PageVo<>();
        pageVo.setList(voList);
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public List<OrderVo> getOrderNeverDelivery(){
        List<OmsOrder> list = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getStatus, 1));

        List<OrderVo> voList = new ArrayList<>();
        for (OmsOrder order : list) {
            OrderVo vo = new OrderVo();
            BeanUtil.copyProperties(order, vo);
            vo.setStatusText(getStatusText(order.getStatus()));
            voList.add(vo);
        }
        return voList;
    }

    public OrderVo getOrderDetail(Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        OrderVo vo = new OrderVo();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusText(getStatusText(order.getStatus()));

        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        List<OrderItemVo> itemVos = BeanUtil.copyToList(items, OrderItemVo.class);
        vo.setOrderItems(itemVos);

        return vo;
    }

    public void deliverOrder(Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 1) throw new RuntimeException("只有待发货订单可发货");

        orderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .set(OmsOrder::getStatus, 2)
                .set(OmsOrder::getDeliveryTime, LocalDateTime.now()));
    }

    public void updateOrderStatus(Long orderId, Integer status) {
        LambdaUpdateWrapper<OmsOrder> wrapper = new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .set(OmsOrder::getStatus, status);
        if (status == 1) {
            wrapper.set(OmsOrder::getPayTime, LocalDateTime.now());
        }
        orderMapper.update(null, wrapper);
    }

    public List<ProductSalesVo> getProductSalesTop5(String startTime, String endTime) {
        return orderMapper.getProductSalesTop5(startTime, endTime);
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    public Map<String, Object> getDashboardStats(String startTime, String endTime) {
        Map<String, Object> stats = new LinkedHashMap<>();

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        LambdaQueryWrapper<OmsOrder> todayWrapper = new LambdaQueryWrapper<OmsOrder>()
                .ge(OmsOrder::getCreateTime, todayStart)
                .le(OmsOrder::getCreateTime, todayEnd);

        BigDecimal todaySales = orderMapper.selectList(todayWrapper)
                .stream()
                .filter(order -> order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3)
                .map(OmsOrder::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("todaySales", todaySales);

        Long todayOrderCount = orderMapper.selectCount(todayWrapper);
        stats.put("todayOrderCount", todayOrderCount);

        Long todayPaidOrder = orderMapper.selectCount(
                todayWrapper.clone().eq(OmsOrder::getStatus, 1)
        );
        stats.put("todayPaidOrder", todayPaidOrder);

        // 优化：使用单条 SQL 查询近7天数据
        LocalDate weekStartDate = LocalDate.now().minusDays(6);
        LocalDateTime weekStart = LocalDateTime.of(weekStartDate, LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<DailyStatsVo> dailyStats = orderMapper.getDailyStats(weekStart, weekEnd);

        // 构建每日数据映射
        Map<String, DailyStatsVo> statsMap = dailyStats.stream()
                .collect(Collectors.toMap(DailyStatsVo::getDate, s -> s, (a, b) -> a));

        // 按日期顺序填充近7天数据
        List<Map<String, Object>> weekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            DailyStatsVo dayStats = statsMap.get(date.toString());

            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("date", date.toString());
            dayData.put("orderCount", dayStats != null ? dayStats.getOrderCount() : 0L);
            dayData.put("sales", dayStats != null ? dayStats.getSales() : BigDecimal.ZERO);
            weekTrend.add(dayData);
        }
        stats.put("weekTrend", weekTrend);

        Map<String, Object> statusDistribution = new LinkedHashMap<>();
        for (int status = 0; status <= 4; status++) {
            Long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getStatus, status)
            );
            statusDistribution.put(getStatusText(status), count);
        }
        stats.put("statusDistribution", statusDistribution);

        return stats;
    }
}
