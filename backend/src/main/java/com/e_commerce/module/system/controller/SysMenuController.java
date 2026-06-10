package com.e_commerce.module.system.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.system.dto.RoleIdDto;
import com.e_commerce.module.system.dto.RoleMenuAssignDto;
import com.e_commerce.module.system.entity.SysMenu;
import com.e_commerce.module.system.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/menu")
@Slf4j
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    @PostMapping("/list")
    public Result<List<SysMenu>> list() {
        List<SysMenu> menus = sysMenuService.listWithTree();
        log.info("获取菜单列表成功，共{}个菜单", menus.size());
        return Result.success(menus);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated SysMenu sysMenu) {
        sysMenuService.add(sysMenu);
        log.info("菜单添加成功：{}", sysMenu.getMenuName());
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody @Validated SysMenu sysMenu) {
        sysMenuService.update(sysMenu);
        log.info("菜单更新成功：{}", sysMenu.getMenuName());
        return Result.success("修改成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Long id) {
        if (id == null || id <= 0) {
            return Result.error("菜单ID不能为空且必须大于0");
        }
        sysMenuService.delete(id);
        log.info("菜单删除成功：ID={}", id);
        return Result.success("删除成功");
    }

    // ====================== 2. 角色-菜单权限分配 ======================
    @PostMapping("/role/assign")
    public Result<String> assignMenusToRole(@RequestBody RoleMenuAssignDto dto) {
        Long roleId = dto.getRoleId();
        List<Long> menuIds = dto.getMenuIds();

        if (roleId == null || roleId <= 0) {
            return Result.error("角色ID不能为空且必须大于0");
        }
        if (menuIds == null || menuIds.isEmpty()) {
            return Result.error("菜单ID列表不能为空");
        }
        sysMenuService.assignMenusToRole(roleId, menuIds);
        log.info("角色菜单分配成功：角色ID={}, 菜单数={}", roleId, menuIds.size());
        return Result.success("分配成功");
    }

    /**
     * 获取角色已分配的菜单ID
     */
    @PostMapping("/role/menus")
    public Result<List<Long>> getRoleMenuIds(@RequestBody RoleIdDto dto) {
        Long roleId = dto.getRoleId();
        if (roleId == null || roleId <= 0) {
            return Result.error("角色ID不能为空且必须大于0");
        }
        List<Long> menuIds = sysMenuService.getMenuIdsByRoleId(roleId);
        log.info("获取角色菜单ID成功：角色ID={}", roleId);
        return Result.success(menuIds);
    }

    // ====================== 3. 用户菜单查询 ======================

    /**
     * 获取当前登录用户菜单（GET → POST）
     */
    @PostMapping("/current")
    public Result<List<SysMenu>> getCurrentUserMenus() {
        Long userId = ThreadLocalUtil.getUserId();
        log.info("当前登录用户ID：{}", userId);

        // 传给 Service
        List<SysMenu> menus = sysMenuService.getCurrentUserMenus(userId);
        log.info("获取当前用户菜单成功，共{}个菜单", menus.size());
        return Result.success(menus);
    }

    /**
     * 根据用户ID获取菜单
     */
    @PostMapping("/user/list")
    public Result<List<SysMenu>> getMenusByUserId(@RequestBody Long userId) {
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须大于0");
        }
        List<SysMenu> menus = sysMenuService.getMenusByUserId(userId);
        log.info("获取用户菜单列表成功，用户ID={}，共{}个菜单", userId, menus.size());
        return Result.success(menus);
    }
}