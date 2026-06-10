package com.e_commerce.common.utils;


import org.springframework.beans.BeanUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Bean 转换工具类
 * 用于 Entity 转 VO，避免重复写 BeanUtils.copyProperties
 */
public class BeanConvertUtils {

    /**
     * 单个对象转换
     * @param source 源对象
     * @param targetClass 目标类字节码
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public static <S, T> T convert(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean 转换失败", e);
        }
    }

    /**
     * 列表转换
     * @param sourceList 源列表
     * @param targetClass 目标类字节码
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(source -> convert(source, targetClass))
                .toList(); // Java 16+ 可用 toList()
        // 低版本用 Collectors.toList()
    }
}