package com.e_commerce.module.comment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReplyVo {
    private Long id;
    private Long commentId;
    private String replyContent;
    private Integer replyType;
    private String replyTypeText;
    private Long replyUserId;
    private String replyUsername;
    private String replyUserAvatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
