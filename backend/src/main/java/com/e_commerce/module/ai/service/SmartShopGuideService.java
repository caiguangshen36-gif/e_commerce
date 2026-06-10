package com.e_commerce.module.ai.service;

import com.e_commerce.module.ai.dto.ShopGuideRequest;
import com.e_commerce.module.ai.vo.ShopGuideProductVo;
import com.e_commerce.module.ai.vo.ShopGuideResponse;
import com.e_commerce.module.product.mapper.PmsProductMapper;
import com.e_commerce.module.product.vo.PmsProductVo;
import com.e_commerce.module.product.vo.PmsSkuVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class SmartShopGuideService {

    @Autowired
    private PmsProductMapper productMapper;

    @Autowired
    private ChatClient chatClient;

    private static final Map<String, PriceRange> PRICE_KEYWORDS = new HashMap<>();
    static {
        PRICE_KEYWORDS.put("便宜", new PriceRange(0, 100));
        PRICE_KEYWORDS.put("实惠", new PriceRange(0, 150));
        PRICE_KEYWORDS.put("性价比", new PriceRange(50, 200));
        PRICE_KEYWORDS.put("平价", new PriceRange(0, 200));
        PRICE_KEYWORDS.put("中端", new PriceRange(200, 500));
        PRICE_KEYWORDS.put("高端", new PriceRange(500, 2000));
        PRICE_KEYWORDS.put("奢华", new PriceRange(2000, Integer.MAX_VALUE));
    }

    private static final Map<String, String> CATEGORY_KEYWORDS = new HashMap<>();
    static {
        CATEGORY_KEYWORDS.put("手机", "数码");
        CATEGORY_KEYWORDS.put("电脑", "数码");
        CATEGORY_KEYWORDS.put("笔记本", "数码");
        CATEGORY_KEYWORDS.put("平板", "数码");
        CATEGORY_KEYWORDS.put("耳机", "数码");
        CATEGORY_KEYWORDS.put("音响", "数码");
        CATEGORY_KEYWORDS.put("电视", "数码");
        CATEGORY_KEYWORDS.put("衣服", "服装");
        CATEGORY_KEYWORDS.put("裤子", "服装");
        CATEGORY_KEYWORDS.put("鞋子", "服装");
        CATEGORY_KEYWORDS.put("裙子", "服装");
        CATEGORY_KEYWORDS.put("外套", "服装");
        CATEGORY_KEYWORDS.put("衬衫", "服装");
        CATEGORY_KEYWORDS.put("T恤", "服装");
        CATEGORY_KEYWORDS.put("零食", "食品");
        CATEGORY_KEYWORDS.put("饮料", "食品");
        CATEGORY_KEYWORDS.put("水果", "食品");
        CATEGORY_KEYWORDS.put("生鲜", "食品");
        CATEGORY_KEYWORDS.put("家居", "家居");
        CATEGORY_KEYWORDS.put("家具", "家居");
        CATEGORY_KEYWORDS.put("厨具", "家居");
        CATEGORY_KEYWORDS.put("化妆品", "美妆");
        CATEGORY_KEYWORDS.put("护肤品", "美妆");
        CATEGORY_KEYWORDS.put("口红", "美妆");
        CATEGORY_KEYWORDS.put("香水", "美妆");
        CATEGORY_KEYWORDS.put("运动", "运动");
        CATEGORY_KEYWORDS.put("健身", "运动");
        CATEGORY_KEYWORDS.put("户外", "运动");
    }

    public ShopGuideResponse guide(ShopGuideRequest request) {
        String userMessage = request.getMessage();
        log.info("智能导购请求：{}", userMessage);

        ShopGuideResponse response = new ShopGuideResponse();

        // 1. 解析用户意图
        UserIntent intent = parseUserIntent(userMessage);

        // 2. 从数据库筛选商品
        List<ShopGuideProductVo> products = filterProductsFromDb(intent);

        if (products != null && !products.isEmpty()) {
            response.setProducts(products);
            response.setTotalCount(products.size());
            response.setSearchSummary(generateSearchSummary(intent, products.size()));
            response.setReplyMessage(generateGuideReply(userMessage, products, intent));
            return response;
        }

        // 3. 无匹配商品，使用 AI 生成友好回复
        response.setProducts(new ArrayList<>());
        response.setTotalCount(0);
        response.setSearchSummary("未找到匹配商品");
        response.setReplyMessage(generateNoMatchReply(userMessage));

        return response;
    }

    private UserIntent parseUserIntent(String message) {
        UserIntent intent = new UserIntent();
        intent.setKeywords(new ArrayList<>());
        intent.setPreferences(new ArrayList<>());

        String lowerMsg = message.toLowerCase();

        for (Map.Entry<String, PriceRange> entry : PRICE_KEYWORDS.entrySet()) {
            if (lowerMsg.contains(entry.getKey())) {
                intent.setPriceRange(entry.getValue());
                break;
            }
        }

        extractPriceFromMessage(message, intent);

        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (lowerMsg.contains(entry.getKey())) {
                intent.setCategory(entry.getValue());
                intent.getKeywords().add(entry.getKey());
            }
        }

        extractKeywordsFromMessage(message, intent);
        extractPreferences(lowerMsg, intent);

        if (lowerMsg.contains("便宜") || lowerMsg.contains("价格低") || lowerMsg.contains("最低价")) {
            intent.setSortBy("price_asc");
        } else if (lowerMsg.contains("贵") || lowerMsg.contains("高端")) {
            intent.setSortBy("price_desc");
        } else if (lowerMsg.contains("热门") || lowerMsg.contains("销量")) {
            intent.setSortBy("hot");
        } else if (lowerMsg.contains("新款") || lowerMsg.contains("新品")) {
            intent.setSortBy("newest");
        } else {
            intent.setSortBy("relevance");
        }

        return intent;
    }

    private void extractPriceFromMessage(String message, UserIntent intent) {
        if (message.matches(".*\\d+\\s*[元块]\\s*以[内外下].*")) {
            try {
                int max = Integer.parseInt(message.replaceAll(".*?(\\d+)\\s*[元块]\\s*以[内外下].*", "$1"));
                if (intent.getPriceRange() == null) {
                    intent.setPriceRange(new PriceRange(0, max));
                } else {
                    intent.getPriceRange().setMax(max);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (message.matches(".*\\d+\\s*[元块]\\s*以上.*")) {
            try {
                int min = Integer.parseInt(message.replaceAll(".*?(\\d+)\\s*[元块]\\s*以上.*", "$1"));
                if (intent.getPriceRange() == null) {
                    intent.setPriceRange(new PriceRange(min, Integer.MAX_VALUE));
                } else {
                    intent.getPriceRange().setMin(min);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (message.matches(".*\\d+\\s*[-到~]\\s*\\d+.*")) {
            String temp = message.replaceAll(".*?(\\d+)\\s*[-到~]\\s*(\\d+).*", "$1,$2");
            String[] parts = temp.split(",");
            if (parts.length == 2) {
                try {
                    intent.setPriceRange(new PriceRange(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (message.matches(".*预算\\s*\\d+.*")) {
            try {
                int budget = Integer.parseInt(message.replaceAll(".*预算\\s*(\\d+).*", "$1"));
                if (intent.getPriceRange() == null) {
                    intent.setPriceRange(new PriceRange(0, budget));
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void extractKeywordsFromMessage(String message, UserIntent intent) {
        String cleaned = message.replaceAll("[，,。.！!？?、\\s]+", " ");
        String[] words = cleaned.split("\\s+");

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "我", "想", "要", "买", "找", "给", "一个", "一些", "什么", "哪", "哪些", "有", "没有", "的", "了", "是", "在"
        ));

        for (String word : words) {
            if (word.length() >= 2 && !stopWords.contains(word)) {
                intent.getKeywords().add(word);
            }
        }

        if (intent.getKeywords().size() > 10) {
            intent.setKeywords(new ArrayList<>(intent.getKeywords().subList(0, 10)));
        }
    }

    private void extractPreferences(String lowerMsg, UserIntent intent) {
        if (lowerMsg.contains("性价比")) intent.getPreferences().add("性价比高");
        if (lowerMsg.contains("质量好")) intent.getPreferences().add("质量好");
        if (lowerMsg.contains("品牌")) intent.getPreferences().add("品牌");
        if (lowerMsg.contains("好看")) intent.getPreferences().add("外观好看");
        if (lowerMsg.contains("实用")) intent.getPreferences().add("实用");
        if (lowerMsg.contains("耐用")) intent.getPreferences().add("耐用");
        if (lowerMsg.contains("环保")) intent.getPreferences().add("环保");
        if (lowerMsg.contains("包邮")) intent.getPreferences().add("包邮");
        if (lowerMsg.contains("正品")) intent.getPreferences().add("正品保障");
    }

    private List<ShopGuideProductVo> filterProductsFromDb(UserIntent intent) {
        List<PmsProductVo> allProducts = productMapper.selectProductList(null, 1, null);
        if (allProducts == null || allProducts.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScoredProduct> scoredProducts = new ArrayList<>();

        for (PmsProductVo product : allProducts) {
            double score = calculateMatchScore(product, intent);
            if (score > 0) {
                ScoredProduct sp = new ScoredProduct();
                sp.setProduct(product);
                sp.setScore(score);
                sp.setMatchReason(generateMatchReason(product, intent, score));
                scoredProducts.add(sp);
            }
        }

        sortProducts(scoredProducts, intent.getSortBy());

        int limit = Math.min(scoredProducts.size(), 10);
        List<ShopGuideProductVo> result = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            ScoredProduct sp = scoredProducts.get(i);
            ShopGuideProductVo vo = convertToShopGuideProduct(sp.getProduct());
            vo.setMatchReason(sp.getMatchReason());
            result.add(vo);
        }

        return result;
    }

    private double calculateMatchScore(PmsProductVo product, UserIntent intent) {
        double score = 0;

        String productName = product.getProductName() != null ? product.getProductName().toLowerCase() : "";
        String categoryName = product.getCategoryName() != null ? product.getCategoryName().toLowerCase() : "";
        String detailHtml = product.getDetailHtml() != null ? product.getDetailHtml().replaceAll("<[^>]*>", "").toLowerCase() : "";
        String combinedText = productName + " " + categoryName + " " + detailHtml;

        if (intent.getKeywords() != null && !intent.getKeywords().isEmpty()) {
            int matchCount = 0;
            for (String keyword : intent.getKeywords()) {
                if (keyword != null && combinedText.contains(keyword.toLowerCase())) {
                    score += 15;
                    matchCount++;
                    if (productName.contains(keyword.toLowerCase())) {
                        score += 10;
                    }
                }
            }
            if (matchCount > 0) score += matchCount * 5;
        }

        if (intent.getCategory() != null && categoryName.contains(intent.getCategory().toLowerCase())) {
            score += 25;
        }

        BigDecimal price = getProductMinPrice(product);
        if (intent.getPriceRange() != null && price != null) {
            PriceRange range = intent.getPriceRange();
            boolean inRange = true;
            if (range.getMin() != null && price.compareTo(BigDecimal.valueOf(range.getMin())) < 0) inRange = false;
            if (range.getMax() != null && price.compareTo(BigDecimal.valueOf(range.getMax())) > 0) inRange = false;
            if (inRange) {
                score += 30;
            } else {
                score -= 50;
            }
        }

        if (intent.getPreferences() != null) {
            for (String pref : intent.getPreferences()) {
                if (pref != null) {
                    String prefLower = pref.toLowerCase();
                    if (prefLower.contains("性价比") && price != null && price.compareTo(new BigDecimal("200")) < 0) {
                        score += 10;
                    }
                    if (prefLower.contains("热门") && product.getIsHot() != null && product.getIsHot() == 1) {
                        score += 15;
                    }
                    if (prefLower.contains("新品") && product.getCreateTime() != null) {
                        long daysDiff = Duration.between(
                                product.getCreateTime().atZone(ZoneId.systemDefault()).toInstant(),
                                java.time.Instant.now()).toDays();
                        if (daysDiff < 30) score += 15;
                    }
                }
            }
        }

        if (product.getIsHot() != null && product.getIsHot() == 1) score += 8;

        if (product.getCreateTime() != null) {
            long daysDiff = Duration.between(
                    product.getCreateTime().atZone(ZoneId.systemDefault()).toInstant(),
                    java.time.Instant.now()).toDays();
            if (daysDiff < 7) score += 20;
            else if (daysDiff < 30) score += 10;
            else if (daysDiff < 90) score += 5;
        }

        return score;
    }

    private String generateMatchReason(PmsProductVo product, UserIntent intent, double score) {
        List<String> reasons = new ArrayList<>();

        if (intent.getKeywords() != null && product.getProductName() != null) {
            for (String keyword : intent.getKeywords()) {
                if (keyword != null && product.getProductName().toLowerCase().contains(keyword.toLowerCase())) {
                    reasons.add("名称包含「" + keyword + "」");
                    break;
                }
            }
        }

        if (intent.getCategory() != null && product.getCategoryName() != null
                && product.getCategoryName().toLowerCase().contains(intent.getCategory().toLowerCase())) {
            reasons.add("属于「" + intent.getCategory() + "」分类");
        }

        if (intent.getPriceRange() != null && getProductMinPrice(product) != null) {
            reasons.add("价格符合预期");
        }

        if (product.getIsHot() != null && product.getIsHot() == 1) reasons.add("热门商品");

        if (score > 50) reasons.add("高度匹配");
        else if (score > 30) reasons.add("匹配度较高");

        return reasons.isEmpty() ? "根据您的需求推荐" : String.join("，", reasons);
    }

    private void sortProducts(List<ScoredProduct> scoredProducts, String sortBy) {
        if (sortBy == null) sortBy = "relevance";

        switch (sortBy) {
            case "price_asc":
                scoredProducts.sort((a, b) -> comparePrices(
                        getProductMinPrice(a.getProduct()), getProductMinPrice(b.getProduct())));
                break;
            case "price_desc":
                scoredProducts.sort((a, b) -> comparePrices(
                        getProductMinPrice(b.getProduct()), getProductMinPrice(a.getProduct())));
                break;
            case "hot":
                scoredProducts.sort((a, b) -> {
                    boolean aHot = a.getProduct().getIsHot() != null && a.getProduct().getIsHot() == 1;
                    boolean bHot = b.getProduct().getIsHot() != null && b.getProduct().getIsHot() == 1;
                    return Boolean.compare(bHot, aHot);
                });
                break;
            case "newest":
                scoredProducts.sort((a, b) -> {
                    if (a.getProduct().getCreateTime() == null && b.getProduct().getCreateTime() == null) return 0;
                    if (a.getProduct().getCreateTime() == null) return 1;
                    if (b.getProduct().getCreateTime() == null) return -1;
                    return b.getProduct().getCreateTime().compareTo(a.getProduct().getCreateTime());
                });
                break;
            default:
                scoredProducts.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        }
    }

    private int comparePrices(BigDecimal p1, BigDecimal p2) {
        if (p1 == null && p2 == null) return 0;
        if (p1 == null) return 1;
        if (p2 == null) return -1;
        return p1.compareTo(p2);
    }

    private BigDecimal getProductMinPrice(PmsProductVo product) {
        if (product.getSkuList() == null || product.getSkuList().isEmpty()) return null;
        BigDecimal minPrice = null;
        for (PmsSkuVo sku : product.getSkuList()) {
            if (sku.getPrice() != null && (minPrice == null || sku.getPrice().compareTo(minPrice) < 0)) {
                minPrice = sku.getPrice();
            }
        }
        return minPrice;
    }

    private ShopGuideProductVo convertToShopGuideProduct(PmsProductVo p) {
        ShopGuideProductVo vo = new ShopGuideProductVo();
        vo.setId(p.getId());
        vo.setProductName(p.getProductName());
        vo.setPic(p.getPic());
        vo.setPrice(getProductMinPrice(p));
        vo.setCategoryName(p.getCategoryName());
        vo.setShortDesc(truncateDescription(p.getDetailHtml()));
        vo.setIsHot(p.getIsHot());
        return vo;
    }

    private String truncateDescription(String detailHtml) {
        if (detailHtml == null || detailHtml.isEmpty()) return null;
        String plainText = detailHtml.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        if (plainText.length() > 50) return plainText.substring(0, 50) + "...";
        return plainText;
    }

    private String generateSearchSummary(UserIntent intent, int count) {
        StringBuilder summary = new StringBuilder();
        summary.append("为您找到").append(count).append("件相关商品");
        if (intent.getCategory() != null) summary.append("（").append(intent.getCategory()).append("）");
        return summary.toString();
    }

    private String generateGuideReply(String userMessage, List<ShopGuideProductVo> products, UserIntent intent) {
        StringBuilder reply = new StringBuilder();
        reply.append("您好！根据您的需求「").append(userMessage).append("」\n");
        if (products.isEmpty()) {
            reply.append("抱歉，暂时没有找到完全符合您要求的商品。");
            return reply.toString();
        }
        reply.append("为您精选了").append(products.size()).append("件商品：");
        if (intent.getCategory() != null) reply.append("\n✨ 分类：").append(intent.getCategory());
        reply.append("\n\n点击商品即可查看详情，如有其他需求请随时告诉我~");
        return reply.toString();
    }

    private String generateNoMatchReply(String userMessage) {
        try {
            String prompt = "用户搜索了「" + userMessage + "」，但数据库中没有匹配的商品。请你作为导购助手，友好地告知用户并给出建议（如调整搜索词、尝试其他分类等），回复控制在100字以内。";
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("AI生成无匹配回复失败: {}", e.getMessage());
            StringBuilder reply = new StringBuilder();
            reply.append("抱歉，暂时没有找到与「").append(userMessage).append("」相关的商品。\n");
            reply.append("\n💡 您可以尝试：");
            reply.append("\n• 使用更通用的关键词搜索");
            reply.append("\n• 调整价格范围");
            reply.append("\n• 尝试其他分类");
            reply.append("\n\n如有疑问，欢迎联系人工客服咨询！");
            return reply.toString();
        }
    }

    @Data
    private static class UserIntent {
        private List<String> keywords;
        private String category;
        private PriceRange priceRange;
        private List<String> preferences;
        private String sortBy;
    }

    @Data
    private static class PriceRange {
        private Integer min;
        private Integer max;

        public PriceRange(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }
    }

    @Data
    private static class ScoredProduct {
        private PmsProductVo product;
        private double score;
        private String matchReason;
    }
}
