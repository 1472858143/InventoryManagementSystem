# Inbound 模块对象层设计参考

## 1. 文档目的

本文档用于明确 `inbound` 模块当前阶段对象层设计范围，为后续 Mapper、Service、Controller 设计和实现提供直接参考。

当前阶段只做对象层设计，不进入代码实现。

## 2. 当前设计范围

当前只围绕以下接口设计对象层：

1. `POST /api/inbounds`
2. `GET /api/inbounds`

## 3. 当前阶段不处理什么

当前阶段暂不纳入以下对象扩展：

- 入库详情接口扩展对象
- 分页查询对象
- 条件筛选对象
- 统计报表对象
- 库存日志对象作为 `inbound` 返回对象
- 直接库存修改请求对象

## 4. 对象层设计原则

当前阶段必须遵守以下原则：

1. Entity 用于数据库映射
2. DTO 用于接收请求
3. VO 用于返回前端
4. 不允许直接返回 Entity
5. `inbound` 对象不承担库存核心规则
6. `inbound` 模块不设计 `domain` 层对象

## 5. Entity 设计

### 5.1 InboundOrder

用途：

- 映射 `inbound_order` 表

建议字段如下：

- `Long id`
- `Long productId`
- `Integer quantity`
- `String operator`
- `LocalDateTime createTime`

字段说明：

- `id`：入库记录主键
- `productId`：关联商品主键
- `quantity`：本次入库数量
- `operator`：操作人
- `createTime`：入库时间

设计要求：

- 字段严格对应数据库表
- 不添加业务字段
- 不编写业务方法

## 6. DTO 设计

### 6.1 InboundCreateRequest

用途：

- 接收“新增入库记录”请求参数

建议字段如下：

- `Long productId`
- `Integer quantity`
- `String operator`

说明要求：

- `productId` 后续应加非空约束
- `quantity` 后续应加非空与大于 `0` 约束
- `operator` 后续应加非空约束

## 7. VO 设计

### 7.1 InboundListItemResponse

用途：

- 用于返回入库列表中的单条入库记录

建议字段如下：

- `Long id`
- `Long productId`
- `String productCode`
- `String productName`
- `Integer quantity`
- `String operator`
- `LocalDateTime createTime`

### 7.2 InboundDetailResponse

用途：

- 当前阶段主要用于新增入库成功后的返回对象
- 后续如果增加详情接口，也可复用

建议字段：

- 与 `InboundListItemResponse` 保持一致

说明要求：

- VO 只返回入库记录及最小商品信息
- 不包含库存数量
- 不包含库存日志字段
- 不包含出库、盘点相关信息

## 8. Enums 设计

当前阶段结论如下：

- 按项目书保留 `enums/` 目录
- 当前阶段不主动设计项目书未明确要求的业务枚举
- 如后续实现确有需要，再根据项目书与实现范围补充

## 9. 对象清单汇总

### 9.1 Entity

- `InboundOrder`
  - 入库表映射对象

### 9.2 DTO

- `InboundCreateRequest`
  - 入库新增请求对象

### 9.3 VO

- `InboundListItemResponse`
  - 入库列表返回对象
- `InboundDetailResponse`
  - 入库详情/创建结果返回对象

### 9.4 Enums

- 当前阶段只保留目录，不新增具体业务枚举

## 10. 本阶段结论

`inbound` 模块第一阶段对象层范围已经明确。

后续编码时应严格按本文件中的 Entity、DTO、VO 边界推进，不提前引入库存规则对象、分页筛选对象或项目书未定义的枚举设计。
