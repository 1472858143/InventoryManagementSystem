package com.supermarket.inventory.auth.model;

import java.time.Instant;

public record AuthSession(
    Long userId,
    String username,
    Instant issuedAt
) {
}
