# API 接口设计文档

本文档描述超市库存管理系统前后端 API 契约，位置归入系统设计阶段文档。内容依据当前后端 Controller、DTO、VO、统一响应封装和鉴权拦截配置整理。

## 1. 接口总体约定

### 1.1 基础信息

- 后端服务：Spring Boot
- 默认开发端口：`8080`
- 统一前缀：`/api`
- 请求体格式：`application/json`
- 响应体格式：`application/json`
- 时间字段：`LocalDateTime` 序列化结果，前端按字符串处理

### 1.2 统一响应格式

所有接口返回统一结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | number | 业务状态码，`0` 表示成功 |
| `message` | string | 响应消息 |
| `data` | any | 响应数据，无数据时为 `null` |

常见错误码：

| code | 说明 |
| --- | --- |
| `400` | 请求参数错误或业务规则不满足 |
| `401` | 未登录、认证失败、用户名或密码错误 |
| `403` | 当前用户被禁用 |
| `404` | 资源不存在 |
| `500` | 系统内部异常或数据回查失败 |

### 1.3 鉴权约定

除 `POST /api/auth/login` 外，业务接口均需要登录态。客户端登录后保存 `token`，后续请求通过请求头传递：

```http
Authorization: Bearer <token>
```

后端也兼容直接传入 token 值，但推荐使用 `Bearer` 格式。

## 2. 接口清单

| 模块 | 方法 | 路径 | 说明 | 是否需要登录 |
| --- | --- | --- | --- | --- |
| 认证 | `POST` | `/api/auth/login` | 用户登录 | 否 |
| 认证 | `POST` | `/api/auth/logout` | 用户退出 | 是 |
| 认证 | `GET` | `/api/auth/me` | 查询当前用户 | 是 |
| 用户 | `POST` | `/api/users` | 新增用户 | 是 |
| 用户 | `GET` | `/api/users` | 查询用户列表 | 是 |
| 用户 | `PUT` | `/api/users/status` | 更新用户状态 | 是 |
| 商品 | `POST` | `/api/products` | 新增商品 | 是 |
| 商品 | `GET` | `/api/products` | 查询商品列表 | 是 |
| 商品 | `PUT` | `/api/products/status` | 更新商品状态 | 是 |
| 库存 | `GET` | `/api/stocks` | 查询库存列表 | 是 |
| 库存 | `GET` | `/api/stocks/{productId}` | 查询指定商品库存 | 是 |
| 库存 | `PUT` | `/api/stocks/{productId}/limit` | 更新库存上下限 | 是 |
| 入库 | `POST` | `/api/inbounds` | 创建入库单 | 是 |
| 入库 | `GET` | `/api/inbounds` | 查询入库记录 | 是 |
| 出库 | `POST` | `/api/outbounds` | 创建出库单 | 是 |
| 出库 | `GET` | `/api/outbounds` | 查询出库记录 | 是 |
| 盘点 | `POST` | `/api/stockchecks` | 创建盘点记录 | 是 |
| 盘点 | `GET` | `/api/stockchecks` | 查询盘点记录 | 是 |

## 3. 认证接口

### 3.1 用户登录

`POST /api/auth/login`

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `username` | string | 是 | 不能为空 |
| `password` | string | 是 | 不能为空 |

成功响应 `data`：

```json
{
  "token": "token-string",
  "username": "admin",
  "roles": ["ADMIN"]
}
```

业务规则：

- 用户名不存在或密码不匹配时返回 `401`。
- 用户状态不是启用状态时返回 `403`。
- 登录成功后由后端签发 token。

### 3.2 用户退出

`POST /api/auth/logout`

请求头：

```http
Authorization: Bearer <token>
```

成功响应 `data` 为 `null`。后端会使当前 token 失效。

### 3.3 查询当前用户

`GET /api/auth/me`

成功响应 `data`：

```json
{
  "userId": 1,
  "username": "admin"
}
```

未登录或 token 无效时返回 `401`。

## 4. 用户接口

### 4.1 新增用户

`POST /api/users`

请求体：

