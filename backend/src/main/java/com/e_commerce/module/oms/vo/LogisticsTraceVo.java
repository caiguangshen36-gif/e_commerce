package com.e_commerce.module.oms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹值对象类
 * 用于封装物流轨迹相关的数据信息
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
public class LogisticsTraceVo {
    private Long id;            // 轨迹ID
    private Long logisticsId;   // 物流ID
    private String content;    // 物流轨迹内容描述
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;  // 物流轨迹创建时间，使用Java 8的LocalDateTime类型
}

