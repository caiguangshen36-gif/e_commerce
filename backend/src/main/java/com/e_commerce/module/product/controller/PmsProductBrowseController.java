package com.e_commerce.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.product.entity.PmsProductBrowse;
import com.e_commerce.module.product.service.PmsProductBrowseService;
import com.e_commerce.module.product.vo.PmsBrowseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/browse")
public class PmsProductBrowseController {
    
    @Autowired
    private PmsProductBrowseService browseService;
    
    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId");
        Long userId = ThreadLocalUtil.getUserId();
        browseService.addBrowse(userId, productId);
        return Result.success("浏览记录添加成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId");
        Long userId = ThreadLocalUtil.getUserId();
        browseService.deleteBrowse(userId, productId);
        return Result.success("浏览记录删除成功");
    }


    @PostMapping("/list")
    public Result<PageVo<PmsBrowseVo>> listByCondition(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String productName = (String) params.get("productName");
        Long categoryId = params.get("categoryId") != null ? Long.valueOf(params.get("categoryId").toString()) : null;
        PageVo<PmsBrowseVo> page = browseService.getBrowseListByCondition(pageNum, pageSize, userId, productName, categoryId);
        return Result.success(page);
    }
    
    @PostMapping("/clear")
    public Result<String> clear() {
        Long userId = ThreadLocalUtil.getUserId();
        browseService.clearBrowse(userId);
        return Result.success("浏览记录清空成功");
    }

    @PostMapping("/batchDelete")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        Long userId = ThreadLocalUtil.getUserId();
        browseService.batchDeleteBrowse(userId, ids);
        return Result.success();
    }
}
