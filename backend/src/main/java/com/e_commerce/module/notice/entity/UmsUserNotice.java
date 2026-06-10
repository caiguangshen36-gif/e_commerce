package com.e_commerce.module.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_user_notice")
public class UmsUserNotice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer noticeType;
    private String title;
    private String content;
    private String bizId;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}