# User 模块 Controller / API 设计参考

## 1. 文档目的

本文档用于明确 `user` 模块第一批接口的 Controller 结构、API 定义、认证要求、参数校验要求与统一响应规范。

## 2. 当前设计范围

当前只覆盖以下接口：

1. 新增用户
2. 查询用户列表
3. 启用/禁用用户

## 3. 当前阶段不处理什么

当前阶段暂不处理：

- 删除用户
- 修改用户
- 重置密码
- 细粒度权限控制
- 分页与条件筛选查询

## 4. UserController 类结构说明

建议设置统一控制器：

- 类名：`UserController`
- 基础路径：`/api/users`

Controller 只负责：

- 接收请求
- 参数校验
- 调用 `UserService`
- 返回 `ApiResponse<T>`

Controller 不负责：

- 业务逻辑
- 数据库操作
- 密码处理
- token 解析

## 5. 接口定义

### 5.1 新增用户

- URL：`POST /api/users`
- HTTP 方法：`POST`
- 输入：`UserCreateRequest`
- 输出：`ApiResponse<UserDetailResponse>`
- 是否认证：是
- 是否使用 `@Valid`：是

设计说明：

- 请求体使用 JSON
- Controller 调用 `UserService.createUser(...)`
- 返回结果不得包含 `password` 或密码哈希摘要

### 5.2 查询用户列表

- URL：`GET /api/users`
- HTTP 方法：`GET`
- 输入：无
- 输出：`ApiResponse<List<UserListItemResponse>>`
- 是否认证：是
- 是否使用 `@Valid`：否

设计说明：

- 当前阶段为无分页、无条件筛选版本
- 返回列表中每个元素都不得包含密码相关字段

### 5.3 启用/禁用用户

#### 方案 A

- URL：`PUT /api/users/status`
- HTTP 方法：`PUT`
- 输入：`UserStatusUpdateRequest`
- 输出：`ApiResponse<Void>`
- 是否认证：是
- 是否使用 `@Valid`：是

#### 方案 B

- URL：`PUT /api/users/{id}/status`
- HTTP 方法：`PUT`
- 输入：路径参数 `id` + 请求体 `status`
- 输出：`ApiResponse<Void>`
- 是否认证：是
- 是否使用 `@Valid`：是

#### 当前阶段定稿方案

当前阶段采用方案 A：

- `PUT /api/users/status`
- 请求体使用 `UserStatusUpdateRequest`

采用原因：

1. 与现有 DTO 设计一致
2. 编码改动最小
3. 更符合当前阶段“先完成主线能力”的原则

## 6. 参数校验设计说明

### 6.1 UserCreateRequest

至少应校验：

- `username` 非空
- `password` 非空
- `roleIds` 非空

### 6.2 UserStatusUpdateRequest

至少应校验：

- `userId` 非空
- `status` 非空
- `status` 只能为 `0` 或 `1`

参数校验建议统一使用：

- `@Valid`
- Bean Validation 注解

校验失败由全局异常处理器统一处理。

## 7. 认证与拦截说明

当前阶段这三个接口都必须登录后访问：

1. `POST /api/users`
2. `GET /api/users`
3. `PUT /api/users/status`

认证链路说明：

1. 前端登录后获取 token
2. 调用 `user` 模块接口时在请求头中携带 token
3. 拦截器统一校验 token
4. Controller 不自行解析 token

## 8. 响应结构说明

所有接口统一返回：

- `ApiResponse<T>`

成功响应约定：

- `code = 0`
- `message = "success"`

失败响应约定：

- 抛出 `BusinessException`
- 由 `GlobalExceptionHandler` 统一处理

## 9. 本阶段结论

当前阶段建议采用以下 3 个接口：

1. `POST /api/users`
2. `GET /api/users`
3. `PUT /api/users/status`

这套方案与当前 DTO / VO / Service 设计保持一致，适合作为 `user` 模块第一阶段的统一 API 基线。
