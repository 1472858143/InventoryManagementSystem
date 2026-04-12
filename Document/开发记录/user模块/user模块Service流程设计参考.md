# User 模块 Service 流程设计参考

## 1. 文档目的

本文档用于明确 `user` 模块第一批接口的 Service 层流程、校验点、异常语义与事务边界，为后续实现提供统一参考。

## 2. 当前设计范围

当前只设计以下 3 个接口对应的 Service 流程：

1. 新增用户
2. 查询用户列表
3. 启用/禁用用户

## 3. 当前阶段不处理什么

当前阶段暂不设计：

- 删除用户
- 修改用户
- 重置密码
- 细粒度权限控制
- 分页与条件筛选查询

## 4. UserService 方法清单

建议当前阶段提供以下方法：

1. `createUser(UserCreateRequest request)`
2. `listUsers()`
3. `updateUserStatus(UserStatusUpdateRequest request)`

## 5. 新增用户流程设计

### 5.1 输入与输出

- 输入：`UserCreateRequest`
- 输出：`UserDetailResponse`

### 5.2 业务流程

1. 接收 `UserCreateRequest`
2. 调用 `UserMapper.findByUsername` 校验用户名是否已存在
3. 校验 `roleIds` 是否为空，并调用 `RoleMapper.findByIds` 校验角色是否合法
4. 调用 `PasswordService.encode(...)` 对明文密码做哈希
5. 组装 `User`
6. 调用 `UserMapper.insert` 写入 `user` 表
7. 组装 `List<UserRole>`
8. 调用 `UserRoleMapper.batchInsert` 写入 `user_role` 表
9. 组装并返回 `UserDetailResponse`

### 5.3 校验点

- 用户名是否已存在
- `roleIds` 是否为空
- `roleIds` 中是否存在非法角色

### 5.4 涉及的 Mapper / Service

- `UserMapper.findByUsername`
- `RoleMapper.findByIds`
- `UserMapper.insert`
- `UserRoleMapper.batchInsert`
- `PasswordService.encode(...)`

### 5.5 异常语义

以下情况应抛出 `BusinessException`：

- 用户名已存在
- `roleIds` 为空
- 存在非法角色

### 5.6 事务要求

- 该流程必须使用事务
- 涉及 `user` 与 `user_role` 两表写入
- 任一步失败都必须回滚

## 6. 查询用户列表流程设计

### 6.1 输入与输出

- 输入：无
- 输出：`List<UserListItemResponse>`

### 6.2 业务流程

1. 调用 `UserMapper.findAll` 查询用户基础信息
2. 提取 `userIds`
3. 调用 `UserRoleMapper.findRoleCodesByUserIds` 查询角色编码关系
4. 在 Service 层组装 `userId -> roleCodes`
5. 将 `User + roleCodes` 组装为 `List<UserListItemResponse>`
6. 返回列表结果

### 6.3 校验点

- 无强业务校验
- 重点在空结果与空角色集合的兼容处理

### 6.4 涉及的 Mapper / Service

- `UserMapper.findAll`
- `UserRoleMapper.findRoleCodesByUserIds`

### 6.5 设计说明

当前阶段推荐由 Service 层做组装，原因如下：

1. Mapper 只负责查数据，职责更单一
2. 角色聚合逻辑更适合放在 Service
3. 后续如增加分页或筛选，扩展更自然

当用户没有角色时：

- `roleCodes` 应返回空列表
- 不建议返回 `null`

### 6.6 事务要求

- 不需要事务

## 7. 启用/禁用用户流程设计

### 7.1 输入与输出

- 输入：`UserStatusUpdateRequest`
- 输出：成功结果或空响应

### 7.2 业务流程

1. 接收 `UserStatusUpdateRequest`
2. 校验 `status` 是否合法，只允许 `0` 或 `1`
3. 调用 `UserMapper.findById` 查询用户是否存在
4. 若用户不存在，抛出业务异常
5. 调用 `UserMapper.updateStatusById` 更新状态
6. 返回成功结果

### 7.3 校验点

- `status` 是否为 `0` 或 `1`
- 用户是否存在

### 7.4 涉及的 Mapper

- `UserMapper.findById`
- `UserMapper.updateStatusById`

### 7.5 异常语义

以下情况应抛出 `BusinessException`：

- `status` 非法
- 用户不存在

### 7.6 事务要求

- 不需要事务

## 8. 职责边界总结

### 8.1 Controller 负责

- 接收请求
- 参数校验
- 调用 Service
- 返回响应

### 8.2 Service 负责

- 业务校验
- 调用 `PasswordService`
- 多表写入编排
- 事务控制
- VO 组装

### 8.3 Mapper 负责

- 数据库访问

### 8.4 PasswordService 负责

- 密码哈希处理

## 9. 本阶段结论

当前 `user` 模块第一批接口的 Service 层流程已经明确。后续实现时应坚持：

- 新增用户走事务
- 列表查询采用分步查询后由 Service 组装
- 状态更新只修改 `status`
- 所有密码操作统一复用 `PasswordService`
