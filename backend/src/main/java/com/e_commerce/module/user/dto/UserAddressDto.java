package com.e_commerce.module.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserAddressDto {
    @JsonSerialize(using = ToStringSerializer.class) // 关键：雪花ID转字符串
    private Long id;
    private Long userId;
    @NotEmpty(message = "收货人不能为空")
    private String receiver;
    @NotEmpty(message = "手机号不能为空")
    private String phone;
    @NotEmpty(message = "省份不能为空")
    private String province;
    @NotEmpty(message = "城市不能为空")
    private String city;
    @NotEmpty(message = "区县不能为空")
    private String area;
    @NotEmpty(message = "详细地址不能为空")
    private String detail;
    private Integer isDefault;
}
