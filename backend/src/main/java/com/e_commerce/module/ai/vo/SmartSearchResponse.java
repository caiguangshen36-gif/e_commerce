package com.e_commerce.module.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * AI 智能搜索响应
 */
@Data
public class SmartSearchResponse {
    /**
     * 原始关键词
     */
    private String original;
    
    /**
     * 纠错后的关键词
     */
    private String corrected;
    
    /**
     * 同义词列表
     */
    private List<String> synonyms;
    
    /**
     * 相关词列表
     */
    private List<String> related;
}
