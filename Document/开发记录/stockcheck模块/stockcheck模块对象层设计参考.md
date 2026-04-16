# Stockcheck 模块对象层设计参考

## 1. 文档目的

本文档用于明确 `stockcheck` 模块第一阶段对象层设计，为后续 Mapper、Service、Controller 设计与编码实现提供直接参考。

当前阶段只做对象设计，不进入 Mapper / Service / Controller 实现。

## 2. 当前设计范围

当前只为以下接口设计对象层：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

## 3. 当前阶段不处理什么

当前阶段不纳入对象扩展范围的内容包括：

- 盘点详情接口专用请求对象
- 删除盘点记录对象
- 修改盘点记录对象
- 分页查询对象
- 条件筛选对象
- 盘点统计对象
- 库存日志对象
- 对外库存调整请求对象

## 4. 对象层设计原则

1. Entity 用于数据库映射
2. DTO 用于接收请求
3. VO 用于返回前端
4. 不允许直接返回 Entity
5. 不允许把库存日志明细混入 `stockcheck` VO
6. 不允许把 `stock` 完整详情对象混入 `stockcheck` 响应
7. 当前阶段保持对象简单，不引入复杂嵌套结构

## 5. Entity 设计

### 5.1 StockCheck

用途：

- 映射 `stock_check` 表
- 记录一次库存盘点结果
- 保存系统库存、实际库存和差异数量

建议字段：

- `Long id`
- `Long productId`
- `Integer systemQuantity`
- `Integer actualQuantity`
- `Integer difference`
- `LocalDateTime checkTime`

字段说明：

- `id`：盘点记录主键
- `productId`：被盘点商品 ID
- `systemQuantity`：盘点时系统记录的库存数量
- `actualQuantity`：盘点时实际库存数量
- `difference`：库存差异，计算规则为 `actualQuantity - systemQuantity`
- `checkTime`：盘点时间

设计约束：

- 字段严格对应 `stock_check` 表
- 不添加业务方法
- 不直接承载商品名称、商品编码等展示字段

## 6. DTO 设计

### 6.1 StockCheckCreateRequest

用途：

- 接收“新增盘点记录”请求参数

建议字段：

- `Long productId`
- `Integer actualQuantity`

字段说明：

- `productId`：需要盘点的商品 ID
- `actualQuantity`：盘点得到的实际库存数量

校验建议：

- `productId` 非空
- `actualQuantity` 非空
- `actualQuantity >= 0`

设计约束：

- 不包含 `systemQuantity`
- 不包含 `difference`
- `systemQuantity` 必须由 Service 根据当前库存读取
- `difference` 必须由 Service 统一计算

## 7. VO 设计

### 7.1 StockCheckListItemResponse

用途：

- 用于返回盘点记录列表中的单个展示项

建议字段：

- `Long id`
- `Long productId`
- `String productCode`
- `String productName`
- `Integer systemQuantity`
- `Integer actualQuantity`
- `Integer difference`
- `LocalDateTime checkTime`

字段说明：

- `productCode`、`productName` 来源于最小商品信息展示
- 不返回库存日志明细
- 不返回当前库存完整对象

### 7.2 StockCheckDetailResponse

用途：

- 当前阶段主要用于“新增盘点成功后的返回对象”
- 后续如果增加盘点详情接口，也可以复用

建议字段：

- 与 `StockCheckListItemResponse` 保持一致

设计约束：

- 当前阶段不额外增加复杂字段
- 不返回 `stock_log`
- 不返回报表统计信息

## 8. Enums 设计

当前阶段保留 `enums/` 目录，但不主动创建项目书未定义的业务枚举。

库存日志变更类型 `CHECK` 已在 `stock` 模块的 `StockChangeTypeEnum` 中存在，`stockcheck` 模块不重复定义。

## 9. 对象清单汇总

### 9.1 Entity

- `StockCheck`：映射 `stock_check` 表

### 9.2 DTO

- `StockCheckCreateRequest`：接收新增盘点请求

### 9.3 VO

- `StockCheckListItemResponse`：返回盘点列表展示项
- `StockCheckDetailResponse`：返回新增盘点成功后的详情

### 9.4 Enums

- 当前阶段仅保留目录，不新增具体枚举类

## 10. 本阶段结论

`stockcheck` 模块第一批接口的对象层设计已经明确。

后续编码时应严格按照本文档中的 Entity / DTO / VO 范围推进，保持盘点模块对象简单清晰，不提前引入分页、筛选、统计和库存日志明细对象。
