package com.e_commerce.module.system.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.module.system.dto.IdDto;
import com.e_commerce.module.system.dto.RoleAssignDto;
import com.e_commerce.module.system.dto.UserIdDto;
import com.e_commerce.module.system.entity.SysRole;
import com.e_commerce.module.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/role")
@Slf4j
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated SysRole sysRole) {
        sysRoleService.add(sysRole);
        log.info("角色添加成功：{}", sysRole.getRoleName());
        return Result.success("添加成功");
    }

    @PostMapping("/list")
    public Result<List<SysRole>> list() {
        List<SysRole> roles = sysRoleService.list();
        log.info("获取角色列表成功，共{}个角色", roles.size());
        return Result.success(roles);
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody @Validated SysRole sysRole) {
        sysRoleService.update(sysRole);
        log.info("角色更新成功：{}", sysRole.getRoleName());
        return Result.success("修改成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestBody IdDto dto) {
        sysRoleService.delete(dto.getId());
        log.info("角色删除成功：id={}", dto.getId());
        return Result.success("删除成功");
    }

    @PostMapping("/user/roleList")
    public Result<List<SysRole>> getRolesByUserId(@RequestBody UserIdDto dto) {
        List<SysRole> roles = sysRoleService.listByUserId(dto.getUserId());
        return Result.success(roles);
    }

    @PostMapping("/user/assign")
    public Result<String> assignRole(@RequestBody RoleAssignDto dto) {
        sysRoleService.assignUserRole(dto.getUserId(), dto.getRoleId());
        log.info("用户角色分配成功：userId={}, roleId={}", dto.getUserId(), dto.getRoleId());
        return Result.success("分配成功");
    }

    @PostMapping("/user/remove")
    public Result<String> removeUserRole(@RequestBody RoleAssignDto dto) {
        sysRoleService.removeUserRole(dto.getUserId(), dto.getRoleId());
        log.info("用户角色移除成功：userId={}, roleId={}", dto.getUserId(), dto.getRoleId());
        return Result.success("移除成功");
    }
}