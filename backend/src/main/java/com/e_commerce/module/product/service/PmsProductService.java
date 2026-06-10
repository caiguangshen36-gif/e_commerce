package com.e_commerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.product.dto.PmsProductDto;
import com.e_commerce.module.product.dto.PmsSkuAttrDto;
import com.e_commerce.module.product.dto.PmsSkuDto;
import com.e_commerce.module.product.entity.*;
import com.e_commerce.module.product.mapper.*;
import com.e_commerce.module.product.vo.PmsProductVo;
import com.e_commerce.module.product.vo.PmsSkuAttrVo;
import com.e_commerce.module.product.vo.PmsSkuVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Service
@Slf4j
public class PmsProductService {

    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsSkuMapper skuMapper;
    @Autowired
    private PmsSkuAttrMapper skuAttrMapper;
    @Autowired
    private PmsCategoryMapper categoryMapper;
    @Autowired
    private PmsProductCollectMapper collectMapper;
    @Autowired
    private PmsProductBrowseMapper browseMapper;
    @Autowired
    private ElasticsearchOperations esOperations;
    @Autowired
    private PmsProductEsService pmsProductEsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String PRODUCT_LIST_CACHE_KEY = "product:list:";
    private static final long PRODUCT_CACHE_EXPIRE = 30;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 添加商品
     */
    @Transactional
    public void addProduct(PmsProductDto productDto) {
        PmsProduct product = new PmsProduct();
        BeanUtils.copyProperties(productDto, product);
        productMapper.insert(product);

        if (productDto.getSkuList() != null && !productDto.getSkuList().isEmpty()) {
            for (PmsSkuDto skuDto : productDto.getSkuList()) {
                PmsSku sku = new PmsSku();
                BeanUtils.copyProperties(skuDto, sku);
                sku.setProductId(product.getId());
                skuMapper.insert(sku);

                if (skuDto.getSkuAttrList() != null && !skuDto.getSkuAttrList().isEmpty()) {
                    for (PmsSkuAttrDto skuAttrDto : skuDto.getSkuAttrList()) {
                        PmsSkuAttr skuAttr = new PmsSkuAttr();
                        skuAttr.setSkuId(sku.getId());
                        skuAttr.setProductId(product.getId());
                        skuAttr.setAttrId(skuAttrDto.getAttrId());
                        skuAttr.setAttrValueId(skuAttrDto.getAttrValueId());
                        skuAttr.setAttrName(skuAttrDto.getAttrName());
                        skuAttr.setAttrValue(skuAttrDto.getAttrValue());
                        skuAttrMapper.insert(skuAttr);
                    }
                }
            }
        }

        pmsProductEsService.syncOneToEs(product.getId());
        clearProductListCache();
    }

    /**
     * 更新商品信息
     */
    @Transactional
    public void updateProduct(PmsProductDto productDto) {
        PmsProduct product = new PmsProduct();
        BeanUtils.copyProperties(productDto, product);
        productMapper.updateById(product);

        // 删除原有SKU和SKU属性关联
        LambdaQueryWrapper<PmsSkuAttr> deleteAttrWrapper = new LambdaQueryWrapper<>();
        deleteAttrWrapper.eq(PmsSkuAttr::getProductId, product.getId());
        skuAttrMapper.delete(deleteAttrWrapper);

        LambdaQueryWrapper<PmsSku> deleteSkuWrapper = new LambdaQueryWrapper<>();
        deleteSkuWrapper.eq(PmsSku::getProductId, product.getId());
        skuMapper.delete(deleteSkuWrapper);

        if (productDto.getSkuList() != null && !productDto.getSkuList().isEmpty()) {
            for (PmsSkuDto skuDto : productDto.getSkuList()) {
                PmsSku sku = new PmsSku();
                BeanUtils.copyProperties(skuDto, sku);
                sku.setProductId(product.getId());
                skuMapper.insert(sku);

                if (skuDto.getSkuAttrList() != null && !skuDto.getSkuAttrList().isEmpty()) {
                    for (PmsSkuAttrDto skuAttrDto : skuDto.getSkuAttrList()) {
                        PmsSkuAttr skuAttr = new PmsSkuAttr();
                        skuAttr.setSkuId(sku.getId());
                        skuAttr.setProductId(product.getId());
                        skuAttr.setAttrId(skuAttrDto.getAttrId());
                        skuAttr.setAttrValueId(skuAttrDto.getAttrValueId());
                        skuAttr.setAttrName(skuAttrDto.getAttrName());
                        skuAttr.setAttrValue(skuAttrDto.getAttrValue());
                        skuAttrMapper.insert(skuAttr);
                    }
                }
            }
        }

        pmsProductEsService.syncOneToEs(product.getId());
        clearProductListCache();
    }

