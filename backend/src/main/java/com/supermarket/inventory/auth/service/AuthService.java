package com.supermarket.inventory.auth.service;

import com.supermarket.inventory.auth.dto.LoginRequest;
import com.supermarket.inventory.auth.vo.CurrentUserResponse;
import com.supermarket.inventory.auth.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String token);

    CurrentUserResponse currentUser();
}
