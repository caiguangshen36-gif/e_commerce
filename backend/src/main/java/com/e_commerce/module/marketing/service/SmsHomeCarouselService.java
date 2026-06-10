package com.e_commerce.module.marketing.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.marketing.vo.SmsHomeCarouselVo;
import com.e_commerce.module.marketing.dto.SmsHomeCarouselDto;
import com.e_commerce.module.marketing.entity.SmsHomeCarousel;
import com.e_commerce.module.marketing.mapper.SmsHomeCarouselMapper;
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
public class SmsHomeCarouselService {
    @Autowired
    private SmsHomeCarouselMapper carouselMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis缓存配置
    private static final String CAROUSEL_CACHE_KEY = "carousel:list:";
    private static final long CAROUSEL_CACHE_EXPIRE = 30; // 缓存30分钟
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public void addCarousel(SmsHomeCarouselDto carousel) {
        SmsHomeCarousel entity = new SmsHomeCarousel();
        BeanUtil.copyProperties(carousel, entity);
        carouselMapper.insert(entity);
        clearCarouselCache();
    }

    public List<SmsHomeCarouselVo> getCarouselList() {
        String cacheKey = CAROUSEL_CACHE_KEY + "all";

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("轮播图缓存命中，key: {}", cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SmsHomeCarouselVo>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("轮播图缓存未命中，查询数据库，key: {}", cacheKey);
        List<SmsHomeCarousel> entities = carouselMapper.selectList(
                new LambdaQueryWrapper<SmsHomeCarousel>().orderByAsc(SmsHomeCarousel::getSort));
        List<SmsHomeCarouselVo> carouselList = entities.stream()
                .map(this::entityToVo)
                .collect(Collectors.toList());

        try {
            String jsonData = objectMapper.writeValueAsString(carouselList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CAROUSEL_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("轮播图已缓存，key: {}, 数据量: {}", cacheKey, carouselList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return carouselList;
    }

    public SmsHomeCarousel getCarouselById(Long id) {
        return carouselMapper.selectById(id);
    }

    public void updateCarousel(SmsHomeCarouselDto carousel) {
        SmsHomeCarousel entity = new SmsHomeCarousel();
        BeanUtil.copyProperties(carousel, entity);
        carouselMapper.updateById(entity);
        clearCarouselCache();
    }

    public void updateStatusCarousel(Long id) {
        carouselMapper.update(null, new LambdaUpdateWrapper<SmsHomeCarousel>()
                .eq(SmsHomeCarousel::getId, id)
                .setSql("status = 1 - status"));
        clearCarouselCache();
    }

    public void deleteCarousel(Long id) {
        carouselMapper.deleteById(id);
        clearCarouselCache();
    }

    public List<SmsHomeCarouselVo> getEnabledCarouselList() {
        String cacheKey = CAROUSEL_CACHE_KEY + "enabled";

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("启用轮播图缓存命中，key: {}", cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<SmsHomeCarouselVo>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("启用轮播图缓存未命中，查询数据库，key: {}", cacheKey);
        List<SmsHomeCarousel> entities = carouselMapper.selectList(
                new LambdaQueryWrapper<SmsHomeCarousel>()
                        .eq(SmsHomeCarousel::getStatus, 1)
                        .orderByAsc(SmsHomeCarousel::getSort));
        List<SmsHomeCarouselVo> carouselList = entities.stream()
                .map(this::entityToVo)
                .collect(Collectors.toList());

        try {
            String jsonData = objectMapper.writeValueAsString(carouselList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CAROUSEL_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("启用轮播图已缓存，key: {}, 数据量: {}", cacheKey, carouselList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return carouselList;
    }

    /**
     * 清除轮播图缓存
     */
    private void clearCarouselCache() {
        try {
            Set<String> keys = stringRedisTemplate.keys(CAROUSEL_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("已清除轮播图缓存，数量: {}", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除轮播图缓存失败: {}", e.getMessage());
        }
    }

    private SmsHomeCarouselVo entityToVo(SmsHomeCarousel entity) {
        SmsHomeCarouselVo vo = new SmsHomeCarouselVo();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }
}
