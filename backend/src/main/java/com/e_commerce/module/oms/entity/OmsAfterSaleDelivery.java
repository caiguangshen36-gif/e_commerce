package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退货物流信息表
 * 该类用于存储退货物流相关信息，包括售后ID、物流公司、物流单号等
 */
@Data
@TableName("oms_after_sale_delivery")
public class OmsAfterSaleDelivery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long afterSaleId;          // 售后ID
    private String deliveryCompany;    // 退货物流公司
    private String deliveryNo;         // 退货物流单号
    private LocalDateTime sendTime;    // 用户退货发货时间
    private LocalDateTime receiveTime;// 商家收货时间
    private Integer status;            // 状态：0-未发货 1-已发货 2-已签收
    private LocalDateTime createTime;
}