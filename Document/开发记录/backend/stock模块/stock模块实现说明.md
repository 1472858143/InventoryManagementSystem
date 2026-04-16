# Stock 模块实现说明

## 1. 模块概述

`stock` 模块负责系统中的库存主线能力，当前阶段主要承担库存查询、库存上下限维护、统一库存变更控制和库存日志记录等功能。

本模块在系统中的作用是：

- 维护商品当前库存状态
- 维护库存上下限字段
- 统一承接库存增减与盘点调整逻辑
- 为后续入库、出库、盘点模块提供统一库存变更入口

与其他模块的关系如下：

- `auth`：负责认证与 token 校验
- `user`：负责用户与操作人体系管理
- `product`：负责商品基础数据，不负责库存数量
- `stock`：负责当前库存、库存规则、库存日志
- `inbound / outbound / stockcheck`：负责库存变化原因与业务单据，后续通过 `stock` 完成库存变更

## 2. 数据结构说明

当前阶段 `stock` 模块主要涉及以下数据表：

- `stock`
- `stock_log`

当前阶段重点使用的字段如下：

### 2.1 stock

- `product_id`
  - 关联商品主键，用于建立商品与库存记录的一一对应关系
- `quantity`
  - 当前库存数量
- `min_stock`
  - 库存下限，用于库存阈值维护
- `max_stock`
  - 库存上限，用于库存阈值维护
- `update_time`
  - 库存最近更新时间

### 2.2 stock_log

- `product_id`
  - 关联发生库存变更的商品
- `change_type`
  - 库存变更类型，当前实现使用 `INBOUND / OUTBOUND / CHECK`
- `change_quantity`
  - 本次变更数量
- `before_quantity`
  - 变更前库存
- `after_quantity`
  - 变更后库存
- `create_time`
  - 日志创建时间

补充说明：

- 当前库存列表和详情展示时，会最小化关联 `product` 表中的 `product_code`、`product_name`
- 该关联仅用于库存展示，不改变 `product` 与 `stock` 的模块职责边界

## 3. 本阶段实现功能

当前阶段已经实现以下功能：

1. 查询库存列表 `listStocks`
2. 查询单个商品当前库存 `getStockByProductId`
3. 维护库存上下限 `updateStockLimit`
4. 内部统一库存增加 `increaseStock`
5. 内部统一库存扣减 `decreaseStock`
6. 内部统一库存盘点调整 `adjustStock`

## 4. 核心实现说明

### 4.1 模块职责边界

- `stock` 是当前系统中唯一允许直接修改库存表的模块
- `product` 不直接维护库存数量
- 后续 `inbound / outbound / stockcheck` 只能通过 `stock` 的内部能力修改库存

### 4.2 查询策略

当前阶段库存查询采用以下策略：

- 对外接口只开放库存列表与单商品库存查询
- 查询时允许最小联表读取商品编码和商品名称
- 不做分页
- 不做复杂筛选
- 不混入入库、出库、盘点统计信息

### 4.3 库存上下限处理

- 当前阶段公开接口只允许维护 `minStock`、`maxStock`
- `quantity` 不通过外部 HTTP 接口直接修改
- `maxStock >= minStock` 由 `domain + service` 统一校验

### 4.4 统一库存变更逻辑

当前阶段虽然没有开放外部库存数量调整接口，但模块内部已经完成统一库存变更能力：

- `increaseStock`
- `decreaseStock`
- `adjustStock`

相关规则由 `StockDomainService` 统一处理，包括：

- 库存数量不得为负
- 变更数量必须合法
- 盘点调整按实际数量覆盖

### 4.5 库存日志记录

- 每次内部库存变更后，都会写入 `stock_log`
- 日志字段包含变更类型、变更数量、变更前库存和变更后库存
- 当前日志类型口径与项目书保持一致：`INBOUND / OUTBOUND / CHECK`

### 4.6 分层职责

- Controller：接收请求、参数校验、返回统一响应
- Service：流程编排、业务校验、事务控制、返回对象组装
- Domain：库存核心规则封装与库存变更结果计算
- Mapper：数据库访问

### 4.7 Mapper 实现方式

- `stock` 模块 Mapper 最终按项目书采用 MyBatis XML 映射
- 当前已实现：
  - `StockMapper.xml`
  - `StockLogMapper.xml`

## 5. 接口说明

### 5.1 GET /api/stocks

- 输入参数：无
- 返回结构：`ApiResponse<List<StockListItemResponse>>`
- 是否需要认证：是

### 5.2 GET /api/stocks/{productId}

- 输入参数：路径参数 `productId`
- 返回结构：`ApiResponse<StockDetailResponse>`
- 是否需要认证：是

### 5.3 PUT /api/stocks/{productId}/limit

- 输入参数：路径参数 `productId` + 请求体 `StockLimitUpdateRequest`
- 返回结构：`ApiResponse<Void>`
- 是否需要认证：是

## 6. 测试结果总结

当前阶段已确认以下结果：

- 代码实现已完成
- 模块结构检查已完成
- 项目编译验证已通过
- `/api/stocks` 和 `/api/stocks/**` 已纳入认证拦截范围
- Postman 接口测试文档模板已补充

基于当前实现，以下场景已经具备明确的代码支持：

- 库存列表查询
- 单个商品库存查询
- 库存上下限更新
- 非法上下限校验
- 库存记录不存在校验
- 统一库存变更逻辑
- 库存日志写入
- 未登录访问返回 `401`

补充说明：

- 当前阶段已完成代码实现、结构检查与编译验证
- Postman 测试请求内容已经整理完成
- 实际响应结果与测试结论预留在接口测试文档中，待后续联调时填写

## 7. 已知限制（当前未实现）

当前阶段尚未实现以下内容：

- 对外库存增减接口
- 入库单处理
- 出库单处理
- 盘点单处理
- 批量库存调整
- 分页查询
- 条件筛选查询
- 对外库存日志查询
- 库存统计报表

## 8. 当前结论

`stock` 模块第一阶段开发完成，已具备基础库存管理能力，可支撑后续入库、出库、盘点等库存相关模块开发。
