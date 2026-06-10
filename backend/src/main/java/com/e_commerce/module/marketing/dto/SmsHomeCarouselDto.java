package com.e_commerce.module.marketing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmsHomeCarouselDto {
    private Long id;
    @NotBlank(message = "图片url不能为空")
    private String pic;
    private String url;
    private Integer sort;
    private Integer status;
}
