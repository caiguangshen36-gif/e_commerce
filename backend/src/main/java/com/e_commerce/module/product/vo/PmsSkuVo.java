package com.e_commerce.module.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PmsSkuVo {
    private Long id;
    private Long productId;

    @Field(type = FieldType.Keyword)
    private String skuCode;

    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;

    @Field(type = FieldType.Keyword)
    private String pic;

    private BigDecimal weight;
    private BigDecimal volume;
    private Integer status;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 嵌套属性：规格属性列表
    @Field(type = FieldType.Nested)
    private List<PmsSkuAttrVo> skuAttrList;
}
