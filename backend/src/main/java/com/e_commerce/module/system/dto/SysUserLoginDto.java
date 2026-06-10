package com.e_commerce.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysUserLoginDto {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
    @NotBlank(message = "未输入验证码")
    private String captchaCode;

}
