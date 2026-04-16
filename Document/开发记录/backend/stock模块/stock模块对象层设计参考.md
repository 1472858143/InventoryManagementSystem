# Stock 模块对象层设计参考

## 1. 文档目的

本文档用于明确 `stock` 模块第一阶段所需的对象层设计，包括 `entity`、`domain`、`dto`、`vo`、`enums` 的名单、字段范围与职责边界。

当前阶段只做对象设计收口，不进入 `Mapper`、`Service`、`Controller` 的代码实现。

## 2. 当前设计范围

当前对象层设计只为以下第一阶段能力做准备：

1. 查询库存列表
2. 查询单个商品当前库存
3. 维护库存上下限

同时，为后续 `inbound / outbound / stockcheck` 复用 `stock` 模块内部统一库存变更能力，当前阶段一并完成 `domain` 与 `enums` 的基础定稿。

## 3. 当前阶段不处理什么

当前阶段不在对象层中扩展以下内容：

- 入库单对象
- 出库单对象
- 盘点单对象
- 批量库存调整对象
- 分页查询对象
- 复杂筛选对象
- 库存统计报表对象
- 库存日志对外查询返回对象

## 4. 对象层定稿原则

当前阶段必须统一遵守以下原则：

1. `Entity` 只用于数据库表映射
2. `Domain` 只用于库存核心规则封装
3. `DTO` 只用于接收请求参数
4. `VO` 只用于返回前端
5. 不允许直接返回 `Entity`
6. 不允许把 `product` 模块完整详情塞入 `stock` 返回对象
7. 不允许把入库、出库、盘点单据对象混入 `stock` 对象层
8. `stock_log.change_type` 在 `Entity` 层保持数据库字段语义，在 `Domain` 层通过枚举统一约束
9. 如后续 `Mapper` 需要额外查询结果模型，应放入 `mapper.model` 或等价内部包，不属于当前对象层定稿范围

## 5. 模块内对象划分总览

当前阶段 `stock` 模块对象层按以下结构定稿：

### 5.1 Entity

- `Stock`
- `StockLog`

### 5.2 Domain

- `StockChangeCommand`
- `StockChangeResult`

### 5.3 DTO

- `StockLimitUpdateRequest`

### 5.4 VO

- `StockListItemResponse`
- `StockDetailResponse`

### 5.5 Enums

- `StockChangeTypeEnum`

## 6. Entity 设计

### 6.1 Stock

用途说明：

- 映射 `stock` 表，用于承载当前库存基础数据

字段定稿：

- `Long id`
- `Long productId`
- `Integer quantity`
- `Integer minStock`
- `Integer maxStock`
- `LocalDateTime updateTime`

字段说明：

- `id`：库存主键
- `productId`：关联商品 ID
- `quantity`：当前库存数量
- `minStock`：库存下限
- `maxStock`：库存上限
- `updateTime`：库存最近更新时间

边界说明：

- `Stock` 不包含商品编码、商品名称，这些字段属于库存展示所需的最小关联信息，应在 `VO` 中体现
- `Stock` 不包含任何业务方法

### 6.2 StockLog

用途说明：

- 映射 `stock_log` 表，用于承载库存变更日志

字段定稿：

- `Long id`
- `Long productId`
- `String changeType`
- `Integer changeQuantity`
- `Integer beforeQuantity`
- `Integer afterQuantity`
- `LocalDateTime createTime`

字段说明：

- `id`：日志主键
- `productId`：关联商品 ID
- `changeType`：库存变化类型，对应数据库日志字段
- `changeQuantity`：本次变化数量
- `beforeQuantity`：变化前库存数量
- `afterQuantity`：变化后库存数量
- `createTime`：日志创建时间

边界说明：

- `StockLog` 属于 `stock` 模块内部支撑实体
- 当前第一阶段不对外暴露库存日志查询接口
- `changeType` 在 `Entity` 层保持 `String`，以严格对应数据库字段

## 7. Domain 设计

项目书已经明确 `domain` 层只在 `stock` 模块中保留，因此当前阶段必须显式保留库存领域对象，用于承载统一库存变更逻辑。

### 7.1 StockChangeCommand

用途说明：

- 用于承载库存增减或调整时的统一领域入参

字段定稿：

- `Long productId`
- `StockChangeTypeEnum changeType`
- `Integer changeQuantity`

字段说明：

- `productId`：被变更的商品 ID
- `changeType`：库存变化类型
- `changeQuantity`：本次变化数量，增加为正值，扣减或调整前由调用方计算后再传入统一变化值

设计说明：

