package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹详情表
 * 该类用于存储物流轨迹的详细信息，包括物流ID、物流描述和创建时间等字段
 */
@Data
@TableName("oms_logistics_trace")
public class OmsLogisticsTrace {
    // 主键ID，使用自增策略
    @TableId(type = IdType.AUTO)
    private Long id;
    // 关联的物流ID，用于标识物流轨迹所属的物流订单
    private Long logisticsId;       // 物流ID
    // 物流轨迹的具体描述信息，如"已发货"、"运输中"等状态
    private String content;         // 物流描述
    // 物流轨迹记录的创建时间，精确到纳秒
    private LocalDateTime createTime;
}
