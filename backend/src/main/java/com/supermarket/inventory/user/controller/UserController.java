package com.supermarket.inventory.user.controller;

import com.supermarket.inventory.common.response.ApiResponse;
import com.supermarket.inventory.user.dto.UserCreateRequest;
import com.supermarket.inventory.user.dto.UserStatusUpdateRequest;
import com.supermarket.inventory.user.service.UserService;
import com.supermarket.inventory.user.vo.UserDetailResponse;
import com.supermarket.inventory.user.vo.UserListItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<UserDetailResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @GetMapping
    public ApiResponse<List<UserListItemResponse>> listUsers() {
        return ApiResponse.success(userService.listUsers());
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateUserStatus(@Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(request);
        return ApiResponse.success();
    }
}
