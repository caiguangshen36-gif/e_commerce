package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 结算单VO
 */
@Data
public class OmsSettleVo {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private Long addressId;
    private Integer status;
    private String statusText;
    private String receiver;
    private String phone;
    private String address;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<OmsSettleItemVo> items;
}