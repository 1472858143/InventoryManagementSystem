# Outbound 模块 Controller / API 设计参考

## 1. 文档目的

本文档用于明确 `outbound` 模块第一阶段对外 API 设计，为后续编写 Controller 提供直接参考。

当前阶段只整理 API 设计，不写代码。

## 2. 当前设计范围

当前阶段只设计以下接口：

1. `POST /api/outbounds`
2. `GET /api/outbounds`

## 3. 当前阶段不处理什么

当前阶段不纳入以下 API：

- `GET /api/outbounds/{id}`
- 出库记录删除接口
- 出库记录修改接口
- 分页与筛选查询接口
- 报表接口
- 对外库存调整接口

## 4. 统一接口规范

当前阶段统一遵守以下规则：

1. 所有接口统一使用 `ApiResponse<T>`
2. 成功返回统一使用 `ApiResponse.success(...)`
3. 失败由全局异常处理器处理
4. 所有接口路径统一前缀：
   - `/api/outbounds`
5. 所有接口均需要认证：
   - 必须经过 `auth` 模块拦截器
   - Controller 不解析 token

## 5. 接口清单设计

### 5.1 新增出库记录

接口：

- `POST /api/outbounds`

请求体：

- `OutboundCreateRequest`

返回：

- `ApiResponse<OutboundDetailResponse>`

说明：

- 使用 `@PostMapping`
- 使用 `@RequestBody`
- 使用 `@Valid`
- Controller 不写业务逻辑
- 由 Service 完成出库单写入和库存扣减流程编排

### 5.2 查询出库记录列表

接口：

- `GET /api/outbounds`

请求参数：

- 无

返回：

- `ApiResponse<List<OutboundListItemResponse>>`

说明：

- 使用 `@GetMapping`
- 当前阶段无分页、无筛选
- 只返回出库记录及最小商品信息

## 6. 参数校验说明

当前阶段参数校验口径如下：

1. 使用 `@Valid` 进行基础校验
2. DTO 层负责非空校验
3. Service 层负责业务规则校验

## 7. 认证与拦截说明

当前阶段必须明确：

1. 所有 `outbound` 接口均为受保护接口
2. 认证由拦截器完成
3. Controller 不解析 `Authorization`
4. 未登录访问应返回 `401`

## 8. 响应字段约束

当前阶段返回 VO 只应包含：

- `id`
- `productId`
- `productCode`
- `productName`
- `quantity`
- `operator`
- `createTime`

必须强调：

- 不返回库存数量作为出库主响应字段
- 不返回 `stock_log` 字段
- 不返回项目书未定义的扩展字段

## 9. 接口设计总结

当前设计特点如下：

- 简单 REST 风格
- 出库单据主线
- 与 `stock` 模块协作完成库存扣减
- 库存不足时拒绝出库
- 无分页
- 无复杂查询
- 与现有模块文档风格一致

## 10. 本阶段结论

`outbound` 模块第一阶段 API 设计已经明确。

后续编码时应严格只实现项目书已定义的两个接口，不提前扩展详情、删除、修改、分页、筛选和报表能力。
