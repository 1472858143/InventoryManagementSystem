package com.supermarket.inventory.user.service.impl;

import com.supermarket.inventory.auth.password.PasswordService;
import com.supermarket.inventory.common.exception.BusinessException;
import com.supermarket.inventory.user.dto.UserCreateRequest;
import com.supermarket.inventory.user.dto.UserStatusUpdateRequest;
import com.supermarket.inventory.user.entity.Role;
import com.supermarket.inventory.user.entity.User;
import com.supermarket.inventory.user.entity.UserRole;
import com.supermarket.inventory.user.mapper.RoleMapper;
import com.supermarket.inventory.user.mapper.UserMapper;
import com.supermarket.inventory.user.mapper.UserRoleMapper;
import com.supermarket.inventory.user.model.UserRoleRelationView;
import com.supermarket.inventory.user.service.UserService;
import com.supermarket.inventory.user.vo.UserDetailResponse;
import com.supermarket.inventory.user.vo.UserListItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordService passwordService;

    public UserServiceImpl(
        UserMapper userMapper,
        RoleMapper roleMapper,
        UserRoleMapper userRoleMapper,
        PasswordService passwordService
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional
    public UserDetailResponse createUser(UserCreateRequest request) {
        User existingUser = userMapper.findByUsername(request.username());
        if (existingUser != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        List<Long> roleIds = normalizeRoleIds(request.roleIds());
        if (roleIds.isEmpty()) {
            throw new BusinessException(400, "角色不能为空");
        }

        List<Role> roles = roleMapper.findByIds(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(400, "存在非法角色");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordService.encode(request.password()));
        user.setRealName(request.realName());
        user.setStatus(1);

        userMapper.insert(user);

        List<UserRole> userRoles = buildUserRoles(user.getId(), roleIds);
        userRoleMapper.batchInsert(userRoles);

        User createdUser = userMapper.findById(user.getId());
        List<String> roleCodes = extractRoleCodes(roles);
        return toUserDetailResponse(createdUser != null ? createdUser : user, roleCodes);
    }

    @Override
    public List<UserListItemResponse> listUsers() {
        List<User> users = userMapper.findAll();
        if (users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream()
            .map(User::getId)
            .toList();

        List<UserRoleRelationView> relations = userRoleMapper.findRoleCodesByUserIds(userIds);
        Map<Long, List<String>> roleCodesByUserId = buildRoleCodesMap(relations);

        List<UserListItemResponse> responses = new ArrayList<>(users.size());
        for (User user : users) {
            List<String> roleCodes = roleCodesByUserId.getOrDefault(user.getId(), List.of());
            responses.add(toUserListItemResponse(user, roleCodes));
        }
        return responses;
    }

    @Override
    public void updateUserStatus(UserStatusUpdateRequest request) {
        if (request.status() == null || (request.status() != 0 && request.status() != 1)) {
            throw new BusinessException(400, "状态只能为0或1");
        }

        User user = userMapper.findById(request.userId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        userMapper.updateStatusById(request.userId(), request.status());
    }

    private List<Long> normalizeRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(roleIds));
    }

    private List<UserRole> buildUserRoles(Long userId, List<Long> roleIds) {
        List<UserRole> userRoles = new ArrayList<>(roleIds.size());
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        return userRoles;
    }

    private Map<Long, List<String>> buildRoleCodesMap(List<UserRoleRelationView> relations) {
        Map<Long, List<String>> roleCodesByUserId = new LinkedHashMap<>();
        for (UserRoleRelationView relation : relations) {
            if (relation.getRoleCode() == null || relation.getRoleCode().isBlank()) {
                continue;
            }
            roleCodesByUserId
                .computeIfAbsent(relation.getUserId(), key -> new ArrayList<>())
                .add(relation.getRoleCode());
        }
        return roleCodesByUserId;
    }

    private List<String> extractRoleCodes(List<Role> roles) {
        List<String> roleCodes = new ArrayList<>(roles.size());
        for (Role role : roles) {
            if (role.getRoleCode() != null && !role.getRoleCode().isBlank()) {
                roleCodes.add(role.getRoleCode());
            }
        }
        return roleCodes;
    }

    private UserDetailResponse toUserDetailResponse(User user, List<String> roleCodes) {
        return new UserDetailResponse(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getStatus(),
            List.copyOf(roleCodes),
            resolveCreateTime(user)
        );
    }

    private UserListItemResponse toUserListItemResponse(User user, List<String> roleCodes) {
        return new UserListItemResponse(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getStatus(),
            List.copyOf(roleCodes),
            user.getCreateTime()
        );
    }

    private LocalDateTime resolveCreateTime(User user) {
        if (user.getCreateTime() != null) {
            return user.getCreateTime();
        }
        return LocalDateTime.now();
    }
}
