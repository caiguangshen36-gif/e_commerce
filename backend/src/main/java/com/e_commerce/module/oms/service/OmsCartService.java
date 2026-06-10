package com.e_commerce.module.oms.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.oms.dto.OmsCartDto;
import com.e_commerce.module.oms.entity.OmsCart;
import com.e_commerce.module.oms.mapper.OmsCartMapper;
import com.e_commerce.module.oms.vo.CartVo;
import com.e_commerce.module.product.entity.PmsProduct;
import com.e_commerce.module.product.entity.PmsSku;
import com.e_commerce.module.product.mapper.PmsProductMapper;
import com.e_commerce.module.product.mapper.PmsSkuMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OmsCartService {

    @Autowired
    private OmsCartMapper omsCartMapper;

    @Autowired
    private PmsSkuMapper pmsSkuMapper;

    @Autowired
    private PmsProductMapper pmsProductMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CART_CACHE_KEY = "cart:list:user:";
    private static final long CART_CACHE_EXPIRE = 30;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public List<CartVo> getCartList(Long userId) {
        String cacheKey = CART_CACHE_KEY + userId;

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("购物车缓存命中，userId: {}, key: {}", userId, cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<CartVo>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("购物车缓存未命中，查询数据库，userId: {}, key: {}", userId, cacheKey);

        List<OmsCart> cartList = omsCartMapper.selectList(
                new LambdaQueryWrapper<OmsCart>().eq(OmsCart::getUserId, userId));
        if (CollUtil.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        List<CartVo> result = new ArrayList<>();
        for (OmsCart cart : cartList) {
            CartVo vo = new CartVo();
            BeanUtil.copyProperties(cart, vo);

            PmsSku sku = pmsSkuMapper.selectById(cart.getSkuId());
            if (sku != null) {
                vo.setPic(sku.getPic());
                vo.setPrice(sku.getPrice());

                PmsProduct product = pmsProductMapper.selectById(sku.getProductId());
                if (product != null) {
                    vo.setProductName(product.getProductName());
                }

                vo.setSkuSpecs(sku.getSkuCode());
            }
            result.add(vo);
        }

        try {
            String jsonData = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CART_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("购物车已缓存，userId: {}, key: {}, 数据量: {}", userId, cacheKey, result.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return result;
    }

    private void clearCartCache(Long userId) {
        try {
            String cacheKey = CART_CACHE_KEY + userId;
            stringRedisTemplate.delete(cacheKey);
            log.info("已清除购物车缓存，userId: {}", userId);
        } catch (Exception e) {
            log.warn("清除购物车缓存失败: {}", e.getMessage());
        }
    }

    public void add(Long userId, OmsCartDto cartDto) {
        Long skuId = cartDto.getSkuId();
        Integer quantity = cartDto.getQuantity();

        OmsCart exist = omsCartMapper.selectOne(
                new LambdaQueryWrapper<OmsCart>()
                        .eq(OmsCart::getUserId, userId)
                        .eq(OmsCart::getSkuId, skuId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + quantity);
            omsCartMapper.update(null,
                    new LambdaUpdateWrapper<OmsCart>()
                            .eq(OmsCart::getId, exist.getId())
                            .set(OmsCart::getQuantity, exist.getQuantity()));
        } else {
            OmsCart cart = new OmsCart();
            cart.setUserId(userId);
            cart.setProductId(cartDto.getProductId());
            cart.setSkuId(skuId);
            cart.setQuantity(quantity);
            omsCartMapper.insert(cart);
        }

        clearCartCache(userId);
    }

    public boolean updateQuantity(Long userId, Long id, Integer quantity) {
        OmsCart cart = omsCartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return false;
        }

        omsCartMapper.update(null,
                new LambdaUpdateWrapper<OmsCart>()
                        .eq(OmsCart::getId, id)
                        .set(OmsCart::getQuantity, quantity));

        clearCartCache(userId);
        return true;
    }

    public boolean deleteById(Long userId, Long id) {
        OmsCart cart = omsCartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return false;
        }
        omsCartMapper.deleteById(id);

        clearCartCache(userId);
        return true;
    }

    public void clearUserCart(Long userId) {
        omsCartMapper.delete(new LambdaQueryWrapper<OmsCart>().eq(OmsCart::getUserId, userId));

        clearCartCache(userId);
    }

    public Map<String, Object> settleCart(Long userId, List<Long> cartIds) {
        Map<String, Object> result = new HashMap<>();
        List<OmsCart> cartList = omsCartMapper.selectList(
                new LambdaQueryWrapper<OmsCart>()
                        .eq(OmsCart::getUserId, userId)
                        .in(OmsCart::getId, cartIds));
        result.put("cartList", cartList);
        return result;
    }
}
