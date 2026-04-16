# Stockcheck 模块设计总览

## 1. 文档目的

本文档是 `stockcheck` 模块第一阶段开发设计的统一入口，用于集中说明当前范围、边界、设计结论和文档引用关系。

本文档以项目书为最高优先级依据。

## 2. 当前开发范围

当前第一阶段接口只包括：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

当前阶段聚焦库存盘点主线能力，不提前扩展详情、修改、删除、分页、筛选和统计报表。

## 3. 模块职责与边界总结

### 3.1 stockcheck 模块长期职责

`stockcheck` 模块长期负责以下内容：

- 记录盘点结果
- 维护盘点业务记录
- 计算系统库存与实际库存差异
- 作为库存校准原因的业务入口

### 3.2 stockcheck 模块第一阶段实际范围

当前第一阶段实际范围仅包括：

1. 新增盘点记录
2. 查询盘点记录列表

### 3.3 当前阶段边界结论

当前阶段需要明确：

- `product` 负责商品基础资料
- `stockcheck` 负责盘点差异和盘点记录
- `stock` 负责库存实际调整与库存日志
- `auth / user` 负责认证与操作人体系

必须强调：

- `stockcheck` 不直接修改 `stock.quantity`
- `stockcheck` 不直接写 `stock_log`
- `stockcheck` 必须通过 `stock` 完成库存调整

## 4. 统一约束总结

当前阶段必须统一遵守以下规则：

1. 所有设计以当前 `stock_check` 结构和项目书为准
2. 不允许擅自扩表
3. `stockcheck` 模块不引入 `domain`
4. Controller 不允许直接访问 Mapper
5. Service 负责流程编排、差异计算与事务控制
6. Mapper 只负责 `stock_check` 数据库访问
7. `stock` 负责库存调整和库存日志写入

## 5. 模块内结构与各层职责

根据项目书，`stockcheck` 模块内部结构如下：

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
  - 查询系统库存
  - 计算库存差异
  - 调用 `stock` 完成库存调整
  - 组装返回对象
- `mapper`
  - `stock_check` 数据库访问
  - 不承载库存调整逻辑
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

- `StockCheck`

### 6.2 DTO

- `StockCheckCreateRequest`

### 6.3 VO

- `StockCheckListItemResponse`
- `StockCheckDetailResponse`

### 6.4 当前阶段对象结论

- `StockCheck` 严格映射 `stock_check`
- DTO 只接收新增盘点请求
- VO 只返回盘点记录与最小商品信息
- 不引入库存日志字段作为 `stockcheck` 核心返回字段
- `systemQuantity` 和 `difference` 不由前端传入，由 Service 计算

## 7. Mapper 结论摘要

当前阶段 `StockCheckMapper` 的最小能力包括：

- `insert`
- `findAll`
- `findById`

当前阶段说明如下：

- 正式实现方式按项目书和现有库存相关模块口径采用 MyBatis XML
- `StockCheckMapper.xml` 位于 `resources/mapper/`
- `Mapper` 不负责库存调整
- `Mapper` 不负责库存日志写入
- 当前阶段不做分页
- 当前阶段不做筛选

## 8. Service 结论摘要

当前阶段 `StockCheckService` 的核心方法包括：

- `createStockCheck`
- `listStockChecks`

### 8.1 createStockCheck 关键结论

主要流程包括：

- 校验参数
- 通过 `StockService.getStockByProductId(productId)` 查询系统库存
- 计算 `difference = actualQuantity - systemQuantity`
- 保存 `stock_check`
- 通过 `stockService.adjustStock(productId, actualQuantity)` 完成库存调整
- 同一事务提交

### 8.2 listStockChecks 关键结论

- 当前阶段只做简单列表查询与 VO 转换
- 不做分页
- 不做筛选

### 8.3 事务结论

- `createStockCheck` 必须使用事务
- `listStockChecks` 不需要事务

## 9. API 结论摘要

当前阶段接口包括：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

统一约束如下：

- 两个接口都必须经过 `auth` 模块认证
- Controller 不解析 token
- 当前阶段不做分页
- 当前阶段不做复杂查询
- 不扩展详情、删除、修改接口

## 10. 当前阶段未纳入范围的内容

当前阶段暂不纳入：

- 盘点详情接口
- 盘点记录删除
- 盘点记录修改
- 分页查询
- 条件筛选查询
- 盘点统计报表
- 对外库存调整接口

## 11. 文档引用关系

本文档对应以下详细设计文档：

1. `stockcheck模块开发注意事项.md`
2. `stockcheck模块对象层设计参考.md`
3. `stockcheck模块Mapper能力设计参考.md`
4. `stockcheck模块Service流程设计参考.md`
5. `stockcheck模块Controller_API设计参考.md`
6. `stockcheck模块项目书对照校验.md`

使用方式说明：

- 总览文档用于快速查看整体设计结论
- 详细文档用于后续编码前逐项核对

## 12. 项目书对照摘要

当前设计与项目书保持一致的关键点如下：

1. `stockcheck` 模块只保留常规分层，不增加 `domain`
2. 关联主表为 `stock_check`
3. 对外接口只包括 `POST /api/stockchecks` 和 `GET /api/stockchecks`
4. 业务流程为“查询当前 stock -> 计算 difference -> 保存 stock_check -> 调用库存模块调整库存 -> 由库存模块更新 stock 与写入 stock_log”
5. 库存调整失败时必须整体回滚
6. 盘点写操作必须保证事务一致性

完整逐条校验见：

- `stockcheck模块项目书对照校验.md`

## 13. 开发顺序

当前阶段建议的开发顺序为：

1. 开发注意事项
2. 对象层设计
3. Mapper 能力设计
4. Service 流程设计
5. Controller / API 设计
6. 设计总览与项目书对照校验
7. 进入编码实现与测试

必须强调：

- 不允许跳过顺序
- 不允许直接从 Controller 开始开发

## 14. 本阶段结论

`stockcheck` 模块第一阶段设计已经完成收口，当前已形成可进入编码阶段的统一设计基线。

后续编码应严格依据本目录中的设计文档推进，并以项目书为最高优先级依据。

## 15. 本文件作用

本文档用于快速查看 `stockcheck` 模块当前阶段整体设计结论。

编码前应优先从本文件开始，再按需查看对象层、Mapper、Service、Controller / API 与项目书对照校验文档。
