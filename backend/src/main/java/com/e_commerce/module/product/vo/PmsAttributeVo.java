package com.e_commerce.module.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PmsAttributeVo {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String attrName;
    private Integer sort;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<PmsAttributeValueVo> valueList;
}
