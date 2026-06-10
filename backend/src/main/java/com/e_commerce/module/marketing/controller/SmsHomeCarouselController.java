package com.e_commerce.module.marketing.controller;

import com.e_commerce.common.utils.BeanConvertUtils;
import com.e_commerce.common.utils.Result;
import com.e_commerce.module.marketing.vo.SmsHomeCarouselVo;
import com.e_commerce.module.marketing.dto.SmsHomeCarouselDto;
import com.e_commerce.module.marketing.entity.SmsHomeCarousel;
import com.e_commerce.module.marketing.service.SmsHomeCarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketing/carousel")
public class SmsHomeCarouselController {

    @Autowired
    private SmsHomeCarouselService carouselService;

    @PostMapping
    public Result<String> addCarousel(@RequestBody SmsHomeCarouselDto carousel) {
        carouselService.addCarousel(carousel);
        return Result.success("轮播添加成功");
    }

    @PostMapping("/update")
    public Result<String> updateCarousel(@RequestBody SmsHomeCarouselDto carousel) {
        SmsHomeCarousel existingCarousel = carouselService.getCarouselById(carousel.getId());
        if (existingCarousel == null) {
            return Result.error("轮播不存在");
        }
        carouselService.updateCarousel(carousel);
        return Result.success("轮播更新成功");
    }

    @PostMapping("/updateStatus")
    public Result<String> updateStatusCarousel(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        SmsHomeCarousel existingCarousel = carouselService.getCarouselById(id);
        if (existingCarousel == null) {
            return Result.error("轮播不存在");
        }
        carouselService.updateStatusCarousel(id);
        return Result.success("状态更新成功");
    }

    @PostMapping("/list")
    public Result<List<SmsHomeCarouselVo>> getCarouselList() {
        List<SmsHomeCarouselVo> carouselsVo = carouselService.getCarouselList();
        return Result.success(carouselsVo);
    }

    @PostMapping("/detail")
    public Result<SmsHomeCarouselVo> getCarouselDetail(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        SmsHomeCarousel carousel = carouselService.getCarouselById(id);
        if (carousel == null) {
            return Result.error("轮播不存在");
        }
        SmsHomeCarouselVo smsHomeCarouselVo = BeanConvertUtils.convert(carousel, SmsHomeCarouselVo.class);
        return Result.success(smsHomeCarouselVo);
    }

    @PostMapping("/delete")
    public Result<String> deleteCarousel(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        SmsHomeCarousel existingCarousel = carouselService.getCarouselById(id);
        if (existingCarousel == null) {
            return Result.error("轮播不存在");
        }
        carouselService.deleteCarousel(id);
        return Result.success("轮播删除成功");
    }

    @PostMapping("/enabled")
    public Result<List<SmsHomeCarouselVo>> getEnabledCarouselList() {
        List<SmsHomeCarouselVo> carouselsVo = carouselService.getEnabledCarouselList();
        return Result.success(carouselsVo);
    }
}