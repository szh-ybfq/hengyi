package com.zh.hengyi.common.utils.enums;

import cn.hutool.core.util.StrUtil;
import com.zh.hengyi.common.exception.BusinessException;
import org.springframework.util.StringUtils;

public class EnumConvertUtil {

    /**
     * 字符串转枚举【容错版本，转换失败返回null，不抛异常】
     * @param enumClass 枚举class
     * @param name 枚举字符串名称
     * @return 枚举实例，失败返回null
     */
    public static <T extends Enum<T>> T strToEnum(Class<T> enumClass, String name){
        //枚举类为null
        if (enumClass == null) {
            return null;
        }
        //name为空、空白
        if (StrUtil.isBlank(name)) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, name.trim());
        } catch (IllegalArgumentException e) {
            //枚举不存在该name，返回null
            return null;
        } catch (Exception e) {
            //异常兜底，例如安全、反射等异常，返回null
            return null;
        }
    }

}

