package com.e_commerce.module.ai.dto;

import lombok.Data;

/**
 * AI 智能搜索请求
 */
@Data
public class SmartSearchRequest {
    /**
     * 搜索关键词
     */
    private String keyword;
}
