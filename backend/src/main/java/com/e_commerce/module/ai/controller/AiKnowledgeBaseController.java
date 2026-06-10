package com.e_commerce.module.ai.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.ai.dto.AiKnowledgeBaseRequest;
import com.e_commerce.module.ai.entity.AiKnowledgeBase;
import com.e_commerce.module.ai.service.AiKnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI知识库管理接口
 */
@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeBaseController {
    
    @Autowired
    private AiKnowledgeBaseService knowledgeBaseService;
    
    /**
     * 添加知识库
     */
    @PostMapping("/add")
    public Result<AiKnowledgeBase> add(@Valid @RequestBody AiKnowledgeBaseRequest request) {
        AiKnowledgeBase result = knowledgeBaseService.add(request);
        return Result.success(result);
    }
    
    /**
     * 更新知识库
     */
    @PutMapping("/update/{id}")
    public Result<Integer> update(@PathVariable Long id, @Valid @RequestBody AiKnowledgeBaseRequest request) {
        int rows = knowledgeBaseService.update(id, request);
        return Result.success(rows);
    }
    
    /**
     * 根据ID查询知识库
     */
    @GetMapping("/detail/{id}")
    public Result<AiKnowledgeBase> getById(@PathVariable Long id) {
        AiKnowledgeBase result = knowledgeBaseService.getById(id);
        return result != null ? Result.success(result) : Result.error("知识库不存在");
    }
    
    /**
     * 查询所有启用的知识库
     */
    @GetMapping("/list")
    public Result<List<AiKnowledgeBase>> getAllEnabled() {
        List<AiKnowledgeBase> list = knowledgeBaseService.getAllEnabled();
        return Result.success(list);
    }
    
    /**
     * 根据类型查询知识库
     */
    @GetMapping("/list/type/{type}")
    public Result<List<AiKnowledgeBase>> getByType(@PathVariable String type) {
        List<AiKnowledgeBase> list = knowledgeBaseService.getByType(type);
        return Result.success(list);
    }
    
    /**
     * 搜索知识库
     */
    @GetMapping("/search")
    public Result<List<AiKnowledgeBase>> search(@RequestParam(required = false) String keyword) {
        List<AiKnowledgeBase> list = knowledgeBaseService.search(keyword);
        return Result.success(list);
    }
    
    /**
     * 查询所有知识库（管理后台用，分页）
     */
    @GetMapping("/admin/list")
    public Result<PageVo<AiKnowledgeBase>> getAll(@RequestParam(defaultValue = "1") Long pageNum,
                                                   @RequestParam(defaultValue = "10") Long pageSize) {
        PageVo<AiKnowledgeBase> result = knowledgeBaseService.getAll(pageNum, pageSize);
        return Result.success(result);
    }
    
    /**
     * 删除知识库
     */
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int rows = knowledgeBaseService.delete(id);
        return Result.success(rows);
    }
    
    /**
     * 根据用户问题匹配知识库（用于AI客服调用）
     */
    @PostMapping("/match")
    public Result<List<AiKnowledgeBase>> match(@RequestBody String userMessage) {
        List<AiKnowledgeBase> matched = knowledgeBaseService.matchKnowledge(userMessage);
        return Result.success(matched);
    }
}
