package com.zh.hengyi.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    // 创建人
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    // 创建时间：新增填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    // 修改时间：新增、更新都填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    // 状态 默认0（启用）【新增自动填充，更新不覆盖】
    @TableField(fill = FieldFill.INSERT)
    private Integer status;

    // 逻辑删除字段（你之前要的逻辑删除）
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
