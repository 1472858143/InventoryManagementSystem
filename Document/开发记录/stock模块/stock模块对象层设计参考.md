# Stock 模块对象层设计参考

## 1. 文档目的

本文档用于明确 `stock` 模块第一阶段所需的对象层设计，包括 Entity、Domain、DTO、VO 的名单、字段范围与职责边界。

当前阶段只做对象设计，不进入 `Mapper`、`Service`、`Controller` 的实现。

## 2. 当前设计范围

当前只为以下第一阶段能力准备对象层设计：

1. 查询库存列表
2. 查询单个商品当前库存
3. 维护库存上下限

同时保留 `stock` 模块内部核心能力设计基础：

- 统一库存变更控制
- 库存日志记录

## 3. 当前阶段不处理什么

当前阶段暂不为以下内容扩展对象：

- 入库单对象
- 出库单对象
- 盘点单对象
- 批量库存调整对象
- 分页查询对象
- 复杂筛选对象
- 库存统计报表对象

## 4. 对象层设计原则

当前阶段必须统一遵守以下原则：

1. Entity 用于数据库映射
2. Domain 用于库存核心规则封装
3. DTO 用于接收请求
4. VO 用于返回前端
5. 不允许直接返回 Entity
6. 不允许把 `product` 模块完整详情塞入 `stock` 返回对象
7. 第一阶段保持对象简单，不提前引入入库/出库/盘点单据对象

## 5. Entity 设计

### 5.1 Stock

用途说明：

- 映射 `stock` 表，用于承载当前库存基础数据

字段建议：

- `Long id`
- `Long productId`
- `Integer quantity`
- `Integer minStock`
- `Integer maxStock`
- `LocalDateTime updateTime`

字段用途说明：

- `id`：库存主键
- `productId`：关联商品 ID
- `quantity`：当前库存数量
- `minStock`：库存下限
- `maxStock`：库存上限
- `updateTime`：库存更新时间

### 5.2 StockLog

用途说明：

- 映射 `stock_log` 表，用于承载库存变更日志

字段建议：

- `Long id`
- `Long productId`
- `String changeType`
- `Integer changeQuantity`
- `Integer beforeQuantity`
- `Integer afterQuantity`
- `LocalDateTime createTime`

设计说明：

- `StockLog` 属于 `stock` 模块内部支撑实体
- 当前第一阶段不对外暴露库存日志查询接口

## 6. Domain 设计

项目书已经明确 `domain` 层只在 `stock` 模块中保留，因此当前阶段应显式预留库存领域对象。

### 6.1 StockChangeCommand

用途说明：

- 用于承载库存增减或调整的领域参数

建议字段：

- `Long productId`
- `String changeType`
- `Integer changeQuantity`

### 6.2 StockChangeResult

用途说明：

- 用于承载库存变更前后结果，便于生成库存日志

建议字段：

- `Long productId`
- `Integer beforeQuantity`
- `Integer afterQuantity`
- `Integer changeQuantity`
- `String changeType`

设计说明：

- Domain 对象不直接作为 Controller 请求对象暴露
- Domain 对象用于服务后续 `inbound / outbound / stockcheck` 对 `stock` 模块的内部调用

## 7. DTO 设计

### 7.1 StockLimitUpdateRequest

用途说明：

- 用于接收“维护库存上下限”请求参数

字段建议：

- `Integer minStock`
- `Integer maxStock`

设计说明：

- 当前阶段不需要单独设计查询 DTO
- `productId` 由路径参数承载更符合项目书中 `PUT /api/stocks/{productId}/limit` 的接口定义

## 8. VO 设计

### 8.1 StockListItemResponse

用途说明：

- 用于返回库存列表中的单个库存展示项

字段建议：

- `Long productId`
- `String productCode`
- `String productName`
- `Integer quantity`
- `Integer minStock`
- `Integer maxStock`
- `LocalDateTime updateTime`

设计说明：

- `productCode`、`productName` 仅作为库存展示所需的最小商品信息
- 不返回完整商品详情

### 8.2 StockDetailResponse

用途说明：

- 用于返回单个商品当前库存详情

字段建议：

- 与 `StockListItemResponse` 保持一致即可

设计说明：

- 当前阶段不加入入库/出库/盘点统计
- 不加入库存变化原因列表

## 9. 对象清单汇总

### 9.1 Entity 清单

- `Stock`
- `StockLog`

### 9.2 Domain 清单

- `StockChangeCommand`
- `StockChangeResult`

### 9.3 DTO 清单

- `StockLimitUpdateRequest`

### 9.4 VO 清单

- `StockListItemResponse`
- `StockDetailResponse`

## 10. 本阶段结论

当前 `stock` 模块第一阶段对象层设计已经明确。

后续编码时应严格按照本文件中的 `Entity / Domain / DTO / VO` 范围推进，并坚持：

- Domain 只在 `stock` 模块中保留
- 对外返回只保留库存场景最小字段
- 不提前引入入库、出库、盘点单据对象
