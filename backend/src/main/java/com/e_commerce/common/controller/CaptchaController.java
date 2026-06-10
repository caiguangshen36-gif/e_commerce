package com.e_commerce.common.controller;

import com.e_commerce.common.utils.CaptchaUtil;
import com.e_commerce.common.utils.Result;
import com.e_commerce.common.vo.CaptchaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE = 5; // 5分钟

    // 1. 获取验证码（前端调用）
    @GetMapping("/get")
    public Result<CaptchaVO> getCaptcha() throws IOException {
        // 生成唯一ID
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        // 生成验证码文本
        String code = CaptchaUtil.generateCode();
        // 生成Base64图片
        String imgBase64 = CaptchaUtil.generateImageBase64(code);

        // 存入Redis：key=captcha:xxx  value=验证码
        stringRedisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + captchaId, code, CAPTCHA_EXPIRE, TimeUnit.MINUTES);

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setCaptchaImg("data:image/png;base64," + imgBase64); // 前端直接用
        return Result.success(vo);
    }
}