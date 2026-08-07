package com.zh.hengyi.admin.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户分页VO")
public class UserPageVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    @Schema(description = "角色id集合")
    private List<Long> roleIdList;
    @Schema(description = "角色名称集合")
    private List<String> roleNameList;
}