package com.supermarket.inventory.auth;

import com.supermarket.inventory.auth.controller.AuthController;
import com.supermarket.inventory.auth.interceptor.AuthInterceptor;
import com.supermarket.inventory.auth.mapper.AuthMapper;
import com.supermarket.inventory.auth.password.PasswordService;
import com.supermarket.inventory.auth.service.AuthServiceImpl;
import com.supermarket.inventory.auth.service.AuthTokenService;
import com.supermarket.inventory.common.exception.GlobalExceptionHandler;
import com.supermarket.inventory.config.WebMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
    AuthController.class,
    AuthServiceImpl.class,
    AuthTokenService.class,
    AuthInterceptor.class,
    WebMvcConfig.class,
    GlobalExceptionHandler.class
})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthTokenService authTokenService;

    @MockBean
    private AuthMapper authMapper;

    @MockBean
    private PasswordService passwordService;

    @Test
    void meShouldReturnCurrentUserWhenTokenValid() throws Exception {
        String token = authTokenService.issueToken(1L, "admin");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void meShouldReturn401WhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("未登录或认证失败"));
    }

    @Test
    void meShouldReturn401WhenTokenInvalid() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("未登录或认证失败"));
    }

    @Test
    void logoutShouldInvalidateTokenThenMeReturn401() throws Exception {
        String token = authTokenService.issueToken(2L, "tester");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("未登录或认证失败"));
    }
}
