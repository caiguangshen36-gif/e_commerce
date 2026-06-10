package com.e_commerce;

import com.e_commerce.common.utils.CaptchaUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CaptchaTest {
    @Test
    public void testCaptcha() {
        String s = CaptchaUtil.generateCode();
        System.out.println("验证码文本: "+s);
    }

    @Test
    public void testGenerateImage(){
        try {
            String s = CaptchaUtil.generateImageBase64("1234");
            System.out.println("验证码图片: "+s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
