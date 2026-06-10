package com.e_commerce.module.product;

import com.e_commerce.module.product.service.PmsProductEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EsDataInitializer implements ApplicationRunner {

    private final PmsProductEsService pmsProductEsService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        pmsProductEsService.syncAllToEs();
        log.info("应用启动，ES全量同步完成");
    }
}
