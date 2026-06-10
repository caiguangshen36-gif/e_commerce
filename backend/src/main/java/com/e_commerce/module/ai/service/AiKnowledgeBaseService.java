package com.e_commerce.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.ai.dto.AiKnowledgeBaseRequest;
import com.e_commerce.module.ai.entity.AiKnowledgeBase;
import com.e_commerce.module.ai.mapper.AiKnowledgeBaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库服务接口
 */
/**
 * 知识库服务实现类
 */
@Slf4j
@Service
public class AiKnowledgeBaseService {

    @Autowired
    private AiKnowledgeBaseMapper knowledgeBaseMapper;

    @Transactional(rollbackFor = Exception.class)
    public AiKnowledgeBase add(AiKnowledgeBaseRequest request) {
        AiKnowledgeBase knowledgeBase = new AiKnowledgeBase();
        knowledgeBase.setTitle(request.getTitle());
        knowledgeBase.setContent(request.getContent());
        knowledgeBase.setType(request.getType());
        knowledgeBase.setKeywords(request.getKeywords());
        knowledgeBase.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        knowledgeBaseMapper.insert(knowledgeBase);
        log.info("新增知识库成功，ID: {}", knowledgeBase.getId());
        return knowledgeBase;
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Long id, AiKnowledgeBaseRequest request) {
        AiKnowledgeBase knowledgeBase = new AiKnowledgeBase();
        knowledgeBase.setId(id);
        knowledgeBase.setTitle(request.getTitle());
        knowledgeBase.setContent(request.getContent());
        knowledgeBase.setType(request.getType());
        knowledgeBase.setKeywords(request.getKeywords());
        knowledgeBase.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        int rows = knowledgeBaseMapper.updateById(knowledgeBase);
        log.info("更新知识库，ID: {}, 影响行数: {}", id, rows);
        return rows;
    }

    public AiKnowledgeBase getById(Long id) {
        return knowledgeBaseMapper.selectById(id);
    }

    public List<AiKnowledgeBase> getAllEnabled() {
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<AiKnowledgeBase>()
                .eq(AiKnowledgeBase::getStatus, 1)
                .orderByAsc(AiKnowledgeBase::getType)
                .orderByDesc(AiKnowledgeBase::getCreateTime));
    }

    public List<AiKnowledgeBase> getByType(String type) {
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<AiKnowledgeBase>()
                .eq(AiKnowledgeBase::getType, type)
                .eq(AiKnowledgeBase::getStatus, 1)
                .orderByDesc(AiKnowledgeBase::getCreateTime));
    }

    public List<AiKnowledgeBase> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEnabled();
        }
        String k = keyword.trim();
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<AiKnowledgeBase>()
                .eq(AiKnowledgeBase::getStatus, 1)
                .and(w -> w.like(AiKnowledgeBase::getTitle, k)
                        .or().like(AiKnowledgeBase::getContent, k)
                        .or().like(AiKnowledgeBase::getKeywords, k))
                .orderByAsc(AiKnowledgeBase::getType)
                .orderByDesc(AiKnowledgeBase::getCreateTime));
    }

    public PageVo<AiKnowledgeBase> getAll(Long pageNum, Long pageSize) {
        Page<AiKnowledgeBase> mpPage = new Page<>(pageNum, pageSize);
        Page<AiKnowledgeBase> result = knowledgeBaseMapper.selectPage(mpPage,
                new LambdaQueryWrapper<AiKnowledgeBase>()
                        .orderByAsc(AiKnowledgeBase::getType)
                        .orderByDesc(AiKnowledgeBase::getCreateTime));

        PageVo<AiKnowledgeBase> pageVo = new PageVo<>();
        pageVo.setList(result.getRecords());
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        int rows = knowledgeBaseMapper.deleteById(id);
        log.info("删除知识库，ID: {}, 影响行数: {}", id, rows);
        return rows;
    }

    public List<AiKnowledgeBase> matchKnowledge(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerMessage = userMessage.toLowerCase();
        List<AiKnowledgeBase> allEnabled = getAllEnabled();
        List<AiKnowledgeBase> matched = new ArrayList<>();

        for (AiKnowledgeBase kb : allEnabled) {
            boolean matchedFlag = false;

            // 检查标题匹配
            if (kb.getTitle() != null && kb.getTitle().toLowerCase().contains(lowerMessage)) {
                matchedFlag = true;
            }

            // 检查内容匹配
            if (!matchedFlag && kb.getContent() != null && kb.getContent().toLowerCase().contains(lowerMessage)) {
                matchedFlag = true;
            }

            // 检查关键词匹配
            if (!matchedFlag && kb.getKeywords() != null) {
                String[] keywords = kb.getKeywords().split(",");
                for (String kw : keywords) {
                    if (lowerMessage.contains(kw.trim().toLowerCase())) {
                        matchedFlag = true;
                        break;
                    }
                }
            }

            if (matchedFlag) {
                matched.add(kb);
            }
        }

        return matched;
    }

    public List<AiKnowledgeBase> matchByTypeAndKeywords(String type, String keywords) {
        LambdaQueryWrapper<AiKnowledgeBase> wrapper = new LambdaQueryWrapper<AiKnowledgeBase>()
                .eq(AiKnowledgeBase::getStatus, 1);
        if (type != null) {
            wrapper.eq(AiKnowledgeBase::getType, type);
        }
        if (keywords != null && !keywords.isEmpty()) {
            wrapper.like(AiKnowledgeBase::getKeywords, keywords);
        }
        wrapper.orderByDesc(AiKnowledgeBase::getCreateTime);
        return knowledgeBaseMapper.selectList(wrapper);
    }
}
