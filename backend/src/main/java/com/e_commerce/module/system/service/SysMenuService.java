package com.e_commerce.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.module.system.entity.SysMenu;
import com.e_commerce.module.system.entity.SysRoleMenu;
import com.e_commerce.module.system.mapper.SysMenuMapper;
import com.e_commerce.module.system.mapper.SysRoleMenuMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysMenuService {
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis缓存配置
    private static final String MENU_CACHE_KEY = "menu:";
    private static final long MENU_CACHE_EXPIRE = 60; // 缓存60分钟（菜单很少变更）
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public void add(SysMenu sysMenu) {
        sysMenuMapper.insert(sysMenu);
        clearMenuCache();
    }

    public void update(SysMenu sysMenu) {
        sysMenuMapper.updateById(sysMenu);
        clearMenuCache();
    }

    public void delete(Long id) {
        sysMenuMapper.deleteById(id);
        clearMenuCache();
    }

    /**
     * 获取菜单树（带缓存）
     */
    public List<SysMenu> listWithTree() {
        String cacheKey = MENU_CACHE_KEY + "tree";

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("菜单树缓存命中，key: {}", cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SysMenu>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("菜单树缓存未命中，查询数据库，key: {}", cacheKey);
        List<SysMenu> menuList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));

        try {
            String jsonData = objectMapper.writeValueAsString(menuList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, MENU_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("菜单树已缓存，key: {}, 数据量: {}", cacheKey, menuList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return menuList;
    }

    public void assignMenusToRole(Long roleId, List<Long> menuIds) {
        // 1. 先删除旧的权限
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

        // 2. 如果有新的权限，再批量插入
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }

        // 清除相关缓存
        clearMenuCache();
        clearRoleMenuCache(roleId);
    }

    /**
     * 获取角色的菜单ID列表（带缓存）
     */
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        String cacheKey = MENU_CACHE_KEY + "role:" + roleId;

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("角色菜单缓存命中，roleId: {}, key: {}", roleId, cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<Long>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("角色菜单缓存未命中，查询数据库，roleId: {}, key: {}", roleId, cacheKey);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());

        try {
            String jsonData = objectMapper.writeValueAsString(menuIds);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, MENU_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("角色菜单已缓存，roleId: {}, key: {}, 数据量: {}", roleId, cacheKey, menuIds.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return menuIds;
    }

    /**
     * 获取当前用户菜单（带缓存）
     */
    public List<SysMenu> getCurrentUserMenus(Long userId) {
        String cacheKey = MENU_CACHE_KEY + "user:" + userId;

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("用户菜单缓存命中，userId: {}, key: {}", userId, cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SysMenu>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("用户菜单缓存未命中，查询数据库，userId: {}, key: {}", userId, cacheKey);
        List<SysMenu> menuList = sysMenuMapper.getCurrentUserMenus(userId);

        try {
            String jsonData = objectMapper.writeValueAsString(menuList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, MENU_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("用户菜单已缓存，userId: {}, key: {}, 数据量: {}", userId, cacheKey, menuList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return menuList;
    }

    /**
     * 获取用户菜单（带缓存）
     */
    public List<SysMenu> getMenusByUserId(Long userId) {
        String cacheKey = MENU_CACHE_KEY + "user:list:" + userId;

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("用户菜单列表缓存命中，userId: {}, key: {}", userId, cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SysMenu>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("用户菜单列表缓存未命中，查询数据库，userId: {}, key: {}", userId, cacheKey);
        List<SysMenu> menuList = sysMenuMapper.getMenusByUserId(userId);

        try {
            String jsonData = objectMapper.writeValueAsString(menuList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, MENU_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("用户菜单列表已缓存，userId: {}, key: {}, 数据量: {}", userId, cacheKey, menuList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return menuList;
    }

    /**
     * 清除所有菜单缓存
     */
    private void clearMenuCache() {
        try {
            Set<String> keys = stringRedisTemplate.keys(MENU_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("已清除菜单缓存，数量: {}", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除菜单缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清除指定角色的菜单缓存
     */
    private void clearRoleMenuCache(Long roleId) {
        try {
            String cacheKey = MENU_CACHE_KEY + "role:" + roleId;
            stringRedisTemplate.delete(cacheKey);
            log.info("已清除角色菜单缓存，roleId: {}", roleId);
        } catch (Exception e) {
            log.warn("清除角色菜单缓存失败: {}", e.getMessage());
        }
    }
}
