package com.e_commerce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.e_commerce.**.mapper")
@EnableRetry   // [Q4优化1] 启用Spring Retry，支持@Retryable注解
@EnableAsync   // [Q4优化4] 启用Spring异步，支持@Async注解
public class ECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }

}