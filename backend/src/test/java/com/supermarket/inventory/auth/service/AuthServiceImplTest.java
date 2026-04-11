package com.supermarket.inventory.auth.service;

import com.supermarket.inventory.auth.context.AuthContext;
import com.supermarket.inventory.auth.dto.LoginRequest;
import com.supermarket.inventory.auth.mapper.AuthMapper;
import com.supermarket.inventory.auth.model.AuthSession;
import com.supermarket.inventory.auth.model.AuthUserAuthInfo;
import com.supermarket.inventory.auth.password.PasswordService;
import com.supermarket.inventory.auth.vo.CurrentUserResponse;
import com.supermarket.inventory.auth.vo.LoginResponse;
import com.supermarket.inventory.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordService passwordService;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void loginShouldSucceedWhenUsernameAndPasswordCorrect() {
        AuthUserAuthInfo authInfo = buildAuthInfo(1L, "admin", "hash", 1, "ADMIN");
        when(authMapper.findAuthInfoByUsername("admin")).thenReturn(List.of(authInfo));
        when(passwordService.matches("123456", "hash")).thenReturn(true);
        when(authTokenService.issueToken(1L, "admin")).thenReturn("token-123");

        LoginResponse response = authService.login(new LoginRequest("admin", "123456"));

        assertEquals("token-123", response.token());
        assertEquals("admin", response.username());
        assertEquals(List.of("ADMIN"), response.roles());
    }

    @Test
    void loginShouldReturn401WhenUserNotFound() {
        when(authMapper.findAuthInfoByUsername("missing")).thenReturn(List.of());

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> authService.login(new LoginRequest("missing", "123456"))
        );

        assertEquals(401, ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void loginShouldReturn401WhenPasswordIncorrect() {
        AuthUserAuthInfo authInfo = buildAuthInfo(1L, "admin", "hash", 1, "ADMIN");
        when(authMapper.findAuthInfoByUsername("admin")).thenReturn(List.of(authInfo));
        when(passwordService.matches("wrong", "hash")).thenReturn(false);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> authService.login(new LoginRequest("admin", "wrong"))
        );

        assertEquals(401, ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void loginShouldReturn403WhenUserDisabled() {
        AuthUserAuthInfo authInfo = buildAuthInfo(1L, "admin", "hash", 0, "ADMIN");
        when(authMapper.findAuthInfoByUsername("admin")).thenReturn(List.of(authInfo));

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> authService.login(new LoginRequest("admin", "123456"))
        );

        assertEquals(403, ex.getCode());
        assertEquals("当前用户已被禁用", ex.getMessage());
    }

    @Test
    void currentUserShouldReturnSessionInfo() {
        AuthContext.setCurrentSession(new AuthSession(7L, "tester", java.time.Instant.now()));

        CurrentUserResponse response = authService.currentUser();

        assertEquals(7L, response.userId());
        assertEquals("tester", response.username());
    }

    @Test
    void currentUserShouldReturn401WhenNoSession() {
        BusinessException ex = assertThrows(BusinessException.class, authService::currentUser);
        assertEquals(401, ex.getCode());
        assertEquals("未登录或认证失败", ex.getMessage());
    }

    @Test
    void logoutShouldInvalidateToken() {
        authService.logout("token-123");
        verify(authTokenService).invalidate("token-123");
    }

    private AuthUserAuthInfo buildAuthInfo(
        Long userId,
        String username,
        String passwordHash,
        Integer status,
        String roleCode
    ) {
        AuthUserAuthInfo authInfo = new AuthUserAuthInfo();
        authInfo.setUserId(userId);
        authInfo.setUsername(username);
        authInfo.setPasswordHash(passwordHash);
        authInfo.setStatus(status);
        authInfo.setRoleCode(roleCode);
        return authInfo;
    }
}
