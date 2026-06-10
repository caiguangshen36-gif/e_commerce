package com.e_commerce.module.ai.service;

import com.e_commerce.module.ai.entity.AiKnowledgeBase;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KnowledgeBaseRagService {

    @Autowired
    private AiKnowledgeBaseService knowledgeBaseService;

    @Autowired
    private EmbeddingModel embeddingModel;

    private VectorStore vectorStore;

    @PostConstruct
    public void init() {
        refreshVectorStore();
    }

    /**
     * 刷新向量存储——从数据库重新加载所有启用的知识库条目
     */
    public void refreshVectorStore() {
        List<AiKnowledgeBase> allEntries = knowledgeBaseService.getAllEnabled();
        SimpleVectorStore newVectorStore = new SimpleVectorStore(embeddingModel);

        if (allEntries == null || allEntries.isEmpty()) {
            log.warn("知识库无启用条目，跳过向量索引");
            this.vectorStore = newVectorStore;
            return;
        }

        List<Document> documents = new ArrayList<>();

        for (AiKnowledgeBase kb : allEntries) {
            String content = buildDocumentContent(kb);
            Map<String, Object> metadata = Map.of(
                    "id", kb.getId() != null ? kb.getId() : 0,
                    "title", kb.getTitle() != null ? kb.getTitle() : "",
                    "type", kb.getType() != null ? kb.getType() : "",
                    "keywords", kb.getKeywords() != null ? kb.getKeywords() : ""
            );
            documents.add(new Document(content, metadata));
        }

        newVectorStore.add(documents);
        this.vectorStore = newVectorStore;
        log.info("知识库向量索引完成，共 {} 条记录", documents.size());
    }

    /**
     * 语义检索知识库
     * @param query 用户问题
     * @param topK 返回条数
     * @return 最相关的知识库文档
     */
    public List<Document> searchRelevant(String query, int topK) {
        if (vectorStore == null) {
            log.warn("向量存储未初始化，返回空结果");
            return List.of();
        }
        try {
            return vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(topK).build());
        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 将检索到的文档拼接为上下文，用于增强 AI 提示词
     */
    public String buildContextFromDocs(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String title = doc.getMetadata() != null ? (String) doc.getMetadata().getOrDefault("title", "") : "";
            sb.append("【参考资料").append(i + 1).append("】");
            if (!title.isEmpty()) {
                sb.append(" ").append(title);
            }
            sb.append("\n").append(doc.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 增强用户提示词：将相关知识库内容注入 Prompt
     */
    public String enrichPrompt(String userMessage) {
        List<Document> relevantDocs = searchRelevant(userMessage, 3);
        if (relevantDocs.isEmpty()) {
            return null;
        }
        String context = buildContextFromDocs(relevantDocs);
        return """
            以下是平台知识库中的相关信息，请严格据此回答用户问题，不要编造知识库以外的政策或规则：

            %s
            ---
            用户问题：%s
            请根据以上知识库内容给出专业回答：
            """.formatted(context, userMessage);
    }

    private String buildDocumentContent(AiKnowledgeBase kb) {
        StringBuilder sb = new StringBuilder();
        if (kb.getTitle() != null) {
            sb.append("标题：").append(kb.getTitle()).append("\n");
        }
        if (kb.getType() != null) {
            sb.append("分类：").append(getTypeLabel(kb.getType())).append("\n");
        }
        if (kb.getContent() != null) {
            sb.append("内容：").append(kb.getContent());
        }
        return sb.toString();
    }

    private String getTypeLabel(String type) {
        if (type == null) return "其他";
        switch (type) {
            case "after_sale": return "售后规则";
            case "goods": return "商品规则";
            case "service": return "服务规则";
            case "payment": return "支付规则";
            case "order": return "订单规则";
            case "logistics": return "物流规则";
            default: return type;
        }
    }
}
