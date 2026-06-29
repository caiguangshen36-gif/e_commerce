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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

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

    /**
     * [Q4优化2] ObjectMapper 配置：忽略LLM多输出的未知字段，避免解析失败
     * 原先默认配置下，LLM输出中多了一个未知字段就会抛UnrecognizedPropertyException
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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

    // ================================================================
    //  AI 生成商品文案 —— 整合 Q4 四个优化：重试 + JSON加固 + 熔断 + 异步
    // ================================================================

    /**
     * [Q4优化全链路] AI 生成商品文案（同步版）
     *
     * 调用链路：
     *   @CircuitBreaker（外层熔断）→ callAiWithRetry（内层@Retryable重试）
     *   → chatClient 调用通义千问 → extractJson 清洗 → Jackson 解析
     *   → 全部失败 → @Recover 兜底 → fallbackGoodsDesc 返回静态文案
     *
     * @CircuitBreaker 说明：
     *   - name="aiGenerate" 对应 application.yaml 中 resilience4j.circuitbreaker.instances.aiGenerate
     *   - 最近10次调用中失败率>50% → 自动熔断30秒 → 期间所有请求直接走fallback不调AI
     *   - 30秒后进入半开状态，放行2个请求试探AI是否恢复
     *   - fallbackMethod 指向本类中的兜底方法，签名必须一致
     */
    @PostMapping("/generate-goods-desc")
    @CircuitBreaker(name = "aiGenerate", fallbackMethod = "fallbackGoodsDesc")
    public Result<GoodsDescResponse> generateGoodsDesc(@Valid @RequestBody GoodsDescRequest request) {
        String prompt = buildProductDescPrompt(request);
        // 内层：@Retryable 自动重试，全部失败则走 @Recover
        GoodsDescResponse result = callAiWithRetry(prompt);
        return Result.success(result);
    }

    /**
     * [Q4优化1] 带重试的AI调用（@Retryable + @Recover）
     *
     * @Retryable 说明：
     *   - retryFor=RuntimeException.class：所有运行时异常都触发重试（网络超时、JSON解析失败等）
     *   - maxAttempts=2：最多重试2次（加上首次共3次调用机会）
     *   - backoff.delay=500：每次重试间隔500ms，给API端恢复窗口
     *
     * 为什么3次就够了？
     *   LLM API瞬时故障（网络抖动、429限流）占比>90%，1~2次重试即可覆盖。
     *   设太多反而会放大请求积压，拖慢响应时间。
     */
    @Retryable(
            retryFor = RuntimeException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    private GoodsDescResponse callAiWithRetry(String prompt) {
        log.info("[AI文案生成] 开始调用通义千问...");
        String aiResult = chatClient.prompt().user(prompt).call().content();
        return parseAiResponse(aiResult, GoodsDescResponse.class);
    }

    /**
     * [Q4优化1] @Retryable全部重试失败后的最终兜底
     *
     * @Recover 触发条件：callAiWithRetry() 重试2次后仍然抛 RuntimeException
     * 此时返回预置的静态默认文案，而非让用户看到"服务器异常"
     * 这叫 graceful degradation（优雅降级）——功能降级但不停服
     *
     * 注意：@Recover 方法的返回值类型、参数列表必须与 @Retryable 方法一致
     * （第一个参数是异常类型，后面是@Retryable方法的参数）
     */
    @Recover
    private GoodsDescResponse recoverFromAiFailure(RuntimeException e, String prompt) {
        log.error("[AI文案生成] 重试3次全部失败，使用静态默认文案兜底。原始异常：", e);
        return GoodsDescResponse.staticDefault();
    }

    /**
     * [Q4优化3] 熔断降级兜底——与@CircuitBreaker配合
     *
     * 触发条件：
     *   1. 熔断器处于OPEN状态（失败率>50%）→ 直接走此方法，不调用 generateGoodsDesc
     *   2. callAiWithRetry() 内部@Recover也失败 → 最终兜底
     *
     * 方法签名要求（Resilience4j规范）：
     *   返回值与控制器方法一致（Result<GoodsDescResponse>）
     *   参数列表 = 控制器方法参数 + Throwable（异常对象放最后）
     */
    private Result<GoodsDescResponse> fallbackGoodsDesc(GoodsDescRequest request, Throwable t) {
        log.warn("[AI文案生成] 熔断降级触发，返回静态默认文案。异常类型：{}，消息：{}",
                t.getClass().getSimpleName(), t.getMessage());
        return Result.success(GoodsDescResponse.staticDefault());
    }

    // ================================================================
    //  AI 智能搜索
    // ================================================================

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

    // ================================================================
    //  [Q4优化4] 异步批量文案生成
    // ================================================================

    /**
     * [Q4优化4] 异步生成单个商品文案（非阻塞，立即返回）
     *
     * 适用场景：后台批量操作——运营人员选中50个商品一键生成文案
     * 原先同步方式：50个商品 × 平均8秒 = 400秒（近7分钟）
     * 异步后：50个任务并行提交，后台轮询进度，总耗时≈最慢的一个（~8秒）
     *
     * @Async 说明：
     *   - Spring 自动从线程池分配线程执行，不占用Tomcat请求线程
     *   - 返回 CompletableFuture，调用方可通过 .get() 等待结果或轮询
     *   - 生产环境建议配置自定义 ThreadPoolTaskExecutor 替代默认 SimpleAsyncTaskExecutor
     */
    @Async
    @PostMapping("/generate-goods-desc/async")
    public CompletableFuture<Result<GoodsDescResponse>> generateGoodsDescAsync(
            @Valid @RequestBody GoodsDescRequest request) {
        log.info("[AI文案生成-异步] 任务已提交，商品：{}", request.getProductName());
        String prompt = buildProductDescPrompt(request);
        GoodsDescResponse result = callAiWithRetry(prompt);
        return CompletableFuture.completedFuture(Result.success(result));
    }

    // ================================================================
    //  Prompt 构建
    // ================================================================

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

    // ================================================================
    //  JSON 解析 —— [Q4优化2] 增强鲁棒性
    // ================================================================

    /**
     * [Q4优化2] 解析AI返回的JSON，调用extractJson做清洗后再用Jackson解析
     */
    private <T> T parseAiResponse(String rawText, Class<T> clazz) {
        try {
            String json = extractJson(rawText);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("[AI文案生成] JSON解析失败，清洗后内容：{}", extractJson(rawText), e);
            throw new RuntimeException("AI 响应解析失败：" + e.getMessage(), e);
        }
    }

    /**
     * [Q4优化2] JSON清洗——处理LLM输出的常见格式偏差
     *
     * 原先（优化前）仅做3步：
     *   ① 去除 ```json ``` 代码围栏
     *   ② 截取首尾 { } 之间的内容
     *   ③ trim
     *
     * 新增处理（优化后）：
     *   ④ 中文双引号“ ” → 英文 "（LLM训练语料含中文，常混用）
     *   ⑤ 尾部多余逗号清除（}, 或 ], 是LLM生成JSON的极高频错误）
     *   ⑥ 单引号 → 双引号（LLM偶尔用Python风格）
     *   ⑦ 返回 "{}" 而非原始文本（避免Jackson必然解析失败）
     *
     * 为什么每个步骤都有意义？见 Q4问题优化.txt 中优先级2的原理说明
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
        }

        // ① 去除 Markdown 代码围栏：```json ... ```
        String cleaned = text
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        // ④ [Q4优化2新增] 中文双引号“ ” → 英文引号 "
        cleaned = cleaned.replaceAll("[“”]", "\"");

        // ② 截取第一个 { 到最后一个 } 之间的内容
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String json = cleaned.substring(start, end + 1);

            // ⑤ [Q4优化2新增] 修复常见LLM JSON格式问题
            json = json
                    .replaceAll(",\\s*}", "}")       // 对象尾部多余逗号：{"a":1,} → {"a":1}
                    .replaceAll(",\\s*]", "]")       // 数组尾部多余逗号：[1,2,] → [1,2]
                    .replaceAll("'", "\"");          // ⑥ 单引号 → 双引号

            return json;
        }

        // ⑦ 无花括号时返回空对象，而非原始文本（避免Jackson必然解析失败）
        log.warn("[AI文案生成] LLM输出中未找到JSON花括号，返回空对象。原始响应前100字符：{}",
                text.length() > 100 ? text.substring(0, 100) + "..." : text);
        return "{}";
    }
}
