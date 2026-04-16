# Outbound 模块 Service 流程设计参考

## 1. 文档目的

本文档用于明确 `outbound` 模块当前阶段 Service 层流程设计，为后续实现 `OutboundService` 及其实现类提供直接参考。

当前阶段只做流程设计，不写代码。

## 2. 当前设计范围

当前只设计以下接口对应的 Service 流程：

1. `POST /api/outbounds`
2. `GET /api/outbounds`

## 3. 当前阶段不处理什么

当前阶段不纳入以下 Service 流程设计：

- 出库详情接口
- 出库删除
- 出库修改
- 分页查询
- 条件筛选
- 报表统计

## 4. OutboundService 方法清单

当前阶段建议提供以下方法：

1. `createOutbound(OutboundCreateRequest request)`
   - 返回：`OutboundDetailResponse`
2. `listOutbounds()`
   - 返回：`List<OutboundListItemResponse>`

## 5. 新增出库记录（createOutbound）流程设计

### 5.1 流程目标

新增一条出库记录，并通过 `stock` 模块完成库存扣减。

### 5.2 业务流程

1. 接收 `OutboundCreateRequest`
2. 校验基础参数：
   - `productId` 非空
   - `quantity` 非空
   - `operator` 非空
3. 校验业务规则：
   - `quantity > 0`
   - 商品存在
   - 库存记录存在
   - 库存充足
4. 组装 `OutboundOrder`
5. 调用 `OutboundOrderMapper.insert(outboundOrder)` 保存出库记录
6. 调用 `stockService.decreaseStock(productId, quantity)` 完成库存扣减
7. 如有必要，根据主键查询完整出库记录
8. 组装 `OutboundDetailResponse`
9. 返回结果

### 5.3 关键职责边界

- `outbound` 负责保存出库单据
- `stock` 负责真正的库存扣减、库存不足校验与库存日志记录
- `outbound` 不直接更新库存数量

### 5.4 事务要求

`createOutbound` 必须加事务。

原因如下：

- 写入 `outbound_order` 和扣减库存属于一个完整业务动作
- 任一步失败都必须整体回滚
- 库存不足时通过事务回滚保证不保留出库记录

### 5.5 异常语义建议

至少覆盖：

- 商品不存在
- 出库数量非法
- 库存记录不存在
- 库存不足

## 6. 查询出库列表（listOutbounds）流程设计

### 6.1 流程目标

查询当前阶段的出库记录列表。

### 6.2 业务流程

1. 调用 `OutboundOrderMapper.findAll()`
2. 如果结果为空，直接返回空列表
3. 遍历查询结果
4. 组装 `List<OutboundListItemResponse>`
5. 返回结果

### 6.3 查询原则

- 当前阶段不做分页
- 当前阶段不做筛选
- 当前阶段只返回出库记录及最小商品信息
- 不混入库存日志
- 不返回库存数量作为主展示内容

### 6.4 事务要求

`listOutbounds` 不需要事务。

## 7. 业务规则与校验点总结

当前阶段 Service 层至少需要保证以下规则：

1. 出库数量必须大于 `0`
2. 商品必须存在
3. 库存不足时必须拒绝出库
4. 出库记录写入与库存扣减必须同事务提交
5. `outbound` 不直接改库存表

规则分类如下：

### 7.1 参数校验

- `productId` 非空
- `quantity` 非空
- `operator` 非空

### 7.2 业务校验

- `quantity > 0`
- 商品存在
- 库存记录存在
- 库存充足
- 库存扣减由 `stock` 执行

## 8. 事务边界总结

### 8.1 createOutbound

- 必须使用事务
- `outbound_order` 保存与库存扣减必须在同一事务中完成

### 8.2 listOutbounds

- 不需要事务

## 9. 职责边界总结

### 9.1 Controller 负责

- 接收请求
- 参数校验
- 调用 Service
- 返回统一响应

### 9.2 Service 负责

- 业务流程编排
- 事务控制
- 出库单据组装
- 调用 `stock` 完成库存扣减
- VO 组装

### 9.3 Mapper 负责

- `outbound_order` 数据库访问

### 9.4 stock 模块负责

- 真正的库存扣减
- 库存不足校验
- 库存规则校验
- 库存日志写入

## 10. 本阶段结论

`outbound` 模块第一阶段 Service 流程已经明确。

后续编码时必须严格坚持以下原则：

- `outbound` 记录原因与单据
- `stock` 修改库存与记录库存日志
- 库存不足时拒绝出库
- 两者通过 Service 编排实现同一事务一致性
