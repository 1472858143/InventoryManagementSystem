# Inbound 模块实现说明

## 1. 模块概述

`inbound` 模块负责系统中的入库单据管理能力，当前阶段主要承担新增入库记录、查询入库记录以及通过 `stock` 模块完成库存增加等功能。

本模块在系统中的作用是：

- 记录库存增加的业务原因
- 维护入库单据主数据
- 作为库存增加流程的业务入口
- 为后续库存流转链路提供可追溯的入库记录

与其他模块的关系如下：

- `auth`：负责认证与 token 校验
- `user`：负责用户与操作人体系管理
- `product`：负责商品基础数据
- `inbound`：负责入库单据和入库流程编排
- `stock`：负责真实库存增加与库存日志记录
- `outbound / stockcheck`：负责其他库存变化原因与业务单据

## 2. 数据结构说明

当前阶段 `inbound` 模块主要涉及以下数据表：

- `inbound_order`

业务流程上依赖：

- `stock`
- `stock_log`

当前阶段重点使用的字段如下：

### 2.1 inbound_order

- `id`
  - 入库记录主键
- `product_id`
  - 关联商品主键
- `quantity`
  - 本次入库数量
- `operator`
  - 操作人
- `create_time`
  - 入库时间

补充说明：

- 当前入库列表展示时，会最小化关联 `product` 表中的 `product_code`、`product_name`
- `stock` 与 `stock_log` 由 `stock` 模块负责写入，`inbound` 不直接操作这两张表

## 3. 本阶段实现功能

当前阶段已经实现以下功能：

1. 新增入库记录 `createInbound`
2. 查询入库记录列表 `listInbounds`

## 4. 核心实现说明

### 4.1 模块职责边界

- `inbound` 负责记录入库单据
- `inbound` 通过 `stockService.increaseStock(...)` 完成库存增加
- `inbound` 不直接修改 `stock.quantity`
- `inbound` 不直接写入 `stock_log`

### 4.2 新增入库流程

当前阶段新增入库记录采用以下流程：

1. 校验请求参数
2. 校验商品是否存在
3. 写入 `inbound_order`
4. 调用 `stockService.increaseStock(productId, quantity)`
5. 回查新增记录并返回

### 4.3 查询策略

当前阶段入库查询采用以下策略：

- 以 `inbound_order` 为主表查询
- 最小联表 `product` 读取 `productCode`、`productName`
- 不做分页
- 不做筛选
- 不混入库存日志和库存统计信息

### 4.4 事务处理

- `createInbound` 已加事务
- 入库单写入和库存增加处于同一事务中
- 如果库存增加失败，`inbound_order` 不保留

### 4.5 分层职责

- Controller：接收请求、参数校验、返回统一响应
- Service：流程编排、业务校验、事务控制、返回对象组装
- Mapper：`inbound_order` 数据库访问

### 4.6 Mapper 实现方式

- `inbound` 模块 Mapper 最终按项目书采用 MyBatis XML 映射
- 当前已实现：
  - `InboundOrderMapper.xml`

## 5. 接口说明

### 5.1 POST /api/inbounds

- 输入参数：`InboundCreateRequest`
- 返回结构：`ApiResponse<InboundDetailResponse>`
- 是否需要认证：是

### 5.2 GET /api/inbounds

- 输入参数：无
- 返回结构：`ApiResponse<List<InboundListItemResponse>>`
- 是否需要认证：是

## 6. 测试结果总结

当前阶段已确认以下结果：

- 代码实现已完成
- 模块结构检查已完成
- 项目编译验证已通过
- `/api/inbounds` 和 `/api/inbounds/**` 已纳入认证拦截范围
- Postman 接口测试文档模板已补充

基于当前实现，以下场景已经具备明确的代码支持：

- 新增入库记录
- 查询入库记录列表
- 商品不存在校验
- 入库数量非法校验
- 库存记录不存在时事务回滚
- 调用 `stock` 模块增加库存
- 通过 `stock` 模块写入库存日志
- 未登录访问返回 `401`

补充说明：

- 当前阶段已完成代码实现、结构检查与编译验证
- Postman 测试请求内容已经整理完成
- 实际响应结果与测试结论预留在接口测试文档中，待后续联调时填写
- 因此本阶段不将“接口测试完成”标记为已完成

## 7. 已知限制（当前未实现）

当前阶段尚未实现以下内容：

- 入库详情接口
- 入库记录删除
- 入库记录修改
- 分页查询
- 条件筛选查询
- 入库统计报表
- 实际接口测试结果记录

## 8. 当前结论

`inbound` 模块第一阶段开发完成，已具备基础入库管理能力，并已按项目书要求接入 `stock` 模块完成库存增加，可支撑后续库存流转相关模块开发。
