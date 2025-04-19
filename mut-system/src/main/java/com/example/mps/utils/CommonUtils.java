package com.example.mps.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author pengYuJun
 */
@Slf4j
public class CommonUtils {

    /**
     * 判断是否存在字符串为空
     * @param strs
     * @return
     */
    public static boolean isAnyBlank(String... strs) {
        for (String str : strs) {
            if (!StringUtils.hasText(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean areAllOtherFieldsNull(Object obj, String idFieldName) {
        if (obj == null) {
            // 如果对象本身为null，视为所有字段都为null
            return true;
        }
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(idFieldName)) {
                // 跳过id字段
                continue;
            }
            try {
                // 设置字段为可访问，以便访问私有字段
                field.setAccessible(true);
                Object value = field.get(obj);

                if (value != null) {
                    // 存在不为null的非id字段，返回false
                    return false;
                }
            } catch (IllegalAccessException e) {
                // 处理不可访问的异常
                log.error("无法访问字段: {}", field.getName());
                // 如果出现异常，假设存在非null字段，返回false
                return false;
            }
        }
        // 所有非id字段都为null
        return true;
    }

}
