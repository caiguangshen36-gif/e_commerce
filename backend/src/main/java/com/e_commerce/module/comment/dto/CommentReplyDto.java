package com.e_commerce.module.comment.dto;

import lombok.Data;

@Data
public class CommentReplyDto {
    private Long commentId;
    private String replyContent;
    private Integer replyType;
    private Long replyUserId;
}