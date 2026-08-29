package com.zh.hengyi.admin.model.dto.seckill;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillActivityQueryDTO extends BaseQueryDTO {

    private String activityName;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
