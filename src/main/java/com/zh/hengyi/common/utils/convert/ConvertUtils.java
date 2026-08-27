package com.zh.hengyi.common.utils.convert;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.List;

public class ConvertUtils {
    public static <T, R> List<R> convertList(List<T> sourceList, Class<R> targetCls) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(source -> BeanUtil.copyProperties(source, targetCls))
                .toList();
    }
}
