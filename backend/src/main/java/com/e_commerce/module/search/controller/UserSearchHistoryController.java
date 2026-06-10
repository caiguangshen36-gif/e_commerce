package com.e_commerce.module.search.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.search.dto.SearchHistoryDTO;
import com.e_commerce.module.search.service.UmsUserSearchHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/search")
@RequiredArgsConstructor
public class UserSearchHistoryController {

    private final UmsUserSearchHistoryService searchHistoryService;

    // 获取当前用户搜索历史
    @GetMapping("/history")
    public Result<List<String>> history() {
        Long userId = ThreadLocalUtil.getUserId();
        List<String> list = searchHistoryService.getSearchHistory(userId);
        return Result.success(list);
    }

    // 保存搜索历史
    @PostMapping("/save")
    public Result<String> save(@Valid @RequestBody SearchHistoryDTO dto) {
        Long userId = ThreadLocalUtil.getUserId();
        searchHistoryService.saveSearchHistory(userId, dto.getKeyword());
        return Result.success("保存成功");
    }

    // 删除搜索历史
    @PostMapping("/delete")
    public Result<String> delete(@Valid @RequestBody SearchHistoryDTO dto) {
        Long userId = ThreadLocalUtil.getUserId();
        searchHistoryService.deleteSearchHistory(userId, dto.getKeyword());
        return Result.success("删除成功");
    }

    // 清空搜索历史
    @PostMapping("/clear")
    public Result<String> clear() {
        Long userId = ThreadLocalUtil.getUserId();
        searchHistoryService.clearSearchHistory(userId);
        return Result.success("清除成功");
    }
}