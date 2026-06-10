package com.e_commerce.module.notice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysNoticeVo {
    private Long id;
    private Integer noticeType;
    private String noticeTypeName;
    private String title;
    private String content;
    private String bizId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isRead;
    private String isReadText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}