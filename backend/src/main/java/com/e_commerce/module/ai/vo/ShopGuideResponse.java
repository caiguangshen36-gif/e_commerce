package com.e_commerce.module.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * AI导购响应VO
 */
@Data
public class ShopGuideResponse {
    private String replyMessage;
    private List<ShopGuideProductVo> products;
    private Integer totalCount;
    private String searchSummary;
    private String intentAnalysis;
}  