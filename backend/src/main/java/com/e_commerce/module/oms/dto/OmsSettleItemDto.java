package com.e_commerce.module.oms.dto;

import lombok.Data;


/**
 * 结算单明细DTO
 */
@Data
public class OmsSettleItemDto {
    private Long cartId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
}