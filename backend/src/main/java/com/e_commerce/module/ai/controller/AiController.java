package com.e_commerce.module.ai.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.module.ai.dto.AiChatRequest;
import com.e_commerce.module.ai.dto.GoodsDescRequest;
import com.e_commerce.module.ai.dto.ShopGuideRequest;
import com.e_commerce.module.ai.dto.SmartSearchRequest;
import com.e_commerce.module.ai.service.KnowledgeBaseRagService;
import com.e_commerce.module.ai.service.SmartCustomerService;
import com.e_commerce.module.ai.service.SmartShopGuideService;
import com.e_commerce.module.ai.vo.GoodsDescResponse;
import com.e_commerce.module.ai.vo.ShopGuideResponse;
import com.e_commerce.module.ai.vo.SmartSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private SmartCustomerService smartCustomerService;

    @Autowired
    private SmartShopGuideService smartShopGuideService;

    @Autowired
    private KnowledgeBaseRagService ragService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI 通用对话
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AiChatRequest request) {
        String prompt = request.getMessage();
        String response = chatClient.prompt().user(prompt).call().content();
        return Result.success(response);
    }

    /**
     * AI 智能客服（支持订单查询、物流查询、售后查询等）
     */
    @PostMapping("/customer-service")
    public Result<String> customerService(@RequestBody AiChatRequest request) {
        String response = smartCustomerService.handleCustomerMessage(request.getMessage());
        return Result.success(response);
    }

    /**
     * AI 生成商品文案
     */
    @PostMapping("/generate-goods-desc")
    public Result<GoodsDescResponse> generateGoodsDesc(@Valid @RequestBody GoodsDescRequest request) {
        String prompt = buildProductDescPrompt(request);
        String aiResult = chatClient.prompt().user(prompt).call().content();
        GoodsDescResponse result = parseAiResponse(aiResult, GoodsDescResponse.class);
        return Result.success(result);
    }

    /**
     * AI 智能搜索（同义词扩展 + 纠错）
     */
    @PostMapping("/intelligent-search")
    public Result<SmartSearchResponse> intelligentSearch(@RequestBody SmartSearchRequest request) {
        if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        String prompt = buildSmartSearchPrompt(request.getKeyword());
        String aiResult = chatClient.prompt().user(prompt).call().content();
        SmartSearchResponse result = parseAiResponse(aiResult, SmartSearchResponse.class);
        return Result.success(result);
    }

    /**
     * AI 智能导购（根据用户需求推荐商品）
     */
    @PostMapping("/shop-guide")
    public Result<ShopGuideResponse> shopGuide(@RequestBody ShopGuideRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.error("购物需求不能为空");
        }
        ShopGuideResponse response = smartShopGuideService.guide(request);
        return Result.success(response);
    }

    /**
     * RAG 知识库智能检索——基于向量相似度匹配知识库并生成回答
     */
    @PostMapping("/knowledge-search")
    public Result<String> knowledgeSearch(@RequestBody AiChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.error("问题不能为空");
        }
        String enriched = ragService.enrichPrompt(request.getMessage());
        if (enriched == null) {
            return Result.success("抱歉，知识库中暂无相关信息，您可以联系人工客服获取帮助。");
        }
        String response = chatClient.prompt().user(enriched).call().content();
        return Result.success(response);
    }

    /**
     * 刷新知识库向量索引（管理后台用）
     */
    @PostMapping("/knowledge-refresh")
    public Result<String> refreshKnowledge() {
        ragService.refreshVectorStore();
        return Result.success("知识库向量索引已刷新");
    }

    // ========== Prompt 构建 ==========

    private String buildProductDescPrompt(GoodsDescRequest request) {
        StringBuilder baseInfo = new StringBuilder();
        baseInfo.append("商品名称：").append(request.getProductName()).append("\n");
        baseInfo.append("商品分类：").append(request.getCategoryName()).append("\n");
        baseInfo.append("售价：").append(request.getPrice()).append("元");
        if (request.getOriginalPrice() != null) {
            baseInfo.append("，原价：").append(request.getOriginalPrice()).append("元");
        }
        baseInfo.append("\n");
        if (request.getSellingPoints() != null && !request.getSellingPoints().isEmpty()) {
            baseInfo.append("核心卖点：").append(request.getSellingPoints()).append("\n");
        }
        if (request.getSpec() != null && !request.getSpec().isEmpty()) {
            baseInfo.append("规格参数：").append(request.getSpec()).append("\n");
        }
        if (request.getTargetUser() != null && !request.getTargetUser().isEmpty()) {
            baseInfo.append("适用人群/场景：").append(request.getTargetUser()).append("\n");
        }
        if (request.getBrand() != null && !request.getBrand().isEmpty()) {
            baseInfo.append("品牌：").append(request.getBrand()).append("\n");
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            baseInfo.append("商品标签：").append(request.getTags()).append("\n");
        }

        String descType = request.getDescType() == null ? "ALL" : request.getDescType();
        String style = request.getStyle() == null ? "专业" : request.getStyle();
        String platform = request.getPlatform() == null ? "通用电商平台" : request.getPlatform();
        String extraRequirement = request.getExtraRequirement() == null ? "" : request.getExtraRequirement();

        return String.format("""
            你是一个专业的电商文案专家，精通商品标题、详情页、营销话术的创作。
            请根据以下商品信息，生成符合要求的文案：

            【商品基础信息】
            %s

            【生成要求】
            1. 文案类型：%s（TITLE=商品标题，DETAIL=详情页长文案，MARKETING=营销短文案，SEO=搜索关键词，ALL=综合生成所有类型）
            2. 文案风格：%s
            3. 目标平台：%s
            4. 额外要求：%s
            5. 要求：标题吸引人、详情页有说服力、营销话术有感染力，符合平台规则，突出商品优势和性价比

            【输出格式】请严格按以下 JSON 格式输出，不要包含其他内容：
            {
                "title": "商品标题",
                "subtitle": "副标题",
                "detailHtml": "详情页 HTML 内容",
                "shortDesc": "简短描述",
                "marketingCopy": "营销话术",
                "keywords": ["关键词 1", "关键词 2", "关键词 3"],
                "tags": ["标签 1", "标签 2", "标签 3"]
            }
            """, baseInfo, descType, style, platform, extraRequirement);
    }

    private String buildSmartSearchPrompt(String keyword) {
        return String.format("""
            你是一个电商平台的智能搜索助手，负责对用户输入的关键词进行处理。
            请对以下关键词进行：
            1. 拼写纠错（如果有）
            2. 同义词扩展（至少 5 个）
            3. 相关词推荐（至少 3 个）

            关键词：%s

            【输出格式】严格 JSON，不要包含其他内容：
            {
                "original": "原始关键词",
                "corrected": "纠错后的关键词",
                "synonyms": ["同义词 1", "同义词 2"],
                "related": ["相关词 1", "相关词 2"]
            }
            """, keyword);
    }

    // ========== JSON 解析 ==========

    private <T> T parseAiResponse(String rawText, Class<T> clazz) {
        try {
            String json = extractJson(rawText);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("AI 响应 JSON 解析失败，原始响应：{}", rawText, e);
            throw new RuntimeException("AI 响应解析失败：" + e.getMessage());
        }
    }

    private String extractJson(String text) {
        if (text == null || text.isEmpty()) return "{}";
        String cleaned = text
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }
}
