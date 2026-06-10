package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后服务值对象(VO)
 * 用于封装和传递售后相关的数据信息
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class AfterSaleVo {
    private Long id;                    // 售后服务ID
    private Long orderId;               // 订单ID
    private Long orderItemId;           // 订单项ID
    private Long userId;                // 用户ID
    private Long productId;             // 产品ID
    private String afterSaleSn;         // 售后服务编号
    private Integer type;               // 售后类型
    private String typeText;            // 售后类型文本描述
    private String reason;              // 售后原因
    private String description;         // 售后问题描述
    private BigDecimal refundAmount;    // 退款金额
    private Integer status;             // 售后状态
    private String statusText;          // 售后状态文本描述
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;     // 审核时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;    // 退款时间
    private String rejectReason;        // 拒绝原因
    private AfterSaleDeliveryVo delivery; // 售后物流信息
}
