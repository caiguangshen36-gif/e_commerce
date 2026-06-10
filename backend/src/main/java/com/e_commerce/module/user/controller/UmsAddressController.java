package com.e_commerce.module.user.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.user.dto.UserAddressDto;
import com.e_commerce.module.user.entity.UmsAddress;
import com.e_commerce.module.user.service.UmsAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ums/address")
public class UmsAddressController {
    @Autowired
    private UmsAddressService umsAddressService;

    @PostMapping("List")
    public Result<List<UmsAddress>> list(){
        return Result.success(umsAddressService.listByUserId());
    }
    @PostMapping("/add")
    public Result<String> add(@RequestBody UserAddressDto umsAddress){
        umsAddressService.add(umsAddress);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody UserAddressDto umsAddress){
        umsAddressService.update(umsAddress);
        return Result.success("更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> map){
        Long id = map.get("id");
        if (id == null) {
            return Result.error("参数错误,id不能为空");
        }
        umsAddressService.delete(id);
        return Result.success("删除成功");
    }

    @PostMapping("/default")
    public Result<String> setDefault(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        umsAddressService.setDefault(id);
        return Result.success("设置成功");
    }
}