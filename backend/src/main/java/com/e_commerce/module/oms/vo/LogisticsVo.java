package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流信息视图对象
 * 用于封装和传输物流相关的数据
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class LogisticsVo {
    private Long id;                    // 物流信息ID
    private Long orderId;               // 订单ID
    private String orderSn;            // 订单编号
    private String deliveryCompany;    // 物流公司名称
    private String deliveryNo;        // 物流单号
    private Integer status;            // 物流状态
    private String statusText;        // 物流状态文本描述
    private String receiver;          // 收件人姓名
    private String phone;              // 收件人联系电话
    private String address;            // 收件地址
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takeTime;    // 取件时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;  // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;  // 更新时间
    private List<LogisticsTraceVo> traces; // 物流轨迹信息列表
}
