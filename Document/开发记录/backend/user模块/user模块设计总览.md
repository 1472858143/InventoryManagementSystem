# User 模块设计总览

版本：v1.0（用户模块第一阶段：基础用户管理）

## 1. 文档目的

本文档是 `user` 模块第一阶段开发设计的统一入口文档，用于集中说明当前模块的开发范围、职责边界、核心设计结论与相关详细文档的引用关系。

适用阶段：

- `user` 模块编码前的统一设计确认
- `user` 模块第一批接口开发时的快速查阅
- 答辩或论文撰写时的设计总览说明

## 2. 当前开发范围

当前第一批接口只包括：

1. 新增用户
2. 查询用户列表
3. 启用/禁用用户

当前阶段先聚焦用户管理主线能力，不提前引入额外复杂功能。

## 3. 模块职责与边界总结

`user` 模块当前只负责：

- 用户基础数据维护
- 用户与角色关系维护

`user` 模块当前不负责：

- 登录
- 登出
- token 生成
- token 校验

以上认证相关能力统一属于 `auth` 模块。

## 4. 统一约束总结

当前阶段必须统一遵守以下约束：

1. 所有密码处理必须复用 `auth` 模块中的 `PasswordService`
2. 不允许在 `user` 模块中自行 `new BCryptPasswordEncoder`
3. 所有 VO 严禁返回 `password` 或密码哈希摘要
4. Controller 不允许直接访问 Mapper
5. Service 负责业务校验、事务控制、VO 组装与多表写入编排
6. Mapper 只负责数据库访问

## 5. 对象层结论摘要

### 5.1 Entity 清单

- `User`
- `Role`
- `UserRole`

### 5.2 DTO 清单

- `UserCreateRequest`
- `UserStatusUpdateRequest`

### 5.3 VO 清单

- `UserListItemResponse`
- `UserDetailResponse`

### 5.4 关键结论

- `User` 作为数据库映射对象，可以包含 `password`
- VO 绝对不能包含 `password`
- VO 绝对不能包含密码哈希摘要
- `UserDetailResponse` 当前阶段主要用于“新增用户成功后的返回对象”，后续如果增加用户详情接口，也可以继续复用

## 6. Mapper 结论摘要

当前第一批接口需要的最小 Mapper 能力包括：

- `UserMapper.findByUsername`
- `RoleMapper.findByIds`
- `UserMapper.insert`
- `UserRoleMapper.batchInsert`
- `UserMapper.findAll`
- `UserRoleMapper.findRoleCodesByUserIds`
- `UserMapper.findById`
- `UserMapper.updateStatusById`

其中，用户列表查询当前推荐采用“分步查询”方案：

1. 先查用户基础信息
2. 再查用户角色编码关系
3. 由 Service 层完成聚合与组装

补充说明：

- `UserRoleRelationView` 属于 Mapper 查询结果模型
- 它不是 VO
- 它不直接返回前端

## 7. Service 结论摘要

当前第一批接口对应的核心 Service 方法为：

- `createUser`
- `listUsers`
- `updateUserStatus`

事务结论如下：

- 新增用户：需要事务
- 查询用户列表：不需要事务
- 启用/禁用用户：不需要事务

原因说明：

- 新增用户涉及 `user` 与 `user_role` 两张表的写入，必须保证原子性
- 查询列表与状态更新属于单次读取或单表更新，不需要额外事务编排

## 8. API 结论摘要

当前阶段建议采用以下接口定义：

1. `POST /api/users`
2. `GET /api/users`
3. `PUT /api/users/status`

统一认证要求：

- 以上三个接口都必须经过 `auth` 模块认证
- token 的校验由拦截器完成，不由 Controller 手工处理

状态更新接口定稿说明：

- 当前阶段采用：`PUT /api/users/status`
- 请求体使用：`UserStatusUpdateRequest`
- 后续如需统一更 RESTful 的风格，再整体评估是否调整为路径参数方案

## 9. 当前阶段未纳入范围的内容

当前阶段暂不纳入：

- 删除用户
- 修改用户
- 重置密码
- 细粒度权限控制
- 分页查询
- 条件筛选查询

补充说明：

- `GET /api/users` 当前阶段先采用无分页、无条件筛选版本
- 后续如业务范围扩大，再扩展分页与查询条件能力

## 10. 文档引用关系

本总览文档对应以下 6 份相关文档：

1. `user模块开发注意事项.md`
2. `user模块对象层设计参考.md`
3. `user模块Mapper能力设计参考.md`
4. `user模块Service流程设计参考.md`
5. `user模块Controller_API设计参考.md`
6. `user模块实现说明.md`

使用方式说明：

- 总览文档：用于快速查看整体设计结论
- 设计文档：用于编码前逐项核对与实现参考
- 实现说明：用于查看当前阶段实际完成结果与模块收尾结论

## 11. 开发顺序

当前阶段推荐严格按以下顺序推进：

1. `Entity / DTO / VO`
   - 完成基础数据结构定义
2. `Mapper`
   - 实现数据库访问能力
3. `Service`
   - 编排业务逻辑与事务
4. `Controller`
   - 对外提供接口
5. `测试`
   - 验证功能完整性与正确性

补充说明：

- 不允许跳过开发顺序
- 禁止直接从 `Controller` 开始开发

## 12. 本阶段结论

当前 `user` 模块已经完成第一阶段的设计收口，包括：

- 模块职责边界明确
- 对象层设计明确
- Mapper 能力设计明确
- Service 流程设计明确
- Controller / API 设计明确

后续进入编码阶段时，应严格以本目录中的整理版文档为统一参考基线。

## 本文件作用

本文档用于快速查看 `user` 模块当前阶段的整体设计结论。
在进入编码阶段后，应优先从本文件开始，再按需跳转到对象层、Mapper、Service 与 Controller 的详细设计文档。
