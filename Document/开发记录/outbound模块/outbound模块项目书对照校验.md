# Outbound 模块项目书对照校验

## 1. 文档目的

本文档用于将 `outbound` 模块当前设计文档与项目书逐条对照，确认模块职责、结构、表依赖、接口、流程、库存不足规则和事务结论均符合项目书要求。

项目书始终为最高优先级依据。

## 2. 对照结论总览

当前 `outbound` 模块设计与项目书结论总体一致。

当前未发现需要以项目书为准重新推翻的设计冲突，关键边界已经保持统一：

- `outbound` 只负责出库单据和流程编排
- `stock` 负责库存扣减、库存不足校验和库存日志
- `outbound` 不引入 `domain`
- 正式 Mapper 实现方式采用 MyBatis XML

## 3. 逐项对照

### 3.1 模块职责

项目书要求：

- 记录出库信息
- 减少库存

文档设计结论：

- `outbound` 负责记录出库信息
- `outbound` 通过 `stock` 模块完成库存减少

是否一致：

- 一致

备注：

- 设计文档没有把库存减少误写为 `outbound` 直接改库存表，而是按项目书“统一库存控制”思想，通过 `stock` 完成

### 3.2 模块结构

项目书要求：

- `outbound` 模块结构为：
  - `controller`
  - `service`
  - `mapper`
  - `entity`
  - `dto`
  - `vo`
  - `enums`

文档设计结论：

- 所有设计文档均按以上结构定义
- 未给 `outbound` 设计 `domain`

是否一致：

- 一致

备注：

- `domain` 明确保留在 `stock` 模块，未扩散到 `outbound`

### 3.3 关联数据表

项目书要求：

- `outbound_order`
- `stock`
- `stock_log`

文档设计结论：

- `outbound` 直接操作 `outbound_order`
- 业务流程依赖 `stock`、`stock_log`
- `stock` 与 `stock_log` 的写入职责仍归 `stock`

是否一致：

- 一致

备注：

- 设计文档明确了表依赖和职责边界，没有把 `stock_log` 误分配给 `outbound mapper`

### 3.4 API

项目书要求：

- `POST /api/outbounds`
- `GET /api/outbounds`

文档设计结论：

- Controller / API 设计文档仅保留这两个接口

是否一致：

- 一致

备注：

- 未扩展 `GET /api/outbounds/{id}`、删除、修改、分页、筛选接口

### 3.5 业务流程

项目书要求：

- 提交出库请求
- 校验库存是否充足
- 保存 `outbound_order`
- 调用库存模块扣减库存
- 更新 `stock`
- 写入 `stock_log`
- 事务提交

文档设计结论：

- `createOutbound` 流程按上述要求设计
- `outbound` 保存单据
- `stock` 执行库存扣减、库存不足校验并写日志

是否一致：

- 一致

备注：

- 文档中把“更新 `stock`、写入 `stock_log`、库存不足校验”明确归到 `stock` 模块，符合项目书统一库存控制原则

### 3.6 库存不足规则

项目书要求：

- 库存不足时必须拒绝出库

文档设计结论：

- `createOutbound` 必须通过 `stockService.decreaseStock(...)` 完成库存扣减
- 库存不足由 `stock` 模块统一兜底
- 库存不足时不保存出库记录

是否一致：

- 一致

备注：

- 设计文档已经在注意事项、Service 设计和总览中固定该规则

### 3.7 事务一致性

项目书要求：

- 入库、出库、盘点等写操作应保证事务一致性

文档设计结论：

- `createOutbound` 必须使用事务
- `outbound_order` 保存与库存扣减必须在同一事务中完成

是否一致：

- 一致

备注：

- 查询接口 `listOutbounds` 明确不需要事务，也符合常规设计

### 3.8 与 stock 的职责边界

项目书要求：

- `stock` 是唯一允许直接修改库存数据的核心模块
- `outbound` 通过 `stock` 完成库存扣减

文档设计结论：

- 所有 `outbound` 文档均明确：
  - 不直接修改 `stock.quantity`
  - 不直接写 `stock_log`
  - 通过 `stockService.decreaseStock(...)` 完成库存扣减

是否一致：

- 一致

备注：

- 这是当前设计中最重要的边界，已在注意事项、Service 设计和总览中重复固定

### 3.9 Mapper 实现方式

项目书要求：

- `resources/mapper/OutboundOrderMapper.xml`

文档设计结论：

- `OutboundOrderMapper` 最终按 MyBatis XML 实现
- `OutboundOrderMapper.xml` 位于 `resources/mapper/`

是否一致：

- 一致

备注：

- 已避免出现注解 SQL 与项目书冲突的设计口径

## 4. 冲突检查结果

当前未发现以下冲突：

- 未发现多设计 `domain` 层
- 未发现扩展项目书之外的公开接口
- 未发现把库存修改职责错误放到 `outbound`
- 未发现把 `stock_log` 直接设计为 `outbound mapper` 写入职责
- 未发现弱化库存不足拒绝出库规则

## 5. 最终结论

当前 `outbound` 模块设计文档已与项目书完成对照校验，结论为：

- 当前设计完全符合项目书要求
- 当前未发现需要回退或重做的设计冲突
- 后续编码阶段应继续以本目录文档和项目书为双重基线推进，其中项目书优先级最高
