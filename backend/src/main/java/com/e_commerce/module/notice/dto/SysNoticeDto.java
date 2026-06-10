package com.e_commerce.module.notice.dto;

import lombok.Data;

import java.util.List;

@Data
public class SysNoticeDto {
    private Integer noticeType;
    private String title;
    private String content;
    private String bizId;
    private List<Long> roleIds;
}