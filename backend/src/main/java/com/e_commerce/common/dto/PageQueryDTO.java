package com.e_commerce.common.dto;

import lombok.Data;

/**
 * 分页查询数据传输对象
 * 用于封装分页查询相关的参数
 */
@Data
public class PageQueryDTO {
    private Long pageNum = 1L;  // 当前页码，默认值为1
    private Long pageSize = 10L; // 每页显示条数，默认值为10
}