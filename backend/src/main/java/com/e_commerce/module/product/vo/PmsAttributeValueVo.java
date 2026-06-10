package com.e_commerce.module.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmsAttributeValueVo {
    private Long id;
    private Long attrId;
    private String attrName;
    private String attrValue;
    private Integer sort;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
