package com.e_commerce.module.product.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.product.dto.PmsProductDto;
import com.e_commerce.module.product.service.PmsProductService;
import com.e_commerce.module.product.vo.PmsProductVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/product")
public class PmsProductController {
    @Autowired
    private PmsProductService pmsProductService;

    @PostMapping("/list")
    public Result<PageVo<PmsProductVo>> list(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String keyword = (String) params.get("keyword");
        Integer status = (Integer) params.get("status");

        // 处理 categoryId，支持 String 和 Number 两种类型
        Long categoryId = null;
        Object categoryIdObj = params.get("categoryId");
        if (categoryIdObj != null) {
            if (categoryIdObj instanceof Number) {
                categoryId = ((Number) categoryIdObj).longValue();
            } else if (categoryIdObj instanceof String) {
                String categoryIdStr = ((String) categoryIdObj).trim();
                if (!categoryIdStr.isEmpty()) {
                    categoryId = Long.valueOf(categoryIdStr);
                }
            }
        }

        System.out.println("前端搜索关键词：" + keyword);
        System.out.println("前端状态：" + status);
        System.out.println("前端分类ID：" + categoryId);

        PageVo<PmsProductVo> page = pmsProductService.getProductList(pageNum, pageSize, keyword, status, categoryId);
        return Result.success(page);
    }

//    @GetMapping("/listOfSku")
//    public Result<List<PmsProductVo>> listOfSku(@RequestParam Long productId) {
//        List<PmsProductVo> list = pmsProductService.getProductListBySku(productId);
//        return Result.success(list);
//    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated PmsProductDto productDto){
        pmsProductService.addProduct(productDto);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody @Validated PmsProductDto productDto){
        pmsProductService.updateProduct(productDto);
        return Result.success("更新成功");
    }

    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params){
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        pmsProductService.updateProductStatus(id, status);
        return Result.success("更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> params){
        pmsProductService.deleteProduct(params.get("id"));
        return Result.success("删除成功");
    }

    @PostMapping("/detail")
    public Result<PmsProductVo> detail(@RequestBody Map<String, Long> params){
        return Result.success(pmsProductService.getProductById(params.get("id")));
    }


    @PostMapping("/listByCategory")
    public Result<List<PmsProductVo>> listByCategory(@RequestBody Map<String, Long> params){
        return Result.success(pmsProductService.getProductsByCategoryId(params.get("categoryId")));
    }

    @GetMapping("/search")
    public Result<List<PmsProductVo>> searchProduct(@RequestParam String keyword) {
        List<PmsProductVo> list = pmsProductService.searchProduct(keyword);
        return Result.success(list);
    }

    /**
     * 更新商品热门状态
     * @param params 包含 id, isHot, hotSort
     * @return 操作结果
     */
    @PostMapping("/updateHotStatus")
    public Result<String> updateHotStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer isHot = (Integer) params.get("isHot");
        Integer hotSort = (Integer) params.get("hotSort");
        
        if (isHot == null) {
            isHot = 0;
        }
        if (hotSort == null) {
            hotSort = 0;
        }
        
        pmsProductService.updateHotStatus(id, isHot, hotSort);
        return Result.success("热门状态更新成功");
    }

    /**
     * 获取热门商品列表
     * @return 热门商品列表（is_hot=1），按hot_sort升序排序
     */
    @GetMapping("/hotList")
    public Result<List<PmsProductVo>> getHotProductList() {
        List<PmsProductVo> list = pmsProductService.getHotProductList();
        return Result.success(list);
    }

    /**
     * 获取混合推荐商品列表（收藏80% + 浏览20%）
     * @return 混合推荐商品列表，最多10个
     */
    @GetMapping("/recommend")
    public Result<List<PmsProductVo>> getRecommendList() {
        Long userId = ThreadLocalUtil.getUserId();
        List<PmsProductVo> list = pmsProductService.getMixedRecommendList(userId);
        return Result.success(list);
    }
}