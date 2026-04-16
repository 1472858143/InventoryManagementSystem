# Stockcheck 模块项目书对照校验

## 1. 文档目的

本文档用于对照项目书核验 `stockcheck` 模块第一阶段设计结论，确保当前设计与项目书要求一致。

如项目书与开发记录文档之间出现冲突，以项目书为最高优先级依据。

## 2. 校验范围

本次校验覆盖以下方面：

1. 模块职责
2. 模块结构
3. 关联数据表
4. API 范围
5. 业务流程
6. 差异计算规则
7. 事务一致性
8. 与 `stock` 模块的职责边界
9. 当前阶段未纳入范围

## 3. 模块职责校验

项目书要求：

- 记录系统库存与实际库存
- 计算库存差异
- 保存盘点记录
- 调用库存模块执行库存调整

文档设计结论：

- `stockcheck` 负责记录盘点结果、计算差异、保存 `stock_check`
- `stockcheck` 通过 `StockService.adjustStock(...)` 调整库存

是否一致：

- 一致

备注：

- 当前设计没有把库存调整实现放到 `stockcheck`，符合项目书职责边界

## 4. 模块结构校验

项目书要求：

- `stockcheck` 模块采用常规分层
- 包结构包括 `controller / service / mapper / entity / dto / vo / enums`
- 大多数模块不需要复杂 DDD 拆分

文档设计结论：

- 当前设计只保留 `controller / service / mapper / entity / dto / vo / enums`
- 当前阶段不新增 `domain`

是否一致：

- 一致

备注：

- `domain` 只在 `stock` 模块中承载库存核心规则，`stockcheck` 不重复拆分

## 5. 关联数据表校验

项目书要求：

- 盘点模块关联：
  - `stock_check`
  - `stock`
  - `stock_log`

文档设计结论：

- `stockcheck` 直接写 `stock_check`
- `stock` 与 `stock_log` 通过 `stock` 模块间接处理

是否一致：

- 一致

备注：

- 设计明确 `stockcheck` 不直接写 `stock` 与 `stock_log`

## 6. 数据字段校验

项目书要求：

`stock_check` 字段包括：

- `id`
- `product_id`
- `system_quantity`
- `actual_quantity`
- `difference`
- `check_time`

文档设计结论：

`StockCheck` Entity 字段设计为：

- `id`
- `productId`
- `systemQuantity`
- `actualQuantity`
- `difference`
- `checkTime`

是否一致：

- 一致

备注：

- Java 字段使用驼峰命名，数据库字段使用下划线命名

## 7. API 范围校验

项目书要求：

- `POST /api/stockchecks`
- `GET /api/stockchecks`

文档设计结论：

- 当前阶段只设计 `POST /api/stockchecks`
- 当前阶段只设计 `GET /api/stockchecks`

是否一致：

- 一致

备注：

- 未扩展 `GET /api/stockchecks/{id}`、删除、修改、分页、筛选接口

## 8. 新增盘点流程校验

项目书要求：

业务流程为：

1. 提交盘点请求
2. 查询当前 `stock`
3. 计算 `difference`
4. 保存 `stock_check`
5. 调用库存模块调整库存
6. 更新 `stock`
7. 写入 `stock_log`
8. 事务提交

文档设计结论：

`createStockCheck` 流程为：

- 接收请求并校验参数
- 调用 `StockService.getStockByProductId(productId)` 获取系统库存
- 计算 `difference = actualQuantity - systemQuantity`
- 保存 `stock_check`
- 调用 `stockService.adjustStock(productId, actualQuantity)`
- 同一事务提交

是否一致：

- 一致

备注：

- `stock` 更新与 `stock_log` 写入由 `StockService.adjustStock(...)` 完成

## 9. 差异计算规则校验

项目书要求：

- `difference` 表示实际库存与系统库存差异

文档设计结论：

- `difference = actualQuantity - systemQuantity`

是否一致：

- 一致

备注：

- 该规则由 Service 层统一计算，前端不传入 `difference`

## 10. 事务一致性校验

项目书要求：

- 入库、出库、盘点等写操作应保证事务一致性
- 盘点记录写入与库存调整应作为一个完整业务动作

文档设计结论：

- `createStockCheck` 必须使用事务
- `stock_check` 保存与库存调整必须同事务提交
- 若库存调整失败，盘点记录整体回滚

是否一致：

- 一致

备注：

- 查询列表不需要事务

## 11. 与 stock 模块职责边界校验

项目书要求：

- `stock` 是唯一允许直接修改库存表的模块
- 入库、出库、盘点只能通过 `stock` 模块修改库存

文档设计结论：

- `stockcheck` 不直接修改 `stock.quantity`
- `stockcheck` 不直接写 `stock_log`
- `stockcheck` 通过 `stockService.adjustStock(...)` 调整库存

是否一致：

- 一致

备注：

- 当前设计没有让 `StockCheckMapper` 操作 `stock` 或 `stock_log`

## 12. 当前阶段未纳入范围校验

项目书要求：

- 第一阶段仅要求新增盘点记录与查询盘点记录

文档设计结论：

当前阶段不纳入：

- 盘点详情接口
- 删除盘点记录
- 修改盘点记录
- 分页查询
- 条件筛选查询
- 盘点统计报表
- 对外库存调整接口

是否一致：

- 一致

备注：

- 当前设计未提前扩展范围外功能

## 13. 统一结论

经对照项目书，当前 `stockcheck` 模块第一阶段设计与项目书保持一致。

当前设计结论如下：

- 模块职责一致
- 模块结构一致
- 数据表字段一致
- API 范围一致
- 业务流程一致
- 差异计算规则一致
- 事务边界一致
- 与 `stock` 模块职责边界一致

当前未发现与项目书冲突的设计项。

后续编码实现时应严格以本文档和项目书为依据推进。
