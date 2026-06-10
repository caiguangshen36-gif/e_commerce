package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单物流信息表
 * 该实体类用于存储订单的物流相关信息，包括物流公司、物流单号、物流状态等
 */
@Data  // Lombok注解，自动生成getter、setter等方法
@TableName("oms_logistics")  // MyBatis-Plus注解，指定对应的数据库表名
public class OmsLogistics {
    @TableId(type = IdType.AUTO)
    private Long id;               // 物流信息ID，主键
    private Long orderId;           // 订单ID
    private String orderSn;         // 订单编号
    private Long userId;            // 用户ID
    private String deliveryCompany; // 物流公司
    private String deliveryNo;     // 物流单号
    private Integer status;         // 物流状态：0-待发货 1-已发货 2-运输中 3-已签收 4-异常
    private String receiver;        // 收货人
    private String phone;           // 收货电话
    private String address;         // 收货地址
    private LocalDateTime takeTime; // 签收时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
