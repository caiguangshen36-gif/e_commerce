package com.e_commerce.module.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
public class PmsSkuAttrVo {
    private Long id;
    private Long skuId;
    private Long productId;
    private Long attrId;
    private Long attrValueId;

    // 搜索字段：属性名（颜色、尺寸等）
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String attrName;

    // 搜索字段：属性值（红色、XL等）
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String attrValue;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
