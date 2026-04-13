# Stock 模块 Mapper 能力设计参考

## 1. 文档目的

本文档用于明确 `stock` 模块第一阶段所需的最小 Mapper 能力，为后续编写 Mapper 接口与 SQL 提供统一参考。

当前阶段只做能力设计，不编写 SQL，不编写 Mapper 代码实现。

## 2. 当前设计范围

当前只围绕以下第一阶段能力设计 Mapper：

1. 查询库存列表
2. 查询单个商品当前库存
3. 维护库存上下限

同时补充 `stock` 模块内部核心支撑能力：

- 库存日志写入

## 3. 当前阶段不处理什么

当前阶段暂不纳入 Mapper 设计范围的内容包括：

- 入库单查询
- 出库单查询
- 盘点单查询
- 分页查询
- 复杂筛选查询
- 库存统计报表查询
- 对外库存日志查询

## 4. 设计约束

当前阶段必须统一遵守以下约束：

1. Mapper 只负责数据库访问
2. Mapper 不负责库存合法性判断
3. Mapper 不负责业务校验
4. Mapper 不直接返回 VO
5. Mapper 不承载库存变更规则
6. 方法命名必须清晰表达用途

## 5. Mapper 划分建议

当前阶段建议采用以下划分：

- `StockMapper`
- `StockLogMapper`

说明如下：

- `StockMapper` 承担第一阶段外部接口的主数据访问
- `StockLogMapper` 承担库存变更日志写入支撑能力
- 当前阶段不新增 `ProductMapper` 作为 `stock` 模块独立 Mapper，而是在库存查询中按需做最小商品信息联表或最小商品读取能力设计

## 6. 方法清单

### 6.1 StockMapper.findAll

- 输入：无
- 输出：`List<StockListView>` 或等价库存查询结果模型
- 用途：查询库存列表

说明：

- 当前阶段允许返回库存列表查询结果模型，而不是直接返回 VO
- 如果库存展示需要 `productCode`、`productName`，可在此处做最小联表查询

### 6.2 StockMapper.findByProductId

- 输入：`Long productId`
- 输出：`StockDetailView` 或 `Stock`
- 用途：根据商品 ID 查询当前库存

### 6.3 StockMapper.updateLimitByProductId

- 输入：
  - `Long productId`
  - `Integer minStock`
  - `Integer maxStock`
- 输出：`int`
- 用途：维护库存上下限

### 6.4 StockLogMapper.insert

- 输入：`StockLog`
- 输出：`int`
- 用途：写入库存变更日志

说明：

- 当前阶段虽然不开放库存日志查询接口，但库存日志写入能力必须纳入 `stock` 模块内部设计

## 7. 查询方案说明

当前阶段库存列表和库存详情查询可采用“库存主表 + 最小商品信息”的查询方案，原因如下：

- 库存展示需要最小商品信息来提高可读性
- 但不能把 `product` 模块完整详情塞进库存响应
- 当前阶段不涉及入库、出库、盘点统计数据聚合

因此当前阶段应坚持：

- 只联动 `stock` 与 `product`
- 只返回库存场景最小字段
- 不做分页
- 不做复杂筛选

## 8. 安全边界说明

当前阶段必须明确：

- 不设计任何“外部直接改 quantity”的 Mapper 暴露语义
- 若后续存在库存增加、扣减、调整能力，应作为 `domain + service` 内部能力设计，不在第一阶段 Controller 文档中外显

## 9. 本阶段结论

当前 `stock` 模块第一阶段所需的 Mapper 能力已经明确。

结论如下：

- 外部接口主能力由 `StockMapper` 承担
- 库存日志写入由 `StockLogMapper` 承担
- 后续编码时应坚持“Mapper 只查库/写库，不承载库存规则”的原则
