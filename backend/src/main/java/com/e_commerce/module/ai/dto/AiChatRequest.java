package com.e_commerce.module.ai.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private String message;
    private Long userId;
}
