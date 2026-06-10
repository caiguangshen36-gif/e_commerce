package com.e_commerce.module.ai.dto;

import lombok.Data;

/**
 * AI导购请求DTO
 */
@Data
public class ShopGuideRequest {
    private String userId;
    private String message;
}  