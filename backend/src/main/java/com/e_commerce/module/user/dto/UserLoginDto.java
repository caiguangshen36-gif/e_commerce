package com.e_commerce.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
    @NotBlank(message = "未输入验证码")
    private String captchaCode;
}
