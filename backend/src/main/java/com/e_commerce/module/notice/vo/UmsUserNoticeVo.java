package com.e_commerce.module.notice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UmsUserNoticeVo {
    private Long id;
    private Long userId;
    private Integer noticeType;
    private String noticeTypeName;
    private String title;
    private String content;
    private String bizId;
    private Integer isRead;
    private String isReadText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}