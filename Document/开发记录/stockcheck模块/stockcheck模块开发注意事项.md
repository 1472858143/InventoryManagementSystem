# Stockcheck 模块开发注意事项

## 1. 文档目的

本文档用于在 `stockcheck` 模块正式编码前，统一确认模块职责、数据库依赖、字段约束、分层结构、开发顺序与测试准备要求。

当前阶段不直接开始编码，而是先把盘点模块与 `stock` 模块的职责边界、事务边界和库存调整规则定清楚，避免后续出现“盘点模块直接修改库存表”或“库存日志由盘点模块直接写入”等违背项目书的实现。

## 2. 模块职责与边界

`stockcheck` 模块当前负责：

- 记录盘点结果
- 记录系统库存与实际库存
- 计算库存差异
- 保存盘点记录
- 调用 `stock` 模块执行库存调整

`stockcheck` 模块当前不负责：

- 商品基础资料维护
- 登录认证与 token 处理
- 用户管理
- 入库业务
- 出库业务
- 直接修改库存表
- 直接写入库存日志
- 报表统计

职责边界说明：

- `product` 管“盘点的是什么商品”
- `stock` 管“当前库存状态如何调整”
- `stockcheck` 管“发现了什么库存差异，以及为什么需要调整”
- `stock_log` 由 `stock` 模块在库存调整时统一记录
- `auth / user` 管“谁在操作系统”

必须强调：

- `stockcheck` 不允许直接修改 `stock.quantity`
- `stockcheck` 不允许绕过 `stock` 直接写 `stock_log`
- `stockcheck` 只能通过 `StockService.adjustStock(...)` 调整库存
- `stockcheck` 是库存变化原因模块，不是库存状态维护模块

## 3. 数据库结构确认

根据项目书和当前数据库结构，`stockcheck` 模块第一阶段主要涉及以下表：

- `stock_check`

业务上依赖：

- `stock`
- `stock_log`

但必须明确：

- `stockcheck` 自己只直接写 `stock_check`
- `stock` 与 `stock_log` 的写入由 `stock` 模块负责

### 3.1 stock_check 表

当前 `stock_check` 表字段如下：

- `id`
- `product_id`
- `system_quantity`
- `actual_quantity`
- `difference`
- `check_time`

关键约束如下：

1. `product_id` 非空，且必须关联合法商品
2. `system_quantity` 非空，且不能小于 `0`
3. `actual_quantity` 非空，且不能小于 `0`
4. `difference` 由业务层计算，规则为 `actualQuantity - systemQuantity`
5. `check_time` 为盘点时间，由数据库默认生成

开发要求：

- 所有设计必须以当前数据库结构和项目书为准
- 不允许擅自扩表
- 不允许擅自变更字段含义

## 4. 第一批接口范围

根据项目书，`stockcheck` 模块第一阶段只纳入以下接口：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

当前阶段暂不纳入：

- 盘点详情接口
- 删除盘点记录
- 修改盘点记录
- 分页查询
- 条件筛选查询
- 盘点统计报表
- 对外库存调整接口

这样限制范围的原因是：

- 项目书当前只明确要求新增盘点记录和查询盘点记录
- 盘点模块当前阶段重点是“记录差异 + 调用库存模块调整库存”
- 不提前扩展统计、分页、筛选等附加能力

## 5. 对象设计要求

当前阶段后续至少应准备以下对象：

### 5.1 Entity

- `StockCheck`

### 5.2 DTO

- `StockCheckCreateRequest`

### 5.3 VO

- `StockCheckListItemResponse`
- `StockCheckDetailResponse`

### 5.4 Enums

- 保留 `enums/` 目录
- 当前阶段不主动创造项目书未定义的业务枚举类

对象层约束如下：

1. Entity 只用于数据库映射
2. DTO 只用于接收请求
3. VO 只用于返回前端
4. 不允许直接返回 Entity
5. 不要把 `stock_log` 内容混入 `stockcheck` VO
6. 不要把库存调整请求对象暴露为单独的外部接口对象

## 6. 模块内各层职责

根据项目书，`stockcheck` 模块只保留以下常规分层：

- `controller`
- `service`
- `mapper`
- `entity`
- `dto`
- `vo`
- `enums`

必须强调：

- `stockcheck` 模块当前阶段不新增 `domain`
- 库存调整规则集中在 `stock` 模块中实现

### 6.1 controller

- 接收请求
- 参数校验
- 调用 Service
- 返回统一响应
- 不解析 token
- 不直接访问 Mapper
- 不处理库存调整规则

### 6.2 service