    /**
     * 更新商品状态
     */
    public void updateProductStatus(Long id, Integer status) {
        PmsProduct product = new PmsProduct();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);

        pmsProductEsService.syncOneToEs(id);
        clearProductListCache();
    }

    /**
     * 更新商品热门状态
     */
    public void updateHotStatus(Long id, Integer isHot, Integer hotSort) {
        LambdaUpdateWrapper<PmsProduct> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PmsProduct::getId, id)
               .set(PmsProduct::getIsHot, isHot)
               .set(PmsProduct::getHotSort, hotSort);
        productMapper.update(null, wrapper);

        pmsProductEsService.syncOneToEs(id);
        clearProductListCache();
    }

    /**
     * 获取热门商品列表（is_hot=1）
     */
    public List<PmsProductVo> getHotProductList() {
        String cacheKey = PRODUCT_LIST_CACHE_KEY + "hot";

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("热门商品缓存命中，key: {}", cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<PmsProductVo>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("热门商品缓存未命中，查询数据库，key: {}", cacheKey);
        List<PmsProductVo> productList = productMapper.selectHotProductList();

        try {
            String jsonData = objectMapper.writeValueAsString(productList);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, PRODUCT_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("热门商品已缓存，key: {}, 数据量: {}", cacheKey, productList.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return productList;
    }

    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long id) {
        LambdaQueryWrapper<PmsSkuAttr> deleteAttrWrapper = new LambdaQueryWrapper<>();
        deleteAttrWrapper.eq(PmsSkuAttr::getProductId, id);
        skuAttrMapper.delete(deleteAttrWrapper);

        LambdaQueryWrapper<PmsSku> deleteSkuWrapper = new LambdaQueryWrapper<>();
        deleteSkuWrapper.eq(PmsSku::getProductId, id);
        skuMapper.delete(deleteSkuWrapper);

        productMapper.deleteById(id);

        pmsProductEsService.deleteFromEs(id);
        clearProductListCache();
    }

    /**
     * 根据ID获取商品详情（走MySQL，不走ES）
     */
    public PmsProductVo getProductById(Long id) {
        return productMapper.selectProductById(id);
    }

    /**
     * 获取商品列表（带Redis缓存，支持分类筛选和分页）
     */
    public PageVo<PmsProductVo> getProductList(Long pageNum, Long pageSize, String keyword, Integer status, Long categoryId) {
        // 缓存key不包含分页参数，缓存的是完整列表
        String cacheKey = generateCacheKey(keyword, status, categoryId);
        List<PmsProductVo> fullList;

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("商品列表缓存命中，key: {}", cacheKey);
                fullList = objectMapper.readValue(cachedData, new TypeReference<List<PmsProductVo>>() {});
            } else {
                log.info("商品列表缓存未命中，查询数据库，key: {}", cacheKey);
                fullList = productMapper.selectProductList(keyword, status, categoryId);
                try {
                    String jsonData = objectMapper.writeValueAsString(fullList);
                    stringRedisTemplate.opsForValue().set(cacheKey, jsonData, PRODUCT_CACHE_EXPIRE, TimeUnit.MINUTES);
                    log.info("商品列表已缓存，key: {}, 数据量: {}", cacheKey, fullList.size());
                } catch (JsonProcessingException e) {
                    log.warn("Redis缓存写入失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
            fullList = productMapper.selectProductList(keyword, status, categoryId);
        }

        // 手动分页
        long total = fullList.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize.intValue(), fullList.size());
        if (fromIndex >= fullList.size()) {
            fromIndex = 0;
            toIndex = 0;
        }
        List<PmsProductVo> pageList = fullList.subList(fromIndex, toIndex);

        PageVo<PmsProductVo> pageVo = new PageVo<>();
        pageVo.setList(pageList);
        pageVo.setTotal(total);
        return pageVo;
    }

    private String generateCacheKey(String keyword, Integer status, Long categoryId) {
        if ((keyword == null || keyword.trim().isEmpty()) && status == null && categoryId == null) {
            return PRODUCT_LIST_CACHE_KEY + "all";
        }
        String key = "k:" + (keyword != null ? keyword.trim().toLowerCase() : "null");
        String statusKey = "s:" + (status != null ? status.toString() : "null");
        String categoryKey = "c:" + (categoryId != null ? categoryId.toString() : "null");
        return PRODUCT_LIST_CACHE_KEY + key + ":" + statusKey + ":" + categoryKey;
    }

    /**
     * 清除商品列表缓存
     */
    public void clearProductListCache() {
        try {
            Set<String> keys = stringRedisTemplate.keys(PRODUCT_LIST_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("已清除商品列表缓存，数量: {}", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除商品列表缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 根据分类ID获取商品列表（走MySQL）
     */
    public List<PmsProductVo> getProductsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getCategoryId, categoryId);
        List<PmsProduct> productList = productMapper.selectList(wrapper);
        return productList.stream()
                .map(this::buildProductVo)
                .collect(Collectors.toList());
    }

    /**
     * 关键词搜索（走ES，再回查MySQL）
     */
    public List<PmsProductVo> searchProduct(String keyword) {
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s.match(m -> m
                                        .field("productName").query(keyword).boost(3.0f)))
                                .should(s -> s.match(m -> m
                                        .field("attrValues").query(keyword).boost(2.0f)))
                                .should(s -> s.match(m -> m
                                        .field("attrNames").query(keyword).boost(1.0f)))
                                .minimumShouldMatch("1")
                                .filter(f -> f.term(t -> t.field("status").value(1)))
                        )
                )
                .withPageable(PageRequest.of(0, 20))
                .build();

        SearchHits<PmsProductDocument> hits = esOperations.search(searchQuery, PmsProductDocument.class);
        List<Long> productIds = hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PmsProductVo> voList = productMapper.selectProductVoByIds(productIds);
        Map<Long, PmsProductVo> voMap = voList.stream()
                .collect(Collectors.toMap(PmsProductVo::getId, v -> v));

        return productIds.stream()
                .map(voMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取混合推荐商品列表（收藏80% + 浏览20%）
     */
    public List<PmsProductVo> getMixedRecommendList(Long userId) {
        String cacheKey = PRODUCT_LIST_CACHE_KEY + "recommend:user:" + (userId != null ? userId : "anonymous");

        try {
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("混合推荐缓存命中，key: {}", cacheKey);
                return objectMapper.readValue(cachedData, new TypeReference<List<PmsProductVo>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，继续查询数据库: {}", e.getMessage());
        }

        log.info("混合推荐缓存未命中，查询数据库，key: {}", cacheKey);

        Map<Long, PmsProductVo> productMap = new LinkedHashMap<>();
        int targetCount = 10;
        int expectedCollect = (int) (targetCount * 0.8);
        int expectedBrowse = targetCount - expectedCollect;

        // 获取收藏商品ID列表
        List<Long> collectProductIds = getTopProductIdsFromCollect(userId, 10);
        List<PmsProductVo> collectList = new ArrayList<>();
        for (Long productId : collectProductIds) {
            PmsProductVo productVo = productMapper.selectProductById(productId);
            if (productVo != null) {
                collectList.add(productVo);
            }
        }

        // 获取浏览商品ID列表
        List<Long> browseProductIds = getTopProductIdsFromBrowse(userId, 10);
        List<PmsProductVo> browseList = new ArrayList<>();
        for (Long productId : browseProductIds) {
            PmsProductVo productVo = productMapper.selectProductById(productId);
            if (productVo != null) {
                browseList.add(productVo);
            }
        }

        int collectCount = 0;
        for (PmsProductVo product : collectList) {
            if (collectCount >= expectedCollect) break;
            if (!productMap.containsKey(product.getId())) {
                productMap.put(product.getId(), product);
                collectCount++;
            }
        }

        int browseCount = 0;
        for (PmsProductVo product : browseList) {
            if (browseCount >= expectedBrowse) break;
            if (!productMap.containsKey(product.getId())) {
                productMap.put(product.getId(), product);
                browseCount++;
            }
        }

        if (productMap.size() < targetCount) {
            for (PmsProductVo product : collectList) {
                if (productMap.size() >= targetCount) break;
                if (!productMap.containsKey(product.getId())) {
                    productMap.put(product.getId(), product);
                }
            }
        }

        if (productMap.size() < targetCount) {
            for (PmsProductVo product : browseList) {
                if (productMap.size() >= targetCount) break;
                if (!productMap.containsKey(product.getId())) {
                    productMap.put(product.getId(), product);
                }
            }
        }

        List<PmsProductVo> result = new ArrayList<>(productMap.values());

        try {
            String jsonData = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, PRODUCT_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.info("混合推荐已缓存，key: {}, 数据量: {}", cacheKey, result.size());
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 清除用户推荐缓存
     */
    public void clearRecommendCache(Long userId) {
        try {
            String cacheKey = PRODUCT_LIST_CACHE_KEY + "recommend:user:" + (userId != null ? userId : "anonymous");
            stringRedisTemplate.delete(cacheKey);
            log.info("已清除用户推荐缓存，userId: {}", userId);
        } catch (Exception e) {
            log.warn("清除用户推荐缓存失败: {}", e.getMessage());
        }
    }

    private List<Long> getTopProductIdsFromCollect(Long userId, int limit) {
        LambdaQueryWrapper<PmsProductCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PmsProductCollect::getProductId)
               .eq(PmsProductCollect::getUserId, userId)
               .orderByDesc(PmsProductCollect::getCreateTime)
               .last("LIMIT " + limit);
        return collectMapper.selectList(wrapper).stream()
                .map(PmsProductCollect::getProductId)
                .collect(Collectors.toList());
    }

    private List<Long> getTopProductIdsFromBrowse(Long userId, int limit) {
        LambdaQueryWrapper<PmsProductBrowse> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PmsProductBrowse::getProductId)
               .eq(PmsProductBrowse::getUserId, userId)
               .orderByDesc(PmsProductBrowse::getCreateTime)
               .last("LIMIT " + limit);
        return browseMapper.selectList(wrapper).stream()
                .map(PmsProductBrowse::getProductId)
                .collect(Collectors.toList());
    }

    private PmsProductVo buildProductVo(PmsProduct product) {
        PmsProductVo productVo = new PmsProductVo();
        BeanUtils.copyProperties(product, productVo);

        PmsCategory category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            productVo.setCategoryName(category.getCategoryName());
        }

        LambdaQueryWrapper<PmsSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(PmsSku::getProductId, product.getId());
        List<PmsSku> skuList = skuMapper.selectList(skuWrapper);

        List<PmsSkuVo> skuVoList = skuList.stream().map(sku -> {
            PmsSkuVo skuVo = new PmsSkuVo();
            BeanUtils.copyProperties(sku, skuVo);

            LambdaQueryWrapper<PmsSkuAttr> attrWrapper = new LambdaQueryWrapper<>();
            attrWrapper.eq(PmsSkuAttr::getSkuId, sku.getId());
            List<PmsSkuAttr> skuAttrList = skuAttrMapper.selectList(attrWrapper);

            List<PmsSkuAttrVo> skuAttrVoList = skuAttrList.stream().map(attr -> {
                PmsSkuAttrVo attrVo = new PmsSkuAttrVo();
                BeanUtils.copyProperties(attr, attrVo);
                return attrVo;
            }).collect(Collectors.toList());
            skuVo.setSkuAttrList(skuAttrVoList);
            return skuVo;
        }).collect(Collectors.toList());
        productVo.setSkuList(skuVoList);

        return productVo;
    }
}
