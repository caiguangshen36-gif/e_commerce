package com.e_commerce.module.search.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.common.utils.Result;
import com.e_commerce.module.search.entity.UmsUserSearchHistory;
import com.e_commerce.module.search.mapper.UmsUserSearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UmsUserSearchHistoryService{

    private final UmsUserSearchHistoryMapper searchHistoryMapper;

    // 保存/更新搜索历史
    public void saveSearchHistory(Long userId, String keyword) {
        // 1. 查是否已存在
        LambdaQueryWrapper<UmsUserSearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsUserSearchHistory::getUserId, userId)
                .eq(UmsUserSearchHistory::getKeyword, keyword);

        UmsUserSearchHistory history = searchHistoryMapper.selectOne(wrapper);

        if (history != null) {
            // 存在 → 更新时间
            LambdaUpdateWrapper<UmsUserSearchHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UmsUserSearchHistory::getId, history.getId())
                    .set(UmsUserSearchHistory::getSearchTime, LocalDateTime.now());
            searchHistoryMapper.update(null, updateWrapper);
        } else {
            // 不存在 → 新增
            UmsUserSearchHistory newHistory = new UmsUserSearchHistory();
            newHistory.setUserId(userId);
            newHistory.setKeyword(keyword);
            newHistory.setSearchTime(LocalDateTime.now());
            searchHistoryMapper.insert(newHistory);
        }
    }

    // 获取最近10条搜索历史
    public List<String> getSearchHistory(Long userId) {
        return searchHistoryMapper.getUserHistoryKeywords(userId);
    }

    public void deleteSearchHistory(Long userId, String keyword) {
        searchHistoryMapper.delete(new LambdaQueryWrapper<UmsUserSearchHistory>()
                .eq(UmsUserSearchHistory::getUserId, userId)
                .eq(UmsUserSearchHistory::getKeyword, keyword));
    }

    // 清空搜索历史
    public void clearSearchHistory(Long userId) {
        searchHistoryMapper.delete(new LambdaQueryWrapper<UmsUserSearchHistory>()
                .eq(UmsUserSearchHistory::getUserId, userId));
    }
}
