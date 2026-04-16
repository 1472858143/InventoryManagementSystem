# Stockcheck 模块 Service 流程设计参考

## 1. 文档目的

本文档用于明确 `stockcheck` 模块第一阶段 Service 层流程设计，为后续实现 `StockCheckService` 及其实现类提供直接参考。

当前阶段只做流程设计，不写代码。

## 2. 当前设计范围

当前只设计以下接口对应的 Service 流程：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

## 3. 当前阶段不处理什么

当前阶段不纳入 Service 流程设计的内容包括：

- 盘点详情接口
- 盘点记录删除
- 盘点记录修改
- 分页查询
- 条件筛选
- 盘点统计报表
- 库存日志查询

## 4. StockCheckService 方法清单

当前阶段建议提供以下方法：

1. `createStockCheck(StockCheckCreateRequest request)`
   - 返回：`StockCheckDetailResponse`
   - 用途：新增盘点记录，并通过 `stock` 模块完成库存调整

2. `listStockChecks()`
   - 返回：`List<StockCheckListItemResponse>`
   - 用途：查询盘点记录列表

## 5. 新增盘点记录流程设计

### 5.1 流程目标

新增一条盘点记录，记录系统库存、实际库存和差异，并通过 `stock` 模块完成库存调整。

### 5.2 业务流程

1. 接收 `StockCheckCreateRequest`
2. 校验基础参数：
   - `productId` 非空
   - `actualQuantity` 非空
   - `actualQuantity >= 0`
3. 调用 `StockService.getStockByProductId(productId)` 获取当前系统库存
4. 从返回结果中读取 `quantity` 作为 `systemQuantity`
5. 计算差异：
   - `difference = actualQuantity - systemQuantity`
6. 组装 `StockCheck`
7. 调用 `StockCheckMapper.insert(stockCheck)` 保存盘点记录
8. 调用 `stockService.adjustStock(productId, actualQuantity)` 完成库存调整
9. 根据主键回查完整盘点记录
10. 组装 `StockCheckDetailResponse`
11. 返回结果

### 5.3 关键职责边界

- `stockcheck` 负责保存盘点记录
- `stockcheck` 负责计算 `difference`
- `stock` 负责真正的库存调整与库存日志记录
- `stockcheck` 不直接更新库存数量

### 5.4 事务要求

`createStockCheck` 必须加事务。

原因如下：

- 写入 `stock_check` 和调整库存属于一个完整业务动作
- 任一步失败都必须整体回滚
- 库存调整失败时不应保留盘点记录

### 5.5 异常语义建议

至少覆盖：

- 商品ID不能为空
- 实际库存不能为空
- 实际库存不能小于0
- 库存记录不存在
- 盘点记录回查失败

商品是否存在当前可通过库存记录是否存在间接确认；如后续需要更清晰的“商品不存在”语义，可在 Service 中增加 `ProductMapper.findById` 只读校验。

## 6. 查询盘点列表流程设计

### 6.1 流程目标

查询当前阶段的盘点记录列表。

### 6.2 业务流程

1. 调用 `StockCheckMapper.findAll()`
2. 如果结果为空，直接返回空列表
3. 遍历查询结果
4. 组装 `List<StockCheckListItemResponse>`
5. 返回结果

### 6.3 查询原则

- 当前阶段不做分页
- 当前阶段不做筛选
- 当前阶段只返回盘点记录及最小商品信息
- 不混入库存日志
- 不返回报表统计信息

### 6.4 事务要求

`listStockChecks` 不需要事务。

## 7. 业务规则与校验点总结

当前阶段 Service 层至少需要保证以下规则：

1. `productId` 必须非空
2. `actualQuantity` 必须非空
3. `actualQuantity >= 0`
4. 系统库存必须能从 `stock` 模块读取
5. `difference = actualQuantity - systemQuantity`
6. 盘点记录写入与库存调整必须同事务提交
7. `stockcheck` 不直接改库存表

规则分类如下：

### 7.1 参数校验

- `productId` 非空
- `actualQuantity` 非空
- `actualQuantity >= 0`

### 7.2 业务校验

- 库存记录存在
- 系统库存读取成功
- 差异计算正确
- 库存调整由 `stock` 执行

## 8. 事务边界总结

### 8.1 createStockCheck

- 必须使用事务
- `stock_check` 保存与库存调整必须在同一事务中完成

### 8.2 listStockChecks

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
- 查询系统库存
- 计算库存差异
- 盘点记录组装
- 调用 `stock` 完成库存调整
- VO 组装

### 9.3 Mapper 负责

- `stock_check` 数据库访问

### 9.4 stock 模块负责

- 真实库存调整
- 库存规则校验
- 库存日志写入

## 10. 本阶段结论

`stockcheck` 模块第一阶段 Service 流程已经明确。

后续编码时必须严格坚持以下原则：

- `stockcheck` 记录盘点差异与原因
- `stock` 修改库存与记录库存日志
- 盘点记录写入和库存调整通过 Service 编排实现同一事务一致性
