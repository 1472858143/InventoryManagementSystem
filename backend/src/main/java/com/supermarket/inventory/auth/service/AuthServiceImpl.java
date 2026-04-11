package com.supermarket.inventory.auth.service;

import com.supermarket.inventory.auth.dto.AuthUserAuthInfo;
import com.supermarket.inventory.auth.dto.LoginRequest;
import com.supermarket.inventory.auth.mapper.AuthMapper;
import com.supermarket.inventory.auth.password.PasswordService;
import com.supermarket.inventory.auth.vo.LoginResponse;
import com.supermarket.inventory.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final PasswordService passwordService;
    private final AuthTokenService authTokenService;

    public AuthServiceImpl(
        AuthMapper authMapper,
        PasswordService passwordService,
        AuthTokenService authTokenService
    ) {
        this.authMapper = authMapper;
        this.passwordService = passwordService;
        this.authTokenService = authTokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        List<AuthUserAuthInfo> authInfos = authMapper.findAuthInfoByUsername(request.username());
        if (authInfos.isEmpty()) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        AuthUserAuthInfo first = authInfos.getFirst();
        if (first.getStatus() == null || first.getStatus() != 1) {
            throw new BusinessException(403, "当前用户已被禁用");
        }

        if (!passwordService.matches(request.password(), first.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        LinkedHashSet<String> roles = new LinkedHashSet<>();
        for (AuthUserAuthInfo authInfo : authInfos) {
            if (authInfo.getRoleCode() != null && !authInfo.getRoleCode().isBlank()) {
                roles.add(authInfo.getRoleCode());
            }
        }

        String token = authTokenService.issueToken(first.getUserId(), first.getUsername());
        return new LoginResponse(token, first.getUsername(), List.copyOf(roles));
    }

    @Override
    public void logout(String token) {
        authTokenService.invalidate(token);
    }
}
