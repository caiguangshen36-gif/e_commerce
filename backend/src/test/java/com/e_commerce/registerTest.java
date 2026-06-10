package com.e_commerce;

import com.e_commerce.common.utils.BCryptUtils;
import org.junit.jupiter.api.Test;

public class registerTest {
    @Test
    public void test(){
        String username="zhangsan";
        String password="123456";
        String encode = BCryptUtils.encode(password);
        System.out.println(encode);
    }
}
