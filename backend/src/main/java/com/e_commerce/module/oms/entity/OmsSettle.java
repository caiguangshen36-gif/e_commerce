package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算单（临时订单）实体类
 */
@Data
@TableName("oms_settle")
public class OmsSettle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private Long addressId;
    private Integer status;
    private LocalDateTime createTime;
}