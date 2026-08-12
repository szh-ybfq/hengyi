package com.zh.hengyi.admin.model.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CartSelectDTO {
    // 单条/多条 局部修改传skuId，skuIdList=[A,B]
    // 全选/清空 skuIdList=null selected=1 / skuIdList=null selected=0；
    private List<Long> skuIdList;

    // 0未选中 1选中
    @NotNull(message = "选中状态不能为空")
    private Integer selected;
}