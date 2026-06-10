package com.e_commerce.module.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PmsBrowseVo {
    private Long browseId;
    // 商品信息
    private Long productId;
    private String productName;
    private String pic;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<PmsSkuVo> skuList;
}