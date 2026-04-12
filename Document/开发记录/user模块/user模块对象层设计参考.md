# User 模块对象层设计参考

## 1. 文档目的

本文档用于明确 `user` 模块第一批接口所需的对象层设计，包括 Entity、DTO、VO 的名单、字段范围与职责边界。

## 2. 当前设计范围

当前只为以下接口准备对象层：

1. 新增用户
2. 查询用户列表
3. 启用/禁用用户

## 3. 当前阶段不处理什么

当前阶段暂不为以下功能扩展对象：

- 删除用户
- 修改用户
- 重置密码
- 细粒度权限控制
- 分页与条件筛选查询

## 4. 对象层设计原则

1. Entity 用于数据库映射
2. DTO 用于接收请求
3. VO 用于返回前端
4. 不允许直接返回 Entity
5. `User` Entity 可以包含 `password`
6. VO 严禁包含 `password` 或密码哈希摘要

## 5. Entity 设计

### 5.1 User

用途说明：

- 映射 `user` 表，用于承载用户基础数据

字段建议：

- `id`
- `username`
- `password`
- `realName`
- `status`
- `createTime`

补充说明：

- `password` 在 Entity 中允许存在，因为它属于数据库映射字段
- Entity 不直接返回前端

### 5.2 Role

用途说明：

- 映射 `role` 表，用于承载角色基础数据

字段建议：

- `id`
- `roleName`
- `roleCode`
- `remark`
- `createTime`

### 5.3 UserRole

用途说明：

- 映射 `user_role` 表，用于承载用户与角色关系数据

字段建议：

- `id`
- `userId`
- `roleId`

## 6. DTO 设计

### 6.1 UserCreateRequest

用途说明：

- 用于接收“新增用户”请求参数

字段建议：

- `username`
- `password`
- `realName`
- `roleIds`

设计说明：

- `password` 为前端传入的明文密码
- `roleIds` 用于角色分配

### 6.2 UserStatusUpdateRequest

用途说明：

- 用于接收“启用/禁用用户”请求参数

字段建议：

- `userId`
- `status`

设计说明：

- 该 DTO 只体现状态修改
- 不混入其他可修改字段

## 7. VO 设计

### 7.1 UserListItemResponse

用途说明：

- 用于“查询用户列表”接口中的单个列表项返回对象

字段建议：

- `id`
- `username`
- `realName`
- `status`
- `roleCodes`
- `createTime`

设计说明：

- 当前阶段角色信息先保持简单，统一使用 `roleCodes`
- VO 中不允许包含 `password`

### 7.2 UserDetailResponse

用途说明：

- 当前阶段主要用于“新增用户成功后的返回对象”，后续详情接口也可复用

字段建议：

- `id`
- `username`
- `realName`
- `status`
- `roleCodes`
- `createTime`

设计说明：

- 当前阶段不做复杂嵌套角色结构
- 不返回 `password`
- 不返回密码哈希摘要

## 8. 对象清单汇总

### 8.1 Entity 清单

- `User`
- `Role`
- `UserRole`

### 8.2 DTO 清单

- `UserCreateRequest`
- `UserStatusUpdateRequest`

### 8.3 VO 清单

- `UserListItemResponse`
- `UserDetailResponse`

## 9. 本阶段结论

当前 `user` 模块对象层已经完成第一批接口所需的基础设计。后续编码时应坚持：

- Entity 保留数据库字段
- DTO 只接收请求
- VO 只返回必要字段
- 所有面向前端的返回对象都不得包含密码相关信息
