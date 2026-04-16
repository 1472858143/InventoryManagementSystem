# Stockcheck 模块实现说明

## 1. 模块概述

`stockcheck` 模块负责系统中的库存盘点业务，当前阶段主要承担新增盘点记录、查询盘点记录列表、读取系统库存、计算库存差异，并通过 `stock` 模块完成真实库存调整。

本模块在系统中的作用是：

- 记录盘点时的系统库存与实际库存
- 计算并保存库存差异
- 作为库存校正流程的业务入口
- 为后续库存追溯、盘点管理和统计分析提供基础记录

与其他模块的关系如下：

- `auth`：负责认证与 token 校验
- `user`：负责用户管理
- `product`：负责商品基础数据
- `stock`：负责真实库存调整与库存日志写入
- `stockcheck`：负责盘点记录、差异计算与库存调整流程编排
- `inbound / outbound`：负责入库、出库等其他库存变化原因

## 2. 数据结构说明

当前阶段 `stockcheck` 模块直接涉及的主表为：

- `stock_check`

业务流程中依赖：

- `stock`
- `stock_log`

其中 `stock` 和 `stock_log` 由 `stock` 模块负责维护，`stockcheck` 模块不直接写入这两张表。

### 2.1 stock_check

当前阶段使用的关键字段如下：

- `id`
  - 盘点记录主键
- `product_id`
  - 被盘点商品 ID
- `system_quantity`
  - 盘点前系统记录的库存数量
- `actual_quantity`
  - 盘点得到的实际库存数量
- `difference`
  - 库存差异，计算规则为 `actualQuantity - systemQuantity`
- `check_time`
  - 盘点记录创建时间

补充说明：

- 查询盘点列表和新增盘点回显时，会最小化关联 `product` 表读取 `product_code`、`product_name`
- `stock.quantity` 的真实调整由 `stockService.adjustStock(...)` 完成
- `stock_log` 的 `CHECK` 类型日志由 `stock` 模块写入

## 3. 本阶段实现功能

当前阶段已经实现以下功能：

1. 新增盘点记录 `createStockCheck`
2. 查询盘点记录列表 `listStockChecks`

## 4. 核心实现说明

### 4.1 模块职责边界

- `stockcheck` 负责保存盘点记录
- `stockcheck` 负责读取当前系统库存并计算库存差异
- `stockcheck` 通过 `stockService.adjustStock(...)` 调整库存
- `stockcheck` 不直接修改 `stock.quantity`
- `stockcheck` 不直接写入 `stock_log`
- 库存合法性与库存日志记录由 `stock` 模块统一负责

### 4.2 新增盘点流程

当前阶段新增盘点记录采用以下流程：

1. 接收 `StockCheckCreateRequest`
2. 校验 `productId` 非空
3. 校验 `actualQuantity` 非空且不能小于 0
4. 调用 `stockService.getStockByProductId(productId)` 获取当前系统库存
5. 将当前库存数量作为 `systemQuantity`
6. 计算 `difference = actualQuantity - systemQuantity`
7. 写入 `stock_check`
8. 调用 `stockService.adjustStock(productId, actualQuantity)` 调整真实库存
9. 由 `stock` 模块写入 `CHECK` 类型库存日志
10. 回查新增盘点记录并返回

如果库存记录不存在、库存调整失败或盘点记录回查失败，则整个事务回滚，已写入的 `stock_check` 不会保留。

### 4.3 查询策略

当前阶段盘点查询采用以下策略：

- 以 `stock_check` 为主表查询
- 最小联表 `product` 读取 `productCode`、`productName`
- 不做分页
- 不做条件筛选
- 不返回库存日志明细
- 不返回报表统计信息
- 不混入 `stock` 完整详情对象

### 4.4 事务处理

- `createStockCheck` 已加事务
- `stock_check` 写入与库存调整处于同一事务
- 库存调整失败时，盘点记录整体回滚
- `listStockChecks` 不需要事务

### 4.5 分层职责

- Controller：接收请求、参数校验、调用 Service、返回统一响应
- Service：流程编排、业务校验、库存读取、差异计算、事务控制、VO 组装
- Mapper：`stock_check` 数据库访问
- Stock Service：真实库存调整、库存合法性校验、库存日志写入

### 4.6 Mapper 实现方式

- `stockcheck` 模块 Mapper 按项目书采用 MyBatis XML 映射
- 当前已实现：
  - `StockCheckMapper.xml`

## 5. 接口说明

### 5.1 POST /api/stockchecks

- 输入参数：`StockCheckCreateRequest`
- 返回结构：`ApiResponse<StockCheckDetailResponse>`
- 是否需要认证：是
- 作用：新增盘点记录，并通过 `stock` 模块调整库存为实际盘点数量

请求字段：

- `productId`
- `actualQuantity`

返回字段：

- `id`
- `productId`
- `productCode`
- `productName`
- `systemQuantity`
- `actualQuantity`
- `difference`
- `checkTime`

### 5.2 GET /api/stockchecks

- 输入参数：无
- 返回结构：`ApiResponse<List<StockCheckListItemResponse>>`
- 是否需要认证：是
- 作用：查询盘点记录列表

## 6. 测试结果总结

当前阶段已确认以下结果：

- 代码实现已完成
- 模块结构检查已完成
- 项目编译验证已通过
- `/api/stockchecks` 和 `/api/stockchecks/**` 已纳入认证拦截范围
- Postman 接口测试已通过
- 查询盘点列表成功
- 新增盘点记录成功
- 新增盘点后库存数量已正确调整为 `actualQuantity`
- 缺少 `productId` 返回 `400`
- 缺少 `actualQuantity` 返回 `400`
- `actualQuantity < 0` 返回 `400`
- 库存记录不存在时返回业务异常
- 未登录访问返回 `401`
- JSON 参数格式错误返回 `400`

补充说明：

- 当前阶段已完成代码实现、结构检查、编译验证与 Postman 手工接口测试
- 真实响应正文可继续补充到 `stockcheck模块接口测试文档.md` 的响应结果记录位中

## 7. 已知限制（当前未实现）

当前阶段尚未实现以下内容：

- 盘点详情接口
- 盘点记录删除
- 盘点记录修改
- 分页查询
- 条件筛选查询
- 盘点统计报表
- 对外库存调整接口
- 库存日志查询接口

## 8. 当前结论

`stockcheck` 模块第一阶段代码开发完成，已具备基础库存盘点能力，并已按项目书要求通过 `stock` 模块完成真实库存调整与库存日志记录。

当前模块已通过 Postman 手工接口测试，可作为后续前端联调、报表模块和系统收尾工作的后端基础。
