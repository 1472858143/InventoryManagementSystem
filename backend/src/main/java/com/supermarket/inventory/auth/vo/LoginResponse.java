package com.supermarket.inventory.auth.vo;

import java.util.List;

public record LoginResponse(
    String token,
    String username,
    List<String> roles
) {
}
