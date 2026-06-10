package com.e_commerce.module.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后申请表
 * 该实体类对应数据库中的oms_after_sale表，用于存储售后申请相关信息
 */
@Data
@TableName("oms_after_sale")
public class OmsAfterSale {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键ID，自增
    private Long orderId;           // 订单ID，关联订单表主键
    private Long orderItemId;       // 订单商品项ID，关联订单商品项表主键
    private Long userId;            // 用户ID，关联用户表主键
    private Long productId;         // 商品ID，关联商品表主键
    private String afterSaleSn;     // 售后单号，唯一标识一个售后申请
    private Integer type;           // 售后类型：1-仅退款 2-退货退款
    private String reason;          // 退款原因，用户选择的退款原因
    private String description;     // 问题描述，用户对售后问题的详细描述
    private BigDecimal refundAmount;// 退款金额，需要退款的金额
    private Integer status;         // 状态：0-待审核 1-审核通过 2-已退款 3-驳回 4-用户已退货 5-商家收货完成
    private LocalDateTime createTime; // 创建时间，售后申请提交时间
    private LocalDateTime auditTime;  // 审核时间，售后申请审核完成时间
    private LocalDateTime refundTime;// 退款时间，退款完成时间
    private String rejectReason;    // 驳回原因，审核驳回时的具体原因
}
