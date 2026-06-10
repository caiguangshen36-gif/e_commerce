package com.e_commerce.module.product.repository;

import com.e_commerce.module.product.entity.PmsProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PmsProductEsRepository extends ElasticsearchRepository<PmsProductDocument, Long> {
    // 基础CRUD和简单查询由框架自动实现
}