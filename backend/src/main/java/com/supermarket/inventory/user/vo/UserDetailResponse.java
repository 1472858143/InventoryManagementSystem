package com.supermarket.inventory.user.vo;

import java.time.LocalDateTime;
import java.util.List;

public record UserDetailResponse(
    Long id,
    String username,
    String realName,
    Integer status,
    List<String> roleCodes,
    LocalDateTime createTime
) {
}
