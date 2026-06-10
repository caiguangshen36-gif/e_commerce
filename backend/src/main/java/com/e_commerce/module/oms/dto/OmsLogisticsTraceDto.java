package com.e_commerce.module.oms.dto;

import lombok.Data;

/**
 * 物流轨迹数据传输对象(DTO)
 * 用于封装物流轨迹相关的数据，在系统各层之间传递信息
 */
@Data
public class OmsLogisticsTraceDto {
    private Long logisticsId;  // 物流ID，用于标识具体的物流信息
    private String content;   // 物流轨迹内容，描述物流的具体状态和位置信息
}
