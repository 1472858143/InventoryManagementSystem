package com.supermarket.inventory.auth.vo;

public record CurrentUserResponse(
    Long userId,
    String username
) {
}
