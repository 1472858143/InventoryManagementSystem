# User 模块 Mapper 能力设计参考

## 1. 文档目的

本文档用于明确 `user` 模块第一批接口所需的最小数据库访问能力，只描述 Mapper 方法设计，不编写 SQL 与实现代码。

## 2. 当前设计范围

当前只为以下 3 类业务设计 Mapper 能力：

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

## 4. 设计约束

1. Mapper 只负责数据库访问
2. Mapper 不负责业务校验
3. Mapper 不负责密码加密
4. Mapper 不直接返回 VO
5. 方法命名必须清晰表达用途

## 5. Mapper 划分建议

建议按表职责拆分为：

- `UserMapper`
- `RoleMapper`
- `UserRoleMapper`

这样更利于职责清晰与后续扩展。

## 6. 方法清单

### 6.1 新增用户相关

#### UserMapper.findByUsername

- 输入：`username`
- 输出：`User` 或 `null`
- 用途：根据用户名查询用户是否已存在

#### RoleMapper.findByIds

- 输入：`List<Long> roleIds`
- 输出：`List<Role>`
- 用途：根据角色 ID 列表查询角色是否存在

#### UserMapper.insert

- 输入：`User`
- 输出：`int`
- 用途：插入用户基础数据，并配合主键回填

#### UserRoleMapper.batchInsert

- 输入：`List<UserRole>`
- 输出：`int`
- 用途：批量写入用户与角色关系

### 6.2 查询用户列表相关

#### UserMapper.findAll

- 输入：无
- 输出：`List<User>`
- 用途：查询用户基础信息列表

#### UserRoleMapper.findRoleCodesByUserIds

- 输入：`List<Long> userIds`
- 输出：`List<UserRoleRelationView>`
- 用途：根据用户 ID 列表查询每个用户对应的角色编码关系

补充说明：

- `UserRoleRelationView` 属于 Mapper 查询结果模型
- 它不属于 VO
- 它不直接返回前端
- 它仅用于 Service 层进行列表结果组装

### 6.3 启用/禁用用户相关

#### UserMapper.findById

- 输入：`userId`
- 输出：`User` 或 `null`
- 用途：根据用户 ID 查询用户是否存在

#### UserMapper.updateStatusById

- 输入：`userId`、`status`
- 输出：`int`
- 用途：根据用户 ID 更新状态字段

## 7. 查询用户列表的方案说明

### 7.1 可选方案

可选方案包括：

1. 一次联表查询全部信息
2. 分步查询用户与角色信息

### 7.2 推荐方案

当前阶段推荐采用“分步查询”方案：

1. `UserMapper.findAll` 查询用户基础信息
2. `UserRoleMapper.findRoleCodesByUserIds` 查询角色编码关系
3. 由 Service 层完成 `userId -> roleCodes` 的聚合与 VO 组装

推荐原因：

- Mapper 职责更单一
- Service 更适合做结果聚合
- 更便于后续增加分页或筛选能力时逐步扩展

## 8. 本阶段结论

当前 `user` 模块第一批接口所需的 Mapper 能力已经明确，后续编码时应坚持：

- Mapper 只查库和写库
- 复杂组装留给 Service
- Mapper 查询结果模型不直接暴露给前端

## 本文件作用

本文档用于指导 Mapper 接口设计与数据库访问能力实现。
进入 Mapper 编码阶段时，应以本文件确定方法边界、输入输出与查询拆分方式。
