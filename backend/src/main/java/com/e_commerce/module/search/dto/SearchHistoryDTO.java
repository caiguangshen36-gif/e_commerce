package com.e_commerce.module.search.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchHistoryDTO {
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
}