- `StockChangeCommand` 不作为 Controller 请求对象暴露
- 它只服务于 `stock` 模块内部统一库存变更入口
- 后续 `inbound / outbound / stockcheck` 只能通过 `stock` 模块服务间接使用该领域能力

### 7.2 StockChangeResult

用途说明：

- 用于承载库存变更执行结果，并为 `stock_log` 写入提供统一结果数据

字段定稿：

- `Long productId`
- `StockChangeTypeEnum changeType`
- `Integer changeQuantity`
- `Integer beforeQuantity`
- `Integer afterQuantity`

字段说明：

- `productId`：被变更的商品 ID
- `changeType`：库存变化类型
- `changeQuantity`：本次变化数量
- `beforeQuantity`：变更前库存
- `afterQuantity`：变更后库存

设计说明：

- `StockChangeResult` 是 `domain` 层输出对象，不直接返回前端
- 它的主要职责是支撑库存合法性校验结果统一表达，以及库存日志写入

## 8. DTO 设计

### 8.1 StockLimitUpdateRequest

用途说明：

- 用于接收“维护库存上下限”请求参数

字段定稿：

- `Integer minStock`
- `Integer maxStock`

字段说明：

- `minStock`：库存下限
- `maxStock`：库存上限

边界说明：

- `productId` 不放入该 DTO，由路径参数承载，更符合项目书中 `PUT /api/stocks/{productId}/limit` 的接口定义
- 当前阶段不单独设计库存查询 DTO，因为当前接口范围是无分页、无复杂筛选的简单查询

## 9. VO 设计

### 9.1 StockListItemResponse

用途说明：

- 用于返回库存列表中的单个库存展示项

字段定稿：

- `Long productId`
- `String productCode`
- `String productName`
- `Integer quantity`
- `Integer minStock`
- `Integer maxStock`
- `LocalDateTime updateTime`

字段说明：

- `productId`：商品 ID
- `productCode`：商品编码，仅作为库存展示所需的最小商品信息
- `productName`：商品名称，仅作为库存展示所需的最小商品信息
- `quantity`：当前库存数量
- `minStock`：库存下限
- `maxStock`：库存上限
- `updateTime`：库存更新时间

边界说明：

- 不返回完整商品详情
- 不返回库存日志
- 不返回入库、出库、盘点统计

### 9.2 StockDetailResponse

用途说明：

- 用于返回单个商品当前库存详情

字段定稿：

- 与 `StockListItemResponse` 保持一致

边界说明：

- 当前阶段详情接口只返回当前库存场景所需最小字段
- 不追加库存变化原因列表
- 不追加库存趋势、预警统计等扩展信息

## 10. Enums 设计

### 10.1 StockChangeTypeEnum

用途说明：

- 用于在 `domain` 层统一库存变化类型，避免魔法字符串散落在 `Service` 或其他模块中

当前阶段定稿枚举值建议：

- `INBOUND`
- `OUTBOUND`
- `STOCKCHECK`

设计说明：

- 该枚举用于 `domain` 层与内部业务编排
- `Entity` 中的 `StockLog.changeType` 仍保持数据库字段映射语义
- 如后续库存主线新增新的合法变化来源，再在 `enums` 层扩展，不在当前阶段提前设计

## 11. 对象清单汇总

### 11.1 Entity 清单

- `Stock`：当前库存表映射对象
- `StockLog`：库存变更日志表映射对象

### 11.2 Domain 清单

- `StockChangeCommand`：统一库存变更领域入参
- `StockChangeResult`：统一库存变更领域结果

### 11.3 DTO 清单

- `StockLimitUpdateRequest`：库存上下限更新请求对象

### 11.4 VO 清单

- `StockListItemResponse`：库存列表单项返回对象
- `StockDetailResponse`：库存详情返回对象

### 11.5 Enums 清单

- `StockChangeTypeEnum`：库存变化类型枚举

## 12. 当前阶段对象层定稿结论

当前 `stock` 模块第一阶段对象层已经完成收口，后续编码时应严格按照本文件中的对象边界推进。

当前阶段已经明确以下结论：

- `domain` 只在 `stock` 模块中保留
- `Stock` 与 `StockLog` 严格对应数据库表结构
- 对外返回只保留库存场景所需最小字段
- `StockLimitUpdateRequest` 只负责上下限维护，不承载 `productId`
- `StockChangeTypeEnum` 作为库存变化类型的统一语义入口
- 如后续 `Mapper` 需要额外查询结果模型，不属于当前对象层定稿范围

## 本文件作用

本文档用于指导 `stock` 模块对象层实现，特别是 `entity / domain / dto / vo / enums` 的边界落地。
进入 Mapper 与 Service 设计前，应先以本文件为对象结构定稿依据。
