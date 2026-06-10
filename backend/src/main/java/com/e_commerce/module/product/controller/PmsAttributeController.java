package com.e_commerce.module.product.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.module.product.dto.PmsAttributeDto;
import com.e_commerce.module.product.dto.PmsAttributeValueDto;
import com.e_commerce.module.product.service.PmsAttributeService;
import com.e_commerce.module.product.vo.PmsAttributeVo;
import com.e_commerce.module.product.vo.PmsAttributeValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/attribute")
public class PmsAttributeController {
    
    @Autowired
    private PmsAttributeService attributeService;
    
    @PostMapping("/list")
    public Result<List<PmsAttributeVo>> list() {
        return Result.success(attributeService.getAttributeList());
    }

    @PostMapping("/admin/list")
    public Result<List<PmsAttributeVo>> adminList(@RequestBody Map<String, Object> params) {
        String attrName = (String) params.get("attrName");
        Long categoryId = params.get("categoryId") != null ? Long.valueOf(params.get("categoryId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;

        System.out.println("前端传过来的 attrName：" + attrName);
        System.out.println("前端传过来的 categoryId：" + categoryId);
        System.out.println("前端传过来的 status：" + status);

        return Result.success(attributeService.getAttributeListByCondition(attrName, categoryId, status));
    }
    
    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated PmsAttributeDto attributeDto) {
        attributeService.addAttribute(attributeDto);
        return Result.success("添加成功");
    }
    
    @PostMapping("/update")
    public Result<String> update(@RequestBody @Validated PmsAttributeDto attributeDto) {
        attributeService.updateAttribute(attributeDto);
        return Result.success("更新成功");
    }
    
    @PostMapping("/detail")
    public Result<PmsAttributeVo> detail(@RequestBody Map<String, Long> params) {
        return Result.success(attributeService.getAttributeById(params.get("id")));
    }
    
    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        attributeService.updateAttributeStatus(id, status);
        return Result.success("更新成功");
    }
    
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> params) {
        attributeService.deleteAttribute(params.get("id"));
        return Result.success("删除成功");
    }
    
    @PostMapping("/listByCategory")
    public Result<List<PmsAttributeVo>> listByCategory(@RequestBody Map<String, Long> params) {
        return Result.success(attributeService.getAttributesByCategoryId(params.get("categoryId")));
    }

    // ==================== 规格值操作接口 ====================

    @PostMapping("/value/listByAttr")
    public Result<List<PmsAttributeValueVo>> listValuesByAttr(@RequestBody Map<String, Long> params) {
        return Result.success(attributeService.getAttributeValuesByAttrId(params.get("attrId")));
    }

    @PostMapping("/value/detail")
    public Result<PmsAttributeValueVo> valueDetail(@RequestBody Map<String, Long> params) {
        return Result.success(attributeService.getAttributeValueById(params.get("id")));
    }

    @PostMapping("/value/add")
    public Result<String> addValue(@RequestBody @Validated PmsAttributeValueDto valueDto) {
        attributeService.addAttributeValue(valueDto);
        return Result.success("添加成功");
    }

    @PostMapping("/value/update")
    public Result<String> updateValue(@RequestBody @Validated PmsAttributeValueDto valueDto) {
        attributeService.updateAttributeValue(valueDto);
        return Result.success("更新成功");
    }

    @PostMapping("/value/updateStatus")
    public Result<String> updateValueStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        attributeService.updateAttributeValueStatus(id, status);
        return Result.success("更新成功");
    }

    @PostMapping("/value/delete")
    public Result<String> deleteValue(@RequestBody Map<String, Long> params) {
        attributeService.deleteAttributeValue(params.get("id"));
        return Result.success("删除成功");
    }
}
