package com.e_commerce.module.comment.dto;

import lombok.Data;

@Data
public class ProductCommentDto {
    private Long productId;
    private Long orderItemId;
    private String content;
    private Integer score;
}