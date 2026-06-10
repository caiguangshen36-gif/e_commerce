package com.e_commerce.module.oms.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.oms.dto.OmsLogisticsDto;
import com.e_commerce.module.oms.dto.OmsLogisticsTraceDto;
import com.e_commerce.module.oms.entity.OmsLogistics;
import com.e_commerce.module.oms.entity.OmsLogisticsTrace;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.mapper.OmsLogisticsMapper;
import com.e_commerce.module.oms.mapper.OmsLogisticsTraceMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import com.e_commerce.module.oms.vo.LogisticsTraceVo;
import com.e_commerce.module.oms.vo.LogisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OmsLogisticsService {

    @Autowired
    private OmsLogisticsMapper logisticsMapper;

    @Autowired
    private OmsLogisticsTraceMapper traceMapper;

    @Autowired
    private OmsOrderMapper orderMapper;

    @Transactional
    public void createLogistics(OmsLogisticsDto dto) {
        OmsOrder order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new RuntimeException("订单不存在");

        String deliveryNo = "OL" + System.currentTimeMillis();
        OmsLogistics logistics = new OmsLogistics();
        logistics.setOrderId(order.getId());
        logistics.setOrderSn(order.getOrderSn());
        logistics.setUserId(order.getUserId());
        logistics.setReceiver(order.getReceiver());
        logistics.setPhone(order.getPhone());
        logistics.setAddress(order.getAddress());
        logistics.setDeliveryCompany(dto.getDeliveryCompany());
        logistics.setDeliveryNo(deliveryNo);
        logistics.setStatus(0);

        logisticsMapper.insert(logistics);
    }

    public List<LogisticsVo> getUserLogisticsList(Long userId) {
        List<OmsLogistics> list = logisticsMapper.selectList(
                new LambdaQueryWrapper<OmsLogistics>()
                        .eq(OmsLogistics::getUserId, userId)
                        .orderByDesc(OmsLogistics::getCreateTime));
        return list.stream().map(this::convert).toList();
    }

    public List<LogisticsVo> getUserLogisticsListByCondition(Long userId, Integer status) {
        LambdaQueryWrapper<OmsLogistics> wrapper = new LambdaQueryWrapper<OmsLogistics>()
                .eq(OmsLogistics::getUserId, userId);
        if (status != null) {
            wrapper.eq(OmsLogistics::getStatus, status);
        }
        wrapper.orderByDesc(OmsLogistics::getCreateTime);
        List<OmsLogistics> list = logisticsMapper.selectList(wrapper);
        return list.stream().map(this::convert).toList();
    }

    @Transactional
    public void sign(Long id) {
        logisticsMapper.update(null,
                new LambdaUpdateWrapper<OmsLogistics>()
                        .eq(OmsLogistics::getId, id)
                        .set(OmsLogistics::getStatus, 3)
                        .set(OmsLogistics::getTakeTime, LocalDateTime.now()));
    }

    public LogisticsVo getLogisticsDetail(Long userId, Long orderId) {
        OmsLogistics logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getOrderId, orderId));

        if (logistics == null || !logistics.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }

        LogisticsVo vo = convert(logistics);
        List<OmsLogisticsTrace> traces = traceMapper.selectList(
                new LambdaQueryWrapper<OmsLogisticsTrace>()
                        .eq(OmsLogisticsTrace::getLogisticsId, logistics.getId())
                        .orderByDesc(OmsLogisticsTrace::getCreateTime));
        vo.setTraces(BeanUtil.copyToList(traces, LogisticsTraceVo.class));
        return vo;
    }

    @Transactional
    public void addAutoTrace(Long orderId) {
        if (orderId == null) {
            throw new RuntimeException("订单ID不能为空");
        }

        OmsLogistics omsLogistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getOrderId, orderId));
        if (omsLogistics == null) {
            throw new RuntimeException("物流信息不存在");
        }

        OmsLogisticsTrace trace = new OmsLogisticsTrace();
        trace.setLogisticsId(omsLogistics.getId());
        trace.setContent("订单已付款，等待商家发货");
        traceMapper.insert(trace);
    }

    @Transactional
    public void updateDeliveryInfo(Long orderId, String deliveryCompany) {
        if (orderId == null || deliveryCompany == null) {
            throw new RuntimeException("订单ID和快递公司不能为空");
        }

        OmsLogistics logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getOrderId, orderId));
        if (logistics == null) {
            throw new RuntimeException("物流信息不存在");
        }

        logisticsMapper.update(null,
                new LambdaUpdateWrapper<OmsLogistics>()
                        .eq(OmsLogistics::getOrderId, orderId)
                        .set(OmsLogistics::getDeliveryCompany, deliveryCompany)
                        .set(OmsLogistics::getStatus, 1)
                        .set(OmsLogistics::getUpdateTime, LocalDateTime.now()));

        OmsLogisticsTrace omsLogisticsTrace = new OmsLogisticsTrace();
        omsLogisticsTrace.setLogisticsId(logistics.getId());
        omsLogisticsTrace.setContent("商家已发货，快递公司：" + deliveryCompany);
        traceMapper.insert(omsLogisticsTrace);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        logisticsMapper.update(null,
                new LambdaUpdateWrapper<OmsLogistics>()
                        .eq(OmsLogistics::getId, id)
                        .set(OmsLogistics::getStatus, status)
                        .set(OmsLogistics::getUpdateTime, LocalDateTime.now()));
    }

    @Transactional
    public void addTrace(OmsLogisticsTraceDto dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getLogisticsId() == null) {
            throw new RuntimeException("物流ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new RuntimeException("地址/轨迹内容不能为空");
        }

        OmsLogistics logistics = logisticsMapper.selectById(dto.getLogisticsId());
        if (logistics == null) {
            throw new RuntimeException("物流信息不存在");
        }

        OmsLogisticsTrace trace = new OmsLogisticsTrace();
        trace.setLogisticsId(dto.getLogisticsId());
        trace.setContent("货品已到达：" + dto.getContent());
        traceMapper.insert(trace);
    }

    public List<LogisticsTraceVo> getTraceListByOrderId(Long userId, Long orderId) {
        OmsLogistics logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getOrderId, orderId));
        if (logistics == null || !logistics.getUserId().equals(userId)) {
            throw new RuntimeException("无权限或物流不存在");
        }
        List<OmsLogisticsTrace> traces = traceMapper.selectList(
                new LambdaQueryWrapper<OmsLogisticsTrace>()
                        .eq(OmsLogisticsTrace::getLogisticsId, logistics.getId())
                        .orderByDesc(OmsLogisticsTrace::getCreateTime));
        return BeanUtil.copyToList(traces, LogisticsTraceVo.class);
    }

    public List<LogisticsTraceVo> getTraceListByLogisticsId(Long logisticsId) {
        List<OmsLogisticsTrace> traces = traceMapper.selectList(
                new LambdaQueryWrapper<OmsLogisticsTrace>()
                        .eq(OmsLogisticsTrace::getLogisticsId, logisticsId)
                        .orderByDesc(OmsLogisticsTrace::getCreateTime));
        return BeanUtil.copyToList(traces, LogisticsTraceVo.class);
    }

    @Transactional
    public void deleteTrace(Long id) {
        traceMapper.deleteById(id);
    }

    public List<LogisticsVo> getAllList() {
        return logisticsMapper.selectList(
                new LambdaQueryWrapper<OmsLogistics>().orderByDesc(OmsLogistics::getCreateTime))
                .stream().map(this::convert).toList();
    }

    public List<LogisticsVo> getListByCondition(String orderSn, String deliveryNo, String deliveryCompany, Integer status) {
        LambdaQueryWrapper<OmsLogistics> wrapper = new LambdaQueryWrapper<>();
        if (orderSn != null && !orderSn.isEmpty()) {
            wrapper.like(OmsLogistics::getOrderSn, orderSn);
        }
        if (deliveryNo != null && !deliveryNo.isEmpty()) {
            wrapper.like(OmsLogistics::getDeliveryNo, deliveryNo);
        }
        if (deliveryCompany != null && !deliveryCompany.isEmpty()) {
            wrapper.like(OmsLogistics::getDeliveryCompany, deliveryCompany);
        }
        if (status != null) {
            wrapper.eq(OmsLogistics::getStatus, status);
        }
        wrapper.orderByDesc(OmsLogistics::getCreateTime);
        return logisticsMapper.selectList(wrapper).stream().map(this::convert).toList();
    }

    private LogisticsVo convert(OmsLogistics l) {
        LogisticsVo vo = new LogisticsVo();
        BeanUtil.copyProperties(l, vo);
        vo.setStatusText(switch (l.getStatus()) {
            case 0 -> "待发货";
            case 1 -> "已发货";
            case 2 -> "运输中";
            case 3 -> "已签收";
            case 4 -> "异常";
            default -> "未知";
        });
        return vo;
    }
}
