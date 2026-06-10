package com.e_commerce.module.user.vo;

import lombok.Data;

/**
 * 用户注册统计VO
 */
@Data
public class UserRegisterStatsVo {
    private Integer todayCount;           // 今日注册用户数
    private Integer yesterdayCount;       // 昨日注册用户数
    private Integer yesterdayDiff;        // 较昨日增减数
    private Double yesterdayRate;         // 较昨日增长率(%)
    
    private Integer thisWeekCount;        // 本周注册用户数
    private Integer lastWeekCount;        // 上周注册用户数
    private Integer lastWeekDiff;         // 较上周增减数
    private Double lastWeekRate;          // 较上周增长率(%)
    
    private Integer totalCount;           // 总注册用户数
}