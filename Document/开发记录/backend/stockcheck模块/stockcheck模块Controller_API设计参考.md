# Stockcheck 模块 Controller / API 设计参考

## 1. 文档目的

本文档用于明确 `stockcheck` 模块第一阶段对外 API 设计，为后续 Controller 编码和接口测试提供标准依据。

当前阶段只做 API 设计，不写代码。

## 2. 当前设计范围

当前只设计以下接口：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

## 3. 当前阶段不处理什么

当前阶段不设计以下接口：

- `GET /api/stockchecks/{id}`
- `DELETE /api/stockchecks/{id}`
- `PUT /api/stockchecks/{id}`
- 分页查询接口
- 条件筛选接口
- 盘点统计接口
- 对外库存调整接口

## 4. 统一接口规范

所有接口统一使用：

- `ApiResponse<T>`

成功返回：

- `ApiResponse.success(data)`

失败处理：

- 由全局异常处理器统一处理

统一路径前缀：

- `/api/stockchecks`

认证要求：

- 所有接口都需要经过 `auth` 模块拦截器
- Controller 不解析 token
- Controller 不读取当前用户

## 5. 接口清单设计

### 5.1 新增盘点记录

接口：

- `POST /api/stockchecks`

请求体：

- `StockCheckCreateRequest`

返回：

- `ApiResponse<StockCheckDetailResponse>`

说明：

- 使用 `@PostMapping`
- 使用 `@RequestBody`
- 使用 `@Valid`
- 由 Service 完成盘点记录写入和库存调整流程编排
- Controller 不写业务逻辑

请求示例：

```json
{
  "productId": 1,
  "actualQuantity": 95
}
```

响应字段建议：

```json
{
  "id": 1,
  "productId": 1,
  "productCode": "P001",
  "productName": "测试商品",
  "systemQuantity": 100,
  "actualQuantity": 95,
  "difference": -5,
  "checkTime": "2026-04-16T10:00:00"
}
```

### 5.2 查询盘点记录列表

接口：

- `GET /api/stockchecks`

请求参数：

- 无

返回：

- `ApiResponse<List<StockCheckListItemResponse>>`

说明：

- 使用 `@GetMapping`
- 当前阶段无分页
- 当前阶段无筛选
- 只返回盘点记录及最小商品信息

## 6. 参数校验说明

基础校验：

- 使用 `@Valid`
- DTO 层负责非空和范围校验

建议校验规则：

- `productId` 非空
- `actualQuantity` 非空
- `actualQuantity >= 0`

业务校验：

- Service 层负责库存记录是否存在
- Service 层负责读取系统库存并计算差异
- Service 层负责调用 `stock` 完成库存调整

## 7. 认证与拦截说明

所有接口均为受保护接口。

要求：

- 必须经过 auth 模块拦截器
- Controller 不解析 `Authorization`
- 未登录访问返回 `401`

后续编码时如需更新 `WebMvcConfig`，应加入：

- `/api/stockchecks`
- `/api/stockchecks/**`

并保持已有 `auth / user / product / stock / inbound / outbound` 拦截逻辑不变。

## 8. 响应字段约束

当前阶段返回 VO 只包含：

- `id`
- `productId`
- `productCode`
- `productName`
- `systemQuantity`
- `actualQuantity`
- `difference`
- `checkTime`

不返回：

- 库存日志明细
- 入库统计
- 出库统计
- 报表统计
- `stock` 完整详情对象

## 9. Controller 职责边界

Controller 只负责：

- 接收请求
- 参数校验
- 调用 Service
- 返回统一响应

Controller 不负责：

- 数据库访问
- 库存调整
- 差异计算
- token 解析
- 异常翻译

## 10. 本阶段结论

`stockcheck` 模块 API 设计已经完成。

当前接口保持简单 REST 风格，严格围绕项目书定义的新增盘点记录和查询盘点记录列表，不扩展详情、修改、删除、分页、筛选和统计能力。
