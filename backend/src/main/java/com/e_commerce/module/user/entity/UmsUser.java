package com.e_commerce.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ums_user")
public class UmsUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String avatar;
    private Integer status;
    private BigDecimal balance;
    private Integer passwordVersion;
    private LocalDateTime createTime;
}