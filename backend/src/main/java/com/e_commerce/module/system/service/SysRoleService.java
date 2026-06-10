package com.e_commerce.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.module.system.entity.SysRole;
import com.e_commerce.module.system.entity.SysUserRole;
import com.e_commerce.module.system.mapper.SysRoleMapper;
import com.e_commerce.module.system.mapper.SysUserRoleMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SysRoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis缓存配置
    private static final String ROLE_CACHE_KEY = "role:";
    private static final long ROLE_CACHE_EXPIRE = 60; // 缓存60分钟（角色很少变更）
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public void add(SysRole sysRole) {
        sysRole.setCreateTime(LocalDateTime.now());
        sysRoleMapper.insert(sysRole);
        clearRoleCache();
    }

    /**
     * 获取角色列表（带缓存）
     */
    public List<SysRole> list() {
        String cacheKey = ROLE_CACHE_KEY + "list";

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("角色列表缓存命中，key: {}", cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SysRole>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("角色列表缓存未命中，查询数据库，key: {}", cacheKey);
        List<SysRole> roleList = sysRoleMapper.selectList(null);

        try {
            String jsonData = objectMapper.writeValueAsString(roleList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, ROLE_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("角色列表已缓存，key: {}, 数据量: {}", cacheKey, roleList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return roleList;
    }

    public void update(SysRole role) {
        role.setCreateTime(LocalDateTime.now());
        sysRoleMapper.updateById(role);
        clearRoleCache();
    }

    public void delete(Long id) {
        sysRoleMapper.deleteById(id);
        clearRoleCache();
    }

    /**
     * 获取用户角色列表（带缓存）
     */
    public List<SysRole> listByUserId(Long userId) {
        String cacheKey = ROLE_CACHE_KEY + "user:" + userId;

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("用户角色缓存命中，userId: {}, key: {}", userId, cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SysRole>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("用户角色缓存未命中，查询数据库，userId: {}, key: {}", userId, cacheKey);
        List<SysRole> roleList = sysRoleMapper.listByUserId(userId);

        try {
            String jsonData = objectMapper.writeValueAsString(roleList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, ROLE_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("用户角色已缓存，userId: {}, key: {}, 数据量: {}", userId, cacheKey, roleList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return roleList;
    }

    public void assignUserRole(Long userId, Long roleId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
        clearRoleCache();
        clearUserRoleCache(userId);
    }

    public void removeUserRole(Long userId, Long roleId) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId));
        clearRoleCache();
        clearUserRoleCache(userId);
    }

    /**
     * 清除所有角色缓存
     */
    private void clearRoleCache() {
        try {
            Set<String> keys = stringRedisTemplate.keys(ROLE_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("已清除角色缓存，数量: {}", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除角色缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清除指定用户的角色缓存
     */
    private void clearUserRoleCache(Long userId) {
        try {
            String cacheKey = ROLE_CACHE_KEY + "user:" + userId;
            stringRedisTemplate.delete(cacheKey);
            log.info("已清除用户角色缓存，userId: {}", userId);
        } catch (Exception e) {
            log.warn("清除用户角色缓存失败: {}", e.getMessage());
        }
    }
}
