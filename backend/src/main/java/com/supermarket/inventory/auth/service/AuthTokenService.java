package com.supermarket.inventory.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    public String issueToken(Long userId, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new LoginSession(userId, username, Instant.now()));
        return token;
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    private record LoginSession(Long userId, String username, Instant issuedAt) {
    }
}
