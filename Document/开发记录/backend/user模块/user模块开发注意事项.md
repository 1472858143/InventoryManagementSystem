# User 模块开发注意事项

## 1. 文档目的

本文档用于在 `user` 模块正式编码前，统一确认该模块的职责范围、数据库依赖、密码处理规则、对象设计要求、事务边界与开发顺序。

## 2. 当前设计范围

当前阶段只围绕以下 3 个接口做准备：

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

## 4. 模块职责与边界

`user` 模块只负责：

- 用户管理
- 用户与角色关系维护

`user` 模块不负责：

- 登录
- 登出
- token 生成
- token 校验

这些能力统一由 `auth` 模块承担。

## 5. 数据库结构确认

当前仅使用以下三张表：

- `user`
- `role`
- `user_role`

需要遵守的约束：

1. `user.username` 唯一
2. `user.password` 只存储哈希摘要，不存明文
3. `user.status`：`0` 为禁用，`1` 为启用
4. `role.role_code` 唯一
5. `user_role` 用于维护用户与角色关系

## 6. 密码处理规则

当前阶段必须严格遵守以下规则：

1. 所有密码处理必须复用 `auth` 模块中的 `PasswordService`
2. 新增用户时接收前端传入的明文密码，再通过 `PasswordService.encode(...)` 生成哈希摘要后入库
3. 不允许在 `user` 模块中自行 `new BCryptPasswordEncoder`
4. 不允许散落编写密码哈希逻辑
5. 所有接口响应中都不得返回 `password` 或密码哈希摘要

## 7. 对象设计要求

在开发接口前，必须先设计：

### 7.1 Entity

- `User`
- `Role`
- `UserRole`

### 7.2 DTO

- `UserCreateRequest`
- `UserStatusUpdateRequest`

### 7.3 VO

- `UserListItemResponse`
- `UserDetailResponse`

设计要求：

1. DTO 只用于接收请求
2. VO 只用于返回数据
3. 不允许直接返回 Entity
4. VO 中不得包含 `password`

## 8. 事务边界

### 8.1 新增用户

- 涉及 `user` 与 `user_role` 两表写入
- 必须作为一个事务处理
- 任一步失败都必须回滚

### 8.2 查询用户列表

- 不需要事务

### 8.3 启用/禁用用户

- 不需要事务

## 9. 查询返回字段约束

用户查询接口只允许返回：

- `id`
- `username`
- `realName`
- `status`
- 角色信息
- `createTime`

禁止返回：

- `password`
- 密码哈希摘要

## 10. 测试数据准备

数据库中至少应存在一个角色，例如：

- `ADMIN`

该角色用于测试用户创建与角色分配流程。

## 11. 开发顺序要求

必须按以下顺序推进：

1. 先设计 Entity / DTO / VO
2. 再设计 Mapper 能力
3. 再设计并实现 Service
4. 最后设计并实现 Controller

禁止直接从 Controller 开始开发。

## 12. 本阶段结论

`user` 模块当前阶段的重点不是快速写接口，而是先把对象边界、密码规则、事务边界和开发顺序统一下来，为后续编码打好基线。

## 本文件作用

本文档用于统一 `user` 模块开发前的边界、约束与顺序要求。
进入编码阶段前应优先阅读本文件，再进入对象层、Mapper、Service 与 Controller 的具体设计文档。
