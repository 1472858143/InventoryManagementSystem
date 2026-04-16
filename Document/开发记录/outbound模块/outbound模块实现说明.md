# Outbound 模块实现说明

## 1. 模块概述

`outbound` 模块负责系统中的出库单据管理能力，当前阶段主要承担新增出库记录、查询出库记录列表，以及通过 `stock` 模块完成库存扣减等功能。

本模块在系统中的作用是：

- 记录库存减少的业务原因
- 维护出库单据主数据
- 作为库存减少流程的业务入口
- 为后续库存流转、库存追溯和出库统计提供基础记录

与其他模块的关系如下：

- `auth`：负责认证与 token 校验
- `user`：负责用户与操作人体系管理
- `product`：负责商品基础数据
- `outbound`：负责出库单据和出库流程编排
- `stock`：负责真实库存扣减、库存不足校验与库存日志记录
- `inbound / stockcheck`：负责其他库存变化原因与业务单据

## 2. 数据结构说明

当前阶段 `outbound` 模块主要涉及以下数据表：

- `outbound_order`

业务流程上依赖：

- `stock`
- `stock_log`

当前阶段重点使用的字段如下：

### 2.1 outbound_order

- `id`
  - 出库记录主键
- `product_id`
  - 关联商品主键
- `quantity`
  - 本次出库数量
- `operator`
  - 操作人
- `create_time`
  - 出库时间

补充说明：

- 当前出库列表展示时，会最小化关联 `product` 表中的 `product_code`、`product_name`
- `stock` 与 `stock_log` 由 `stock` 模块负责写入，`outbound` 不直接操作这两张表

## 3. 本阶段实现功能

当前阶段已经实现以下功能：

1. 新增出库记录 `createOutbound`
2. 查询出库记录列表 `listOutbounds`

## 4. 核心实现说明

### 4.1 模块职责边界

- `outbound` 负责记录出库单据
- `outbound` 通过 `stockService.decreaseStock(...)` 完成库存扣减
- `outbound` 不直接修改 `stock.quantity`
- `outbound` 不直接写入 `stock_log`
- 库存不足校验由 `stock` 模块统一兜底

### 4.2 新增出库流程

当前阶段新增出库记录采用以下流程：

1. 校验请求参数
2. 校验商品是否存在
3. 写入 `outbound_order`
4. 调用 `stockService.decreaseStock(productId, quantity)`
5. 由 `stock` 模块完成库存扣减与 `stock_log` 写入
6. 回查新增记录并返回

如果库存扣减失败，例如库存记录不存在或库存不足，则整个事务回滚，已写入的 `outbound_order` 不会保留。

### 4.3 查询策略

当前阶段出库查询采用以下策略：

- 以 `outbound_order` 为主表查询
- 最小联表 `product` 读取 `productCode`、`productName`
- 不做分页
- 不做筛选
- 不混入库存日志和库存统计信息
- 不返回当前库存数量作为出库记录主响应字段

### 4.4 事务处理

- `createOutbound` 已加事务
- 出库单写入和库存扣减处于同一事务中
- 如果库存扣减失败，`outbound_order` 不保留
- `listOutbounds` 不需要事务

### 4.5 分层职责

- Controller：接收请求、参数校验、返回统一响应
- Service：流程编排、业务校验、事务控制、返回对象组装
- Mapper：`outbound_order` 数据库访问
- Stock Service：真实库存扣减、库存不足校验、库存日志写入

### 4.6 Mapper 实现方式

- `outbound` 模块 Mapper 按项目书采用 MyBatis XML 映射
- 当前已实现：
  - `OutboundOrderMapper.xml`

## 5. 接口说明

### 5.1 POST /api/outbounds

- 输入参数：`OutboundCreateRequest`
- 返回结构：`ApiResponse<OutboundDetailResponse>`
- 是否需要认证：是
- 作用：新增出库记录，并通过 `stock` 模块扣减库存

### 5.2 GET /api/outbounds

- 输入参数：无
- 返回结构：`ApiResponse<List<OutboundListItemResponse>>`
- 是否需要认证：是
- 作用：查询出库记录列表

## 6. 测试结果总结

当前阶段已确认以下结果：

- 代码实现已完成
- 模块结构检查已完成
- 项目编译验证已通过
- `/api/outbounds` 和 `/api/outbounds/**` 已纳入认证拦截范围
- Postman 接口测试文档模板已补充
- 全局参数异常 message 已支持返回 DTO 校验中的具体错误信息

基于当前实现，以下场景已经具备明确的代码支持：

- 新增出库记录
- 查询出库记录列表
- 商品不存在校验
- 出库数量非法校验
- 库存记录不存在时事务回滚
- 库存不足时事务回滚
- 调用 `stock` 模块扣减库存
- 通过 `stock` 模块写入库存日志
- 未登录访问返回 `401`

补充说明：

- 当前阶段已完成代码实现、结构检查与编译验证
- Postman 测试请求内容已经整理完成
- 实际响应结果与测试结论预留在接口测试文档中，供后续联调时填写
- 如实际 Postman 响应尚未完整记录，不应将“接口测试完成”标记为已完成

## 7. 已知限制（当前未实现）

当前阶段尚未实现以下内容：

- 出库详情接口
- 出库记录删除
- 出库记录修改
- 分页查询
- 条件筛选查询
- 出库统计报表
- 对外库存调整接口
- 实际接口测试结果记录

## 8. 当前结论

`outbound` 模块第一阶段代码开发完成，已具备基础出库管理能力，并已按项目书要求通过 `stock` 模块完成库存扣减、库存不足校验与库存日志记录，可支撑后续 `stockcheck / report` 等模块开发。
