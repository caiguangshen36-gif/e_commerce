package com.e_commerce.module.product.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.e_commerce.module.product.entity.PmsProduct;
import com.e_commerce.module.product.entity.PmsProductDocument;
import com.e_commerce.module.product.entity.PmsSkuAttr;
import com.e_commerce.module.product.repository.PmsProductEsRepository;
import com.e_commerce.module.product.mapper.PmsProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PmsProductEsService{

    private final PmsProductMapper productMapper;
    private final PmsProductEsRepository esRepository;

    public void syncAllToEs() {
        List<PmsProduct> products = productMapper.selectList(null);
        List<PmsProductDocument> docs = products.stream()
                .map(this::buildDocument)
                .collect(Collectors.toList());
        esRepository.saveAll(docs);
        log.info("全量同步ES完成，共同步{}条", docs.size());
    }

    public void syncOneToEs(Long productId) {
        PmsProduct product = productMapper.selectById(productId);
        if (product != null) {
            esRepository.save(buildDocument(product));
        }
    }

    public void deleteFromEs(Long productId) {
        esRepository.deleteById(productId);
    }

    private PmsProductDocument buildDocument(PmsProduct product) {
        PmsProductDocument doc = new PmsProductDocument();

        // PmsProduct 有的字段直接赋值
        doc.setId(product.getId());
        doc.setProductName(product.getProductName());
        doc.setCategoryId(product.getCategoryId());
        doc.setPic(product.getPic());
        doc.setStatus(product.getStatus());
        doc.setCreateTime(product.getCreateTime());

        // categoryName 需要关联查询
        String categoryName = productMapper.selectCategoryNameById(product.getCategoryId());
        doc.setCategoryName(categoryName);

        // 查SKU属性冗余到ES（用于搜索匹配）
        List<PmsSkuAttr> attrs = productMapper.selectAttrsByProductId(product.getId());
        if (!CollectionUtils.isEmpty(attrs)) {
            doc.setAttrNames(attrs.stream()
                    .map(PmsSkuAttr::getAttrName)
                    .distinct()
                    .collect(Collectors.toList()));
            doc.setAttrValues(attrs.stream()
                    .map(PmsSkuAttr::getAttrValue)
                    .distinct()
                    .collect(Collectors.toList()));
        } else {
            doc.setAttrNames(Collections.emptyList());
            doc.setAttrValues(Collections.emptyList());
        }

        // detailHtml 不同步到ES（内容太大，无需搜索）
        return doc;
    }
}