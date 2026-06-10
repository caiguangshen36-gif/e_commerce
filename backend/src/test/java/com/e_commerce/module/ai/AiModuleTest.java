package com.e_commerce.module.ai;

import com.e_commerce.module.ai.controller.AiController;
import com.e_commerce.module.ai.controller.AiKnowledgeBaseController;
import com.e_commerce.module.ai.dto.*;
import com.e_commerce.module.ai.entity.AiKnowledgeBase;
import com.e_commerce.module.ai.service.AiKnowledgeBaseService;
import com.e_commerce.module.ai.service.SmartCustomerService;
import com.e_commerce.module.ai.service.SmartShopGuideService;
import com.e_commerce.module.ai.vo.GoodsDescResponse;
import com.e_commerce.module.ai.vo.ShopGuideProductVo;
import com.e_commerce.module.ai.vo.ShopGuideResponse;
import com.e_commerce.module.ai.vo.SmartSearchResponse;
import com.e_commerce.common.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiModuleTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private SmartCustomerService smartCustomerService;
    private SmartShopGuideService smartShopGuideService;
    private AiKnowledgeBaseService knowledgeBaseService;
    private AiController aiController;
    private AiKnowledgeBaseController knowledgeBaseController;

    @BeforeEach
    void setUp() {
        // Setup ChatClient mocks
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        smartCustomerService = mock(SmartCustomerService.class);
        smartShopGuideService = mock(SmartShopGuideService.class);
        knowledgeBaseService = mock(AiKnowledgeBaseService.class);

        aiController = new AiController();
        ReflectionTestUtils.setField(aiController, "chatClient", chatClient);
        ReflectionTestUtils.setField(aiController, "smartCustomerService", smartCustomerService);
        ReflectionTestUtils.setField(aiController, "smartShopGuideService", smartShopGuideService);

        knowledgeBaseController = new AiKnowledgeBaseController();
        ReflectionTestUtils.setField(knowledgeBaseController, "knowledgeBaseService", knowledgeBaseService);
    }

    // ==================== AiController Tests ====================

    @Test
    void testChat_ShouldReturnSuccess() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("你好");
        when(callResponseSpec.content()).thenReturn("你好，我是AI助手");

        Result<String> result = aiController.chat(request);

        assertEquals(200, result.getCode());
        assertEquals("你好，我是AI助手", result.getData());
    }

    @Test
    void testCustomerService_ShouldReturnSuccess() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("如何退货");
        when(smartCustomerService.handleCustomerMessage("如何退货")).thenReturn("退货指南...");

        Result<String> result = aiController.customerService(request);

        assertEquals(200, result.getCode());
        assertEquals("退货指南...", result.getData());
    }

    @Test
    void testGenerateGoodsDesc_ShouldReturnSuccess() {
        GoodsDescRequest request = new GoodsDescRequest();
        request.setProductName("测试手机");
        request.setCategoryName("手机");
        request.setPrice(new BigDecimal("999"));
        when(callResponseSpec.content()).thenReturn(
            "{\"title\":\"超值测试手机\",\"subtitle\":\"性价比之王\",\"keywords\":[\"手机\",\"智能手机\"],\"tags\":[\"新品\",\"热卖\"]}");

        Result<GoodsDescResponse> result = aiController.generateGoodsDesc(request);

        assertEquals(200, result.getCode());
        assertEquals("超值测试手机", result.getData().getTitle());
    }

    @Test
    void testIntelligentSearch_ShouldReturnSuccess() {
        SmartSearchRequest request = new SmartSearchRequest();
        request.setKeyword("shouji");
        when(callResponseSpec.content()).thenReturn(
            "{\"original\":\"shouji\",\"corrected\":\"手机\",\"synonyms\":[\"移动电话\",\"智能手机\",\"cellphone\"],\"related\":[\"手机壳\",\"充电器\",\"耳机\"]}");

        Result<SmartSearchResponse> result = aiController.intelligentSearch(request);

        assertEquals(200, result.getCode());
        assertEquals("手机", result.getData().getCorrected());
    }

    @Test
    void testIntelligentSearch_EmptyKeyword_ShouldReturnError() {
        SmartSearchRequest request = new SmartSearchRequest();
        request.setKeyword("");

        Result<SmartSearchResponse> result = aiController.intelligentSearch(request);

        assertEquals(500, result.getCode());
    }

    @Test
    void testShopGuide_ShouldReturnSuccess() {
        ShopGuideRequest request = new ShopGuideRequest();
        request.setMessage("便宜的手机");

        ShopGuideResponse mockResp = new ShopGuideResponse();
        mockResp.setReplyMessage("为您找到3件商品");
        mockResp.setTotalCount(3);
        mockResp.setProducts(new ArrayList<>());
        when(smartShopGuideService.guide(request)).thenReturn(mockResp);

        Result<ShopGuideResponse> result = aiController.shopGuide(request);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().getTotalCount());
    }

    @Test
    void testShopGuide_EmptyMessage_ShouldReturnError() {
        ShopGuideRequest request = new ShopGuideRequest();
        request.setMessage("");

        Result<ShopGuideResponse> result = aiController.shopGuide(request);

        assertEquals(500, result.getCode());
        verify(smartShopGuideService, never()).guide(any());
    }

    // ==================== AiKnowledgeBaseController Tests ====================

    @Test
    void testAddKnowledge_Success() {
        AiKnowledgeBaseRequest request = new AiKnowledgeBaseRequest();
        request.setTitle("退货规则");
        request.setContent("7天无理由退货");
        request.setType("after_sale");

        AiKnowledgeBase kb = new AiKnowledgeBase();
        kb.setId(1L);
        kb.setTitle("退货规则");
        when(knowledgeBaseService.add(request)).thenReturn(kb);

        Result<AiKnowledgeBase> result = knowledgeBaseController.add(request);

        assertEquals(200, result.getCode());
        assertEquals("退货规则", result.getData().getTitle());
    }

    @Test
    void testGetKnowledgeById_Success() {
        AiKnowledgeBase kb = new AiKnowledgeBase();
        kb.setId(1L);
        kb.setTitle("测试知识");
        when(knowledgeBaseService.getById(1L)).thenReturn(kb);

        Result<AiKnowledgeBase> result = knowledgeBaseController.getById(1L);

        assertEquals(200, result.getCode());
    }

    @Test
    void testGetKnowledgeById_NotFound_ShouldReturnError() {
        when(knowledgeBaseService.getById(999L)).thenReturn(null);

        Result<AiKnowledgeBase> result = knowledgeBaseController.getById(999L);

        assertEquals(500, result.getCode());
    }

    @Test
    void testDeleteKnowledge_Success() {
        when(knowledgeBaseService.delete(1L)).thenReturn(1);

        Result<Integer> result = knowledgeBaseController.delete(1L);

        assertEquals(200, result.getCode());
    }

    @Test
    void testSearchKnowledge_Success() {
        List<AiKnowledgeBase> list = new ArrayList<>();
        AiKnowledgeBase kb = new AiKnowledgeBase();
        kb.setTitle("退货规则");
        list.add(kb);
        when(knowledgeBaseService.search("退货")).thenReturn(list);

        Result<List<AiKnowledgeBase>> result = knowledgeBaseController.search("退货");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }
}
