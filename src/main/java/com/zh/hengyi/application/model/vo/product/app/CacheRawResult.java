package com.zh.hengyi.application.model.vo.product.app;


import lombok.Data;
import java.io.Serializable;

@Data
public class CacheRawResult<T> implements Serializable {
    private boolean fromLocal;
    private Object data;
}

