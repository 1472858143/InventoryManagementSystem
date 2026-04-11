package com.supermarket.inventory.auth.service;

import com.supermarket.inventory.auth.model.AuthSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();

    public String issueToken(Long userId, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new AuthSession(userId, username, Instant.now()));
        return token;
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public boolean isValid(String token) {
        return token != null && !token.isBlank() && sessions.containsKey(token);
    }

    public Optional<AuthSession> getSession(String token) {
        if (!isValid(token)) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }
}
