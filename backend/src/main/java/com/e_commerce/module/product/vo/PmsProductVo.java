package com.e_commerce.module.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
// 核心：指定ES索引名（必须加）
@Document(indexName = "pms_product")
public class PmsProductVo {

    // ES 文档ID（必须加 @Id）
    @Id
    private Long id;

    // 商品名称：需要分词搜索，必须加 ik 分词器
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productName;

    // 分类ID：精确匹配，不分词
    @Field(type = FieldType.Long)
    private Long categoryId;

    // 分类名称：可搜索
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String categoryName;

    // 图片：不需要搜索，Keyword 即可
    @Field(type = FieldType.Keyword)
    private String pic;

    //
    private BigDecimal originalPrice;

    // 商品简短描述
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String shortDesc;

    // 状态：精确匹配
    @Field(type = FieldType.Integer)
    private Integer status;

    // 是否热门：0-否 1-是
    @Field(type = FieldType.Integer)
    private Integer isHot;

    // 热门排序：数字越小越靠前
    @Field(type = FieldType.Integer)
    private Integer hotSort;

    // 创建时间
    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // sku列表：嵌套对象（ES 自动识别）
    @Field(type = FieldType.Nested)
    private List<PmsSkuVo> skuList;

    // 商品详情：大文本，分词搜索
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String detailHtml;
}
