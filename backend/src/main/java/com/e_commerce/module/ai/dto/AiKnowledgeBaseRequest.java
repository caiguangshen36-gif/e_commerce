package com.e_commerce.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 知识库请求DTO
 */
@Data
public class AiKnowledgeBaseRequest {
    @NotBlank(message = "规则标题不能为空")
    private String title;
    @NotBlank(message = "规则内容不能为空")
    private String content;
    @NotBlank(message = "规则类型不能为空")
    @Pattern(regexp = "^(goods|after_sale|service|promotion|order|logistics|payment)$", message = "规则类型只能是 goods、after_sale、service、promotion、order、logistics、payment 之一")
    private String type;
    private String keywords;
    private Integer status = 1;
}
