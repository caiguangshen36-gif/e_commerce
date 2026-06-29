package com.e_commerce.module.ai.vo;

import lombok.Data;

import java.util.Arrays;
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

    /**
     * [Q4优化1/3] 静态默认文案工厂方法
     *
     * 目的：当AI调用全部失败（重试耗尽 + 熔断触发）时，返回此预置文案
     *      确保下游（前端/其他服务）始终能拿到合法的 GoodsDescResponse，不会因为AI故障而报错
     *
     * 这是"优雅降级（graceful degradation）"的关键一环：
     *   - 完美情况：AI生成高质量个性化文案
     *   - 降级情况：返回这段通用默认文案，用户看到的内容不如AI生成的好，但不会看到报错
     */
    public static GoodsDescResponse staticDefault() {
        GoodsDescResponse fallback = new GoodsDescResponse();
        fallback.setTitle("品质好物推荐");
        fallback.setSubtitle("精选优质商品");
        fallback.setDetailHtml(
            "<div class='product-detail'>" +
                "<h3>产品亮点</h3>" +
                "<ul>" +
                    "<li>精选优质原材料，品质有保障</li>" +
                    "<li>严格品控流程，值得信赖</li>" +
                    "<li>贴心售后服务，购物无忧</li>" +
                "</ul>" +
                "<p>详情请查看商品规格参数或咨询在线客服。</p>" +
            "</div>"
        );
        fallback.setShortDesc("精选优质商品，品质有保障，详情请查看商品页或咨询在线客服");
        fallback.setMarketingCopy("品质好物，限时热卖中！精选优质商品，值得拥有~");
        fallback.setKeywords(Arrays.asList("品质好物", "精选", "推荐", "热卖"));
        fallback.setTags(Arrays.asList("推荐", "热卖"));
        return fallback;
    }
}