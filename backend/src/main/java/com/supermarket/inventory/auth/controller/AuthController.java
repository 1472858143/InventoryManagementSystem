package com.supermarket.inventory.auth.controller;

import com.supermarket.inventory.auth.dto.LoginRequest;
import com.supermarket.inventory.auth.service.AuthService;
import com.supermarket.inventory.auth.vo.CurrentUserResponse;
import com.supermarket.inventory.auth.vo.LoginResponse;
import com.supermarket.inventory.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.logout(extractToken(authorization));
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> currentUser() {
        return ApiResponse.success(authService.currentUser());
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
