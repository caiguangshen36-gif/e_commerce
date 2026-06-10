package com.e_commerce.module.product.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.product.service.PmsProductCollectService;
import com.e_commerce.module.product.vo.PmsCollectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品收藏控制器
 * 提供商品收藏相关的API接口，包括添加收藏、取消收藏、获取收藏列表和检查是否收藏等功能
 */
@RestController
@RequestMapping("/product/collect")
public class PmsProductCollectController {
    
    // 注入商品收藏服务
    @Autowired
    private PmsProductCollectService collectService;
    
    /**
     * 添加商品收藏
     * @param params 包含商品ID的参数映射
     * @return 返回操作结果，收藏成功信息
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId"); // 从参数中获取商品ID
        Long userId = ThreadLocalUtil.getUserId(); // 获取当前登录用户ID
        collectService.addCollect(userId, productId); // 调用服务层添加收藏
        return Result.success("收藏成功"); // 返回成功结果
    }
    
    /**
     * 取消商品收藏
     * @param params 包含商品ID的参数映射
     * @return 返回操作结果，取消收藏成功信息
     */
    @PostMapping("/remove")
    public Result<String> remove(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId"); // 从参数中获取商品ID
        Long userId = ThreadLocalUtil.getUserId(); // 获取当前登录用户ID
        collectService.removeCollect(userId, productId); // 调用服务层取消收藏
        return Result.success("取消收藏成功"); // 返回成功结果
    }

    /**
     * 获取用户的收藏列表（条件查询）
     * @param params 查询参数
     * @return 返回用户的收藏商品列表
     */
    @PostMapping("/list")
    public Result<PageVo<PmsCollectVo>> listByCondition(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String productName = (String) params.get("productName");
        Long categoryId = params.get("categoryId") != null ? Long.valueOf(params.get("categoryId").toString()) : null;
        return Result.success(collectService.getCollectListByCondition(pageNum, pageSize, userId, productName, categoryId));
    }
    
    /**
     * 检查用户是否收藏了指定商品
     * @param params 包含商品ID的参数映射
     * @return 返回是否收藏的结果
     */
    @PostMapping("/isCollected")
    public Result<Boolean> isCollected(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId"); // 从参数中获取商品ID
        Long userId = ThreadLocalUtil.getUserId(); // 获取当前登录用户ID
        return Result.success(collectService.isCollected(userId, productId)); // 返回收藏状态
    }

    @PostMapping("/clear")
    public Result<String> clearCollect(){
        Long userId = ThreadLocalUtil.getUserId(); // 获取当前登录用户ID
        collectService.clear(userId);
        return Result.success("清除成功");
    }

    @PostMapping("/batchRemove")
    public Result<?> batchRemove(@RequestBody List<Long> productIds) {
        Long userId = ThreadLocalUtil.getUserId();
        collectService.batchRemoveCollect(userId, productIds);
        return Result.success("批量取消收藏成功");
    }

}
