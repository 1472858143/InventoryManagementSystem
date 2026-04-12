package com.supermarket.inventory.user.service;

import com.supermarket.inventory.user.dto.UserCreateRequest;
import com.supermarket.inventory.user.dto.UserStatusUpdateRequest;
import com.supermarket.inventory.user.vo.UserDetailResponse;
import com.supermarket.inventory.user.vo.UserListItemResponse;

import java.util.List;

public interface UserService {

    UserDetailResponse createUser(UserCreateRequest request);

    List<UserListItemResponse> listUsers();

    void updateUserStatus(UserStatusUpdateRequest request);
}
