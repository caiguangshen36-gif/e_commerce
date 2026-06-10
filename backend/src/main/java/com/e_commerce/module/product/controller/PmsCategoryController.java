package com.e_commerce.module.product.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.module.product.entity.PmsCategory;
import com.e_commerce.module.product.service.PmsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/category")
public class PmsCategoryController {

    @Autowired
    private PmsCategoryService pmsCategoryService;

    @PostMapping("/list")
    public Result<List<PmsCategory>> list(){
        return Result.success(pmsCategoryService.list());
    }

    @PostMapping("/admin/list")
    public Result<List<PmsCategory>> adminList(@RequestBody Map<String, Object> params) {
        String categoryName = (String) params.get("categoryName");
        Long parentId = params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;

        System.out.println("前端传过来的 categoryName：" + categoryName);
        System.out.println("前端传过来的 parentId：" + parentId);
        System.out.println("前端传过来的 status：" + status);

        return Result.success(pmsCategoryService.listByCondition(categoryName, parentId, status));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated PmsCategory pmsCategory){
        pmsCategoryService.add(pmsCategory);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody @Validated PmsCategory pmsCategory) {
        pmsCategoryService.update(pmsCategory);
        return Result.success("更新成功");
    }

    //按ID删除
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> params){
        pmsCategoryService.deleteById(params.get("id"));
        return Result.success("删除成功");
    }

    // 更新状态（上下架）
    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params){
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        pmsCategoryService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    // 根据父ID查询子分类
    @PostMapping("/listByParentId")
    public Result<List<PmsCategory>> listByParentId(@RequestBody Map<String, Long> params){
        return Result.success(pmsCategoryService.getByParentId(params.get("parentId")));
    }

    // 根据ID查询分类详情
    @PostMapping("/detail")
    public Result<PmsCategory> detail(@RequestBody Map<String, Long> params){
        return Result.success(pmsCategoryService.getById(params.get("id")));
    }
}