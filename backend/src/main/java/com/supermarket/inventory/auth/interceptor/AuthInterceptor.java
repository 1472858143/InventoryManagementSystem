package com.supermarket.inventory.auth.interceptor;

import com.supermarket.inventory.auth.context.AuthContext;
import com.supermarket.inventory.auth.model.AuthSession;
import com.supermarket.inventory.auth.service.AuthTokenService;
import com.supermarket.inventory.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenService authTokenService;

    public AuthInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request.getHeader("Authorization"));
        AuthSession session = authTokenService.getSession(token)
            .orElseThrow(() -> new BusinessException(401, "未登录或认证失败"));
        AuthContext.setCurrentSession(session);
        return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex
    ) {
        AuthContext.clear();
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
