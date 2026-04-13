# Stock 模块设计总览

## 1. 文档目的

本文档是 `stock` 模块第一阶段开发设计的统一入口文档，用于集中说明当前阶段的开发范围、模块边界、关键设计结论与相关文档的引用关系。

## 2. 当前开发范围

当前第一阶段对外接口只包括：

1. 查询库存列表
2. 查询单个商品当前库存
3. 维护库存上下限

同时，当前阶段必须明确以下内部核心能力：

- 统一库存变更控制
- 库存日志记录

当前阶段聚焦库存主线基础能力，不提前开放危险库存调整接口。

## 3. 模块职责与边界总结

### 3.1 stock 模块的长期职责

`stock` 模块长期负责以下内容：

- 当前库存数量维护
- 库存上下限维护
- 库存合法性校验
- 库存变更控制
- 库存日志记录
- 库存查询

### 3.2 stock 模块第一阶段实际范围

当前第一阶段对外实际范围仅包括：

1. 查询库存列表
2. 查询单个商品当前库存
3. 维护库存上下限

### 3.3 当前阶段边界结论

当前阶段需要明确：

- `product` 只负责商品基础数据
- `stock` 负责当前库存状态与统一库存变更规则
- `inbound / outbound` 负责库存变化原因和业务单据
- `stockcheck` 负责盘点结果与差异调整原因
- `auth / user` 负责认证与操作人体系

必须强调：

- 只有 `stock` 能直接修改库存表
- 外部模块只能通过 `stock` 变更库存
- 第一阶段不开放直接改库存数量的外部接口

## 4. 统一约束总结

当前阶段必须统一遵守以下规则：

1. 所有设计以当前 `stock`、`product`、`stock_log` 结构为准
2. 不允许擅自扩表
3. `domain` 只在 `stock` 模块中保留，这是项目书要求
4. Controller 不允许直接访问 Mapper
5. Service 负责流程编排与事务控制
6. Domain 负责库存核心规则封装
7. Mapper 只负责数据库访问
8. 不允许把库存规则分散到其他模块里直接实现

## 5. 模块内结构与各层职责

项目书规定 `stock` 模块内部结构如下：

- `controller`
- `service`
- `domain`
- `mapper`
- `entity`
- `dto`
- `vo`
- `enums`

各层职责如下：

- `controller`
  - 接收请求
  - 参数校验
  - 调用 Service
  - 返回统一响应
- `service`
  - 对外业务流程编排
  - 事务控制
  - 调用 `domain`
  - 组装返回对象
- `domain`
  - 封装库存核心规则
  - 统一库存增减/调整逻辑
  - 校验库存合法性
  - 生成库存日志变更结果
- `mapper`
  - 数据库访问
  - 不承载业务规则
- `entity`
  - 表映射对象
- `dto`
  - 请求对象
- `vo`
  - 返回对象
- `enums`
  - 库存变更类型、库存状态语义等枚举

## 6. 对象层结论摘要

### 6.1 Entity

- `Stock`
- `StockLog`

### 6.2 Domain

- `StockChangeCommand`
- `StockChangeResult`

### 6.3 DTO

- `StockLimitUpdateRequest`

### 6.4 VO

- `StockListItemResponse`
- `StockDetailResponse`

### 6.5 当前阶段对象结论

- 对外返回只保留库存场景最小字段
- 仅在库存展示需要时引用最小商品信息
- 不引入入库、出库、盘点单据对象
- 对象层已完成定稿，`domain` 与 `enums` 的命名边界已固定，可直接进入后续 Mapper 设计与编码准备

## 7. Mapper 结论摘要

当前阶段 Mapper 设计包括：

- `StockMapper`
- `StockLogMapper`

当前阶段最小能力包括：

- `StockMapper.findAll`
- `StockMapper.findByProductId`
- `StockMapper.updateLimitByProductId`
- `StockLogMapper.insert`

说明：

- 对外接口主能力由 `StockMapper` 承担
- `StockLogMapper` 为内部库存变更支撑能力
- 正式代码实现按项目书采用 MyBatis XML 映射文件
- 当前阶段不做分页
- 当前阶段不做复杂筛选

## 8. Service 结论摘要

当前阶段对外 Service 核心方法包括：

- `listStocks`
- `getStockByProductId`
- `updateStockLimit`

内部核心能力包括：

- `increaseStock`
- `decreaseStock`
- `adjustStock`

事务结论如下：

- 查询接口不需要事务
- 上下限维护通常不需要复杂事务
- 后续库存增减或调整应在事务中同时更新 `stock` 与写入 `stock_log`

## 9. API 结论摘要

当前阶段接口包括：

1. `GET /api/stocks`
2. `GET /api/stocks/{productId}`
3. `PUT /api/stocks/{productId}/limit`

统一约束如下：

- 三个接口都必须经过 `auth` 模块认证
- Controller 不解析 token
- 当前阶段不做分页
- 当前阶段不做复杂筛选
- 不开放直接修改库存数量的公共接口

## 10. 安全边界总结

当前阶段必须坚持以下安全边界：

1. 禁止设计任何绕过 `stock` 模块直接修改库存表的方案
2. 禁止在 `product`、`inbound`、`outbound`、`stockcheck` 中直接写 `stock.quantity`
3. 禁止第一阶段暴露“任意手工改库存数量”的公共接口
4. 禁止把库存变更规则分散到多个模块分别实现
5. 禁止在 `Controller` 或 `Mapper` 中承载库存合法性判断

## 11. 当前阶段未纳入范围的内容

当前阶段暂不纳入：

- 对外库存增减接口
- 入库单处理
- 出库单处理
- 盘点单处理
- 批量库存调整
- 分页查询
- 复杂筛选
- 对外库存日志查询
- 库存统计报表

## 12. 文档引用关系

本总览文档对应以下详细设计文档：

1. `stock模块开发注意事项.md`
2. `stock模块对象层设计参考.md`
3. `stock模块Mapper能力设计参考.md`
4. `stock模块Service流程设计参考.md`
5. `stock模块Controller_API设计参考.md`

使用方式说明：

- 总览文档用于快速查看整体设计结论
- 详细文档用于后续编码前逐项核对

## 13. 开发顺序

当前阶段建议的开发顺序为：

1. 开发注意事项
2. 对象层设计
3. Mapper 能力设计
4. Service 流程设计
5. Controller / API 设计
6. 进入编码实现与测试

必须强调：

- 不允许跳过顺序
- 不允许直接从 Controller 开始开发

## 14. 本阶段结论

`stock` 模块第一阶段设计已经完成收口，并已完成代码落地到 Controller 层，当前已形成设计与实现一致的统一基线。

当前第一阶段设计已完成落地，并已完成结构检查与编译验证，Mapper 已按项目书要求对齐为 XML。

## 15. 本文件作用

本文档用于快速查看 `stock` 模块当前阶段整体设计结论。

编码前应优先从本文件开始，再按需查看对象层、Mapper、Service 与 Controller / API 的详细设计文档。