- 业务流程编排
- 事务控制
- 查询当前系统库存
- 计算盘点差异
- 组装盘点记录
- 调用 `stock` 完成库存调整
- 组装返回对象

### 6.3 mapper

- 只负责 `stock_check` 数据库访问
- 不负责库存调整
- 不负责库存日志写入
- 不负责库存差异之外的业务规则判断
- 最终按项目书采用 MyBatis XML 映射文件实现

### 6.4 entity / dto / vo / enums

- `entity`：表映射对象
- `dto`：请求对象
- `vo`：返回对象
- `enums`：保留目录，后续如项目书需要再扩展

## 7. 业务规则与字段约束

根据项目书和数据库约束，`stockcheck` 模块当前阶段需要重点遵守以下规则：

1. `productId` 必须非空
2. 商品必须存在
3. 库存记录必须存在
4. `actualQuantity` 必须非空
5. `actualQuantity >= 0`
6. `systemQuantity` 来自当前 `stock.quantity`
7. `difference = actualQuantity - systemQuantity`
8. 保存盘点记录后，必须通过 `stock` 模块调整库存
9. 盘点记录写入与库存调整必须保持事务一致性
10. `stockcheck` 不允许直接写库存表

规则分类说明：

### 7.1 参数校验

- `productId` 非空
- `actualQuantity` 非空
- `actualQuantity >= 0`

### 7.2 业务校验

- 商品是否存在
- 库存记录是否存在
- 系统库存读取是否成功
- 差异计算是否正确
- 盘点记录与库存调整是否处于同一事务

结论说明：

- DTO 层负责基础参数校验
- Service 层负责流程编排与业务规则兜底
- 库存调整最终由 `stock` 模块统一执行

## 8. Service 流程准备要求

当前阶段只做流程准备说明，不展开具体代码实现。

对于“新增盘点记录”，后续流程应至少包括：

1. 接收盘点请求
2. 校验参数
3. 校验商品与库存前置条件
4. 调用 `StockService.getStockByProductId(productId)` 获取系统库存
5. 计算 `difference = actualQuantity - systemQuantity`
6. 保存 `stock_check`
7. 调用 `stockService.adjustStock(productId, actualQuantity)` 调整库存
8. 事务提交

对于“查询盘点记录”，后续流程应至少包括：

1. 查询盘点记录列表
2. 组装返回对象
3. 返回结果

必须强调：

- 盘点模块负责记录差异和原因
- 库存实际调整由 `stock` 模块完成
- 库存调整失败时整个盘点流程失败

## 9. 事务边界初步判断

对当前第一批接口的事务边界，初步判断如下：

- 新增盘点记录：必须使用事务
- 查询盘点记录：不需要事务

事务原因说明：

- `stock_check` 保存和库存调整属于一个完整业务动作
- 任一步失败都不能留下部分成功的数据
- 库存调整失败时不应保留盘点记录

## 10. 查询返回字段约束

当前阶段 `stockcheck` 模块返回字段应聚焦盘点记录本身。

建议优先返回：

- `id`
- `productId`
- `productCode`
- `productName`
- `systemQuantity`
- `actualQuantity`
- `difference`
- `checkTime`

必须强调：

- 不返回库存日志明细
- 不返回入库、出库统计
- 不返回报表聚合数据
- 不把 `stock` 完整详情直接塞进 `stockcheck` 响应

## 11. 测试数据准备

在 `stockcheck` 模块正式测试前，建议准备以下数据：

1. 已存在的商品数据
2. 已存在的库存记录
3. 有效登录账号与 token
4. 可用于正常盘点的商品 ID
5. 可用于库存记录不存在测试的商品 ID
6. 可用于非法实际库存测试的数据样例

当前阶段测试准备应与第一批范围匹配：

- 不提前要求入库单数据
- 不提前要求出库单数据
- 不提前要求报表数据

## 12. 开发顺序要求

`stockcheck` 模块必须按以下顺序推进：

1. 先写开发注意事项
2. 再做对象层设计
3. 再做 Mapper 能力设计
4. 再做 Service 流程设计
5. 再做 Controller / API 设计
6. 再做设计总览和项目书对照校验
7. 最后进入编码实现与测试

必须强调：

- 不允许直接从 Controller 开始写
- 不允许跳过设计阶段直接写代码
- 不允许让 `stockcheck` 直接修改库存表

## 13. 本阶段结论

当前阶段 `stockcheck` 模块最重要的是先统一职责边界、数据库依赖、差异计算规则、事务边界和与 `stock` 的协作方式。

本文件将作为后续 `stockcheck` 模块设计与实现的基线。