```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "roleIds": [1, 2]
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `username` | string | 是 | 不能为空，不能与已有用户名重复 |
| `password` | string | 是 | 不能为空，后端保存前加密 |
| `realName` | string | 否 | 用户真实姓名 |
| `roleIds` | number[] | 是 | 不能为空，角色 ID 必须存在 |

成功响应 `data`：

```json
{
  "id": 1,
  "username": "zhangsan",
  "realName": "张三",
  "status": 1,
  "roleCodes": ["ADMIN"],
  "createTime": "2026-05-07T10:00:00"
}
```

业务规则：

- 新建用户默认启用，`status = 1`。
- `roleIds` 会去重后校验。
- 用户名重复返回 `400`。
- 角色为空或存在非法角色返回 `400`。

### 4.2 查询用户列表

`GET /api/users`

成功响应 `data`：

```json
[
  {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "status": 1,
    "roleCodes": ["ADMIN"],
    "createTime": "2026-05-07T10:00:00"
  }
]
```

返回空数据时 `data` 为 `[]`。

### 4.3 更新用户状态

`PUT /api/users/status`

请求体：

```json
{
  "userId": 1,
  "status": 0
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `userId` | number | 是 | 用户 ID 必须存在 |
| `status` | number | 是 | 只能为 `0` 或 `1` |

状态含义：

| 值 | 含义 |
| --- | --- |
| `0` | 禁用 |
| `1` | 启用 |

成功响应 `data` 为 `null`。用户不存在返回 `404`。

## 5. 商品接口

### 5.1 新增商品

`POST /api/products`

请求体：

```json
{
  "productCode": "P001",
  "productName": "矿泉水",
  "category": "饮料",
  "purchasePrice": 1.20,
  "salePrice": 2.00
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productCode` | string | 是 | 不能为空，不能重复 |
| `productName` | string | 是 | 不能为空 |
| `category` | string | 是 | 不能为空 |
| `purchasePrice` | number | 是 | 不能小于 `0` |
| `salePrice` | number | 是 | 不能小于 `0`，不能低于进价 |

成功响应 `data`：

```json
{
  "id": 1,
  "productCode": "P001",
  "productName": "矿泉水",
  "category": "饮料",
  "purchasePrice": 1.20,
  "salePrice": 2.00,
  "status": 1,
  "createTime": "2026-05-07T10:00:00"
}
```

业务规则：

- 新建商品默认启用，`status = 1`。
- 商品编码重复返回 `400`。
- 售价低于进价返回 `400`。

### 5.2 查询商品列表

`GET /api/products`

成功响应 `data`：

```json
[
  {
    "id": 1,
    "productCode": "P001",
    "productName": "矿泉水",
    "category": "饮料",
    "purchasePrice": 1.20,
    "salePrice": 2.00,
    "status": 1,
    "createTime": "2026-05-07T10:00:00"
  }
]
```

返回空数据时 `data` 为 `[]`。

### 5.3 更新商品状态

`PUT /api/products/status`

请求体：

```json
{
  "productId": 1,
  "status": 0
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productId` | number | 是 | 商品 ID 必须存在 |
| `status` | number | 是 | 只能为 `0` 或 `1` |

成功响应 `data` 为 `null`。商品不存在返回 `404`。

## 6. 库存接口

### 6.1 查询库存列表

`GET /api/stocks`

成功响应 `data`：

```json
[
  {
    "productId": 1,
    "productCode": "P001",
    "productName": "矿泉水",
    "quantity": 100,
    "minStock": 10,
    "maxStock": 500,
    "updateTime": "2026-05-07T10:00:00"
  }
]
```

返回空数据时 `data` 为 `[]`。

### 6.2 查询指定商品库存

`GET /api/stocks/{productId}`

路径参数：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productId` | number | 是 | 商品 ID |

成功响应 `data`：

```json
{
  "productId": 1,
  "productCode": "P001",
  "productName": "矿泉水",
  "quantity": 100,
  "minStock": 10,
  "maxStock": 500,
  "updateTime": "2026-05-07T10:00:00"
}
```

库存记录不存在时返回 `404`。

### 6.3 更新库存上下限

`PUT /api/stocks/{productId}/limit`

路径参数：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productId` | number | 是 | 商品 ID |

请求体：

```json
{
  "minStock": 10,
  "maxStock": 500
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `minStock` | number | 是 | 不能小于 `0` |
| `maxStock` | number | 是 | 不能小于 `0`，不能低于 `minStock` |

成功响应 `data` 为 `null`。库存记录不存在返回 `404`。

## 7. 入库接口

### 7.1 创建入库单

`POST /api/inbounds`

请求体：

```json
{
  "productId": 1,
  "quantity": 20,
  "operator": "张三"
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productId` | number | 是 | 商品 ID 必须存在 |
| `quantity` | number | 是 | 必须大于 `0` |
| `operator` | string | 是 | 不能为空 |

成功响应 `data`：

```json
{
  "id": 1,
  "productId": 1,
  "productCode": "P001",
  "productName": "矿泉水",
  "quantity": 20,
  "operator": "张三",
  "createTime": "2026-05-07T10:00:00"
}
```

业务规则：

- 创建入库记录后，同步增加对应商品库存。
- 商品不存在返回 `404`。
- 入库记录回查失败返回 `500`。

### 7.2 查询入库记录

`GET /api/inbounds`

成功响应 `data`：

```json
[
  {
    "id": 1,
    "productId": 1,
    "productCode": "P001",
    "productName": "矿泉水",
    "quantity": 20,
    "operator": "张三",
    "createTime": "2026-05-07T10:00:00"
  }
]
```

返回空数据时 `data` 为 `[]`。

## 8. 出库接口

### 8.1 创建出库单

`POST /api/outbounds`

请求体：

```json
{
  "productId": 1,
  "quantity": 5,
  "operator": "李四"
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productId` | number | 是 | 商品 ID 必须存在 |
| `quantity` | number | 是 | 必须大于 `0` |
| `operator` | string | 是 | 不能为空 |

成功响应 `data`：

```json
{
  "id": 1,
  "productId": 1,
  "productCode": "P001",
  "productName": "矿泉水",
  "quantity": 5,
  "operator": "李四",
  "createTime": "2026-05-07T10:00:00"
}
```

业务规则：

- 创建出库记录后，同步扣减对应商品库存。
- 商品不存在返回 `404`。
- 库存扣减后不能小于 `0`，否则返回 `400`。
- 出库记录回查失败返回 `500`。

### 8.2 查询出库记录

`GET /api/outbounds`

成功响应 `data`：

```json
[
  {
    "id": 1,
    "productId": 1,
    "productCode": "P001",
    "productName": "矿泉水",
    "quantity": 5,
    "operator": "李四",
    "createTime": "2026-05-07T10:00:00"
  }
]
```

返回空数据时 `data` 为 `[]`。

## 9. 盘点接口

### 9.1 创建盘点记录

`POST /api/stockchecks`

请求体：

```json
{
  "productId": 1,
  "actualQuantity": 98
}
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `productId` | number | 是 | 商品 ID 必须存在且存在库存记录 |
| `actualQuantity` | number | 是 | 不能小于 `0` |

成功响应 `data`：

```json
{
  "id": 1,
  "productId": 1,
  "productCode": "P001",
  "productName": "矿泉水",
  "systemQuantity": 100,
  "actualQuantity": 98,
  "difference": -2,
  "checkTime": "2026-05-07T10:00:00"
}
```

业务规则：

- `systemQuantity` 取盘点前系统库存。
- `difference = actualQuantity - systemQuantity`。
- 创建盘点记录后，同步把库存调整为 `actualQuantity`。
- 库存记录不存在返回 `404`。
- 盘点记录回查失败返回 `500`。

### 9.2 查询盘点记录

`GET /api/stockchecks`

成功响应 `data`：

```json
[
  {
    "id": 1,
    "productId": 1,
    "productCode": "P001",
    "productName": "矿泉水",
    "systemQuantity": 100,
    "actualQuantity": 98,
    "difference": -2,
    "checkTime": "2026-05-07T10:00:00"
  }
]
```

返回空数据时 `data` 为 `[]`。

## 10. 数据模型汇总

### 10.1 通用状态

| 字段值 | 说明 |
| --- | --- |
| `0` | 禁用 |
| `1` | 启用 |

### 10.2 主要响应模型

| 模型 | 字段 |
| --- | --- |
| `LoginResponse` | `token`, `username`, `roles` |
| `CurrentUserResponse` | `userId`, `username` |
| `UserDetailResponse` / `UserListItemResponse` | `id`, `username`, `realName`, `status`, `roleCodes`, `createTime` |
| `ProductDetailResponse` / `ProductListItemResponse` | `id`, `productCode`, `productName`, `category`, `purchasePrice`, `salePrice`, `status`, `createTime` |
| `StockDetailResponse` / `StockListItemResponse` | `productId`, `productCode`, `productName`, `quantity`, `minStock`, `maxStock`, `updateTime` |
| `InboundDetailResponse` / `InboundListItemResponse` | `id`, `productId`, `productCode`, `productName`, `quantity`, `operator`, `createTime` |
| `OutboundDetailResponse` / `OutboundListItemResponse` | `id`, `productId`, `productCode`, `productName`, `quantity`, `operator`, `createTime` |
| `StockCheckDetailResponse` / `StockCheckListItemResponse` | `id`, `productId`, `productCode`, `productName`, `systemQuantity`, `actualQuantity`, `difference`, `checkTime` |

## 11. 后续扩展建议

- 列表接口目前未设计分页、筛选和排序；数据量增加后建议统一增加 `pageNum`、`pageSize`、`keyword` 等查询参数。
- 当前状态更新接口使用请求体传 ID；后续可改为更 REST 风格的 `PUT /api/products/{id}/status`、`PUT /api/users/{id}/status`。
- 业务错误码目前复用 HTTP 语义数字；后续可拆分更细的业务码，如 `10001` 表示商品编码重复。
- API 文档后续可由 OpenAPI/Swagger 自动生成，减少代码和文档漂移。
