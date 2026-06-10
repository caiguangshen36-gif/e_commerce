package com.e_commerce.module.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品文案生成响应VO
 */
@Data
public class GoodsDescResponse {
    private String title; // 商品标题
    private String subtitle; // 副标题
    private String detailHtml; // 详情页HTML
    private String shortDesc; // 简短描述（用于列表展示）
    private String marketingCopy; // 营销话术
    private List<String> keywords; // SEO关键词
    private List<String> tags; // 商品标签
}