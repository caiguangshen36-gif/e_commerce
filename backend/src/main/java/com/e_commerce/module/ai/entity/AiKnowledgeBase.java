package com.e_commerce.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI知识库实体类
 */
@Data
@TableName("ai_knowledge_base")
public class AiKnowledgeBase {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 规则标题
     */
    private String title;
    
    /**
     * 规则内容
     */
    private String content;
    
    /**
     * 规则类型：goods-商品规则，after_sale-售后规则，service-服务规则，promotion-促销规则，order-订单规则，logistics-物流规则，payment-支付规则
     */
    private String type;
    
    /**
     * 关键词（逗号分隔）
     */
    private String keywords;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
