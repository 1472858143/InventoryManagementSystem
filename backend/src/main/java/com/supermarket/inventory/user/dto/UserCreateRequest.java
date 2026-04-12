package com.supermarket.inventory.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserCreateRequest(
    @NotBlank(message = "用户名不能为空")
    String username,
    @NotBlank(message = "密码不能为空")
    String password,
    String realName,
    @NotEmpty(message = "角色不能为空")
    List<Long> roleIds
) {
}
