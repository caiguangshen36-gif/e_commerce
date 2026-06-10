package com.e_commerce.common.vo;

import lombok.Data;

@Data
public class CaptchaVO {
    private String captchaId;   // 唯一ID（UUID）
    private String captchaImg;  // Base64图片
}