# Outbound 模块设计总览

## 1. 文档目的

本文档是 `outbound` 模块第一阶段开发设计的统一入口文档，用于集中说明当前阶段的开发范围、模块边界、关键设计结论、文档引用关系以及项目书对照摘要。

## 2. 当前开发范围

当前第一阶段接口只包括：

1. `POST /api/outbounds`
2. `GET /api/outbounds`

当前阶段聚焦出库记录主线能力，不提前扩展详情、修改、删除、分页、筛选和统计报表。

## 3. 模块职责与边界总结

### 3.1 outbound 模块的长期职责

`outbound` 模块长期负责以下内容：

- 记录出库信息
- 维护出库业务单据
- 作为库存减少原因的业务入口
- 库存不足时拒绝出库

### 3.2 outbound 模块第一阶段实际范围

当前第一阶段实际范围仅包括：

1. 新增出库记录
2. 查询出库记录列表

### 3.3 当前阶段边界结论

当前阶段需要明确：

- `product` 负责商品基础资料
- `outbound` 负责出库原因与出库单据
- `stock` 负责库存实际扣减、库存不足校验与库存日志
- `auth / user` 负责认证与操作人体系

必须强调：

- `outbound` 不直接修改 `stock.quantity`
- `outbound` 不直接写 `stock_log`
- `outbound` 必须通过 `stock` 完成库存扣减
- 库存不足时必须拒绝出库

## 4. 统一约束总结

当前阶段必须统一遵守以下规则：

1. 所有设计以当前 `outbound_order` 结构和项目书为准
2. 不允许擅自扩表
3. `outbound` 模块不引入 `domain`
4. Controller 不允许直接访问 Mapper
5. Service 负责流程编排与事务控制
6. Mapper 只负责 `outbound_order` 数据库访问
7. `stock` 负责库存扣减、库存不足校验和库存日志写入

## 5. 模块内结构与各层职责

根据项目书，`outbound` 模块内部结构如下：

- `controller`
- `service`
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
  - 业务流程编排
  - 事务控制
  - 调用 `stock` 完成库存扣减
  - 组装返回对象
- `mapper`
  - `outbound_order` 数据库访问
  - 不承载库存逻辑
- `entity`
  - 表映射对象
- `dto`
  - 请求对象
- `vo`
  - 返回对象
- `enums`
  - 保留目录，按需要再扩展

## 6. 对象层结论摘要

### 6.1 Entity

- `OutboundOrder`

### 6.2 DTO

- `OutboundCreateRequest`

### 6.3 VO

- `OutboundListItemResponse`
- `OutboundDetailResponse`

### 6.4 当前阶段对象结论

- `OutboundOrder` 严格映射 `outbound_order`
- DTO 只承载新增请求
- VO 只返回出库记录与最小商品信息
- 不引入库存字段作为 `outbound` 核心返回字段

## 7. Mapper 结论摘要

当前阶段 `OutboundOrderMapper` 的最小能力包括：

- `insert`
- `findAll`
- `findById`（可选保留）

当前阶段说明如下：

- 正式实现方式按项目书采用 MyBatis XML
- `OutboundOrderMapper.xml` 位于 `resources/mapper/`
- `Mapper` 不负责库存扣减
- `Mapper` 不负责库存不足判断
- 当前阶段不做分页
- 当前阶段不做筛选

## 8. Service 结论摘要

当前阶段 `OutboundService` 的核心方法包括：

- `createOutbound`
- `listOutbounds`

### 8.1 createOutbound 关键结论

主要流程包括：

- 校验参数
- 校验商品存在
- 保存 `outbound_order`
- 通过 `stockService.decreaseStock(productId, quantity)` 完成库存扣减与库存不足校验
- 同一事务提交

### 8.2 listOutbounds 关键结论

- 当前阶段只做简单列表查询与 VO 转换
- 不做分页
- 不做筛选

### 8.3 事务结论

- `createOutbound` 必须使用事务
- `listOutbounds` 不需要事务

## 9. API 结论摘要

当前阶段接口包括：

1. `POST /api/outbounds`
2. `GET /api/outbounds`

统一约束如下：

- 两个接口都必须经过 `auth` 模块认证
- Controller 不解析 token
- 当前阶段不做分页
- 当前阶段不做复杂查询
- 不扩展详情、删除、修改接口

## 10. 当前阶段未纳入范围的内容

当前阶段暂不纳入：

- 出库详情接口
- 出库记录删除
- 出库记录修改
- 分页查询
- 条件筛选查询
- 出库统计报表
- 对外库存调整接口

## 11. 文档引用关系

本总览文档对应以下详细设计文档：

1. `outbound模块开发注意事项.md`
2. `outbound模块对象层设计参考.md`
3. `outbound模块Mapper能力设计参考.md`
4. `outbound模块Service流程设计参考.md`
5. `outbound模块Controller_API设计参考.md`
6. `outbound模块项目书对照校验.md`

使用方式说明：

- 总览文档用于快速查看整体设计结论
- 详细文档用于后续编码前逐项核对

## 12. 项目书对照摘要

当前设计与项目书保持一致的关键点如下：

1. `outbound` 模块只保留常规分层，不增加 `domain`
2. 关联主表为 `outbound_order`
3. 对外接口只包括 `POST /api/outbounds` 和 `GET /api/outbounds`
4. 业务流程为“校验出库请求 -> 保存出库记录 -> 调用库存模块扣减库存 -> 由库存模块更新 `stock` 与写入 `stock_log`，失败则整体回滚”
5. 库存不足时必须拒绝出库
6. 出库写操作必须保证事务一致性

完整逐条校验见：

- `outbound模块项目书对照校验.md`

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

`outbound` 模块第一阶段设计已经完成收口，当前已形成可进入编码阶段的统一设计基线。

后续编码应严格依据本目录中的设计文档推进，并以项目书为最高优先级依据。

## 15. 本文件作用

本文档用于快速查看 `outbound` 模块当前阶段整体设计结论。

编码前应优先从本文件开始，再按需查看对象层、Mapper、Service、Controller / API 与项目书对照校验文档。
