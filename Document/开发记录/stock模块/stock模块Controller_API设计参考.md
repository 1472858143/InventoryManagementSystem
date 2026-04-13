# Stock 模块 Controller / API 设计参考

## 1. 文档目的

本文档用于明确 `stock` 模块第一阶段对外接口的 Controller 结构、API 定义、认证要求、参数边界与统一响应规范。

当前阶段只做接口设计，不编写 Controller 代码。

## 2. 当前设计范围

当前只覆盖以下第一阶段接口：

1. 查询库存列表
2. 查询单个商品当前库存
3. 维护库存上下限

## 3. 当前阶段不处理什么

当前阶段暂不处理：

- 对外库存增加接口
- 对外库存扣减接口
- 对外库存调整接口
- 入库单处理
- 出库单处理
- 盘点单处理
- 分页与复杂筛选
- 对外库存日志查询

## 4. 统一接口规范

当前阶段统一遵循以下规则：

1. 所有接口统一使用 `ApiResponse<T>`
2. 成功返回统一使用 `ApiResponse.success(data)` 或 `ApiResponse.success()`
3. 失败响应由全局异常处理器统一处理
4. 所有接口统一路径前缀为 `/api/stocks`
5. 所有接口均需要认证
6. token 校验由 `auth` 模块拦截器完成
7. Controller 不负责解析 token

## 5. 接口清单设计

### 5.1 查询库存列表

- URL：`GET /api/stocks`
- 请求参数：无
- 返回类型：`ApiResponse<List<StockListItemResponse>>`

设计说明：

- 当前阶段无分页、无复杂筛选
- 只返回库存场景最小字段

### 5.2 查询单个商品当前库存

- URL：`GET /api/stocks/{productId}`
- 路径参数：`productId`
- 返回类型：`ApiResponse<StockDetailResponse>`

设计说明：

- 根据商品 ID 查询当前库存
- 不扩展为复杂商品详情接口

### 5.3 维护库存上下限

- URL：`PUT /api/stocks/{productId}/limit`
- 路径参数：`productId`
- 请求体：`StockLimitUpdateRequest`
- 返回类型：`ApiResponse<Void>`

设计说明：

- 当前阶段只维护上下限
- 不允许通过该接口直接调整 `quantity`

## 6. 安全边界说明

当前阶段必须明确禁止以下接口设计：

- `POST /api/stocks/increase`
- `POST /api/stocks/decrease`
- `PUT /api/stocks/{productId}/quantity`
- 任何可被外部直接用来修改库存数量的危险接口

说明：

- 对外不开放直接改库存数量的接口，是当前阶段必须坚持的安全边界
- 库存数量变更只能走 `stock` 模块内部核心能力，并在后续由 `inbound / outbound / stockcheck` 间接调用

## 7. 认证与拦截说明

当前阶段以上 3 个接口均为受保护接口：

1. `GET /api/stocks`
2. `GET /api/stocks/{productId}`
3. `PUT /api/stocks/{productId}/limit`

认证链路说明：

- 认证由拦截器完成
- Controller 不解析 `Authorization`
- 未登录访问返回 `401`

## 8. 响应字段约束

当前阶段返回字段只允许聚焦库存场景最小信息，例如：

- `productId`
- `productCode`
- `productName`
- `quantity`
- `minStock`
- `maxStock`
- `updateTime`

必须强调：

- 不返回完整商品详情
- 不返回入库、出库、盘点统计
- 不返回库存变更日志列表

## 9. 接口设计总结

当前阶段接口设计具有以下特点：

- 简单 REST 风格
- 外部接口聚焦库存查询与上下限维护
- 无分页
- 无复杂筛选
- 严格禁止对外直接改库存数量

## 10. 本阶段结论

当前 `stock` 模块第一阶段对外 API 设计已经明确。

后续编码时应坚持：

- 对外只暴露查询与上下限维护
- 库存数量调整能力保留在 `stock` 模块内部
- 不允许通过公共接口绕过库存安全边界
