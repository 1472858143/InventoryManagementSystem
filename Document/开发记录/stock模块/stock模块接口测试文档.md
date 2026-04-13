# Stock 模块接口测试文档

## 1. 文档目的

本文档用于记录 `stock` 模块第一阶段公开接口的 Postman 测试内容，统一测试入口、请求内容、预期结果和实际响应记录位置。

当前阶段只覆盖已经实现的 3 个公开接口：

1. `GET /api/stocks`
2. `GET /api/stocks/{productId}`
3. `PUT /api/stocks/{productId}/limit`

## 2. 测试环境说明

- 后端服务地址：`http://localhost:8080`
- 测试工具：`Postman`
- 数据库：使用当前项目 `market.sql` 初始化后的本地数据库
- 认证方式：`Authorization: Bearer <token>`

## 3. 认证说明

所有 `stock` 模块公开接口都属于受保护接口，必须先通过登录接口获取 token，再在 Postman 中添加请求头：

```http
Authorization: Bearer {{token}}
```

未携带 token 或 token 无效时，预期返回：

```json
{
  "code": 401,
  "message": "未登录或认证失败",
  "data": null
}
```

## 4. 前置数据准备

测试前建议准备以下数据：

1. 已存在的登录账号和有效 token
2. 已存在的商品数据
3. 已存在的库存记录
4. 至少一条可正常查询的库存记录
5. 至少一个不存在库存记录的商品 ID

建议示例数据：

- 有效 `productId`：`1`
- 无效 `productId`：`999999`

## 5. 接口测试清单

当前阶段建议覆盖以下测试用例：

1. 库存列表正常查询
2. 库存列表未登录访问
3. 单个商品库存正常查询
4. 单个商品库存不存在
5. 库存上下限正常更新
6. 库存上下限非法更新
7. 库存上下限未登录访问

## 6. 测试用例详情

### 6.1 库存列表正常查询

- 请求名称：库存列表正常查询
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stocks`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stocks
Authorization: Bearer {{token}}
```

- 预期结果：
  - 返回 `200`
  - 响应结构为 `ApiResponse<List<StockListItemResponse>>`
  - `code = 0`

响应结果：



测试结论：



### 6.2 库存列表未登录访问

- 请求名称：库存列表未登录访问
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stocks`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stocks
```

- 预期结果：
  - 返回 `401`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "未登录或认证失败"`

响应结果：



测试结论：



### 6.3 单个商品库存正常查询

- 请求名称：单个商品库存正常查询
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stocks/1`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：
  - `productId = 1`
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stocks/1
Authorization: Bearer {{token}}
```

- 预期结果：
  - 返回 `200`
  - 响应结构为 `ApiResponse<StockDetailResponse>`
  - `code = 0`

响应结果：



测试结论：



### 6.4 单个商品库存不存在

- 请求名称：单个商品库存不存在
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stocks/999999`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：
  - `productId = 999999`
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stocks/999999
Authorization: Bearer {{token}}
```

- 预期结果：
  - 返回 `404`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "库存记录不存在"`

响应结果：



测试结论：



### 6.5 库存上下限正常更新

- 请求名称：库存上下限正常更新
- 请求方式：`PUT`
- URL：`{{baseUrl}}/api/stocks/1/limit`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：
  - `productId = 1`
- Body 示例：

```json
{
  "minStock": 10,
  "maxStock": 100
}
```

- Postman 请求内容：

```http
PUT {{baseUrl}}/api/stocks/1/limit
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "minStock": 10,
  "maxStock": 100
}
```

- 预期结果：
  - 返回 `200`
  - 响应结构为 `ApiResponse<Void>`
  - `code = 0`
  - 仅更新 `min_stock`、`max_stock`

响应结果：



测试结论：



### 6.6 库存上下限非法更新

- 请求名称：库存上下限非法更新
- 请求方式：`PUT`
- URL：`{{baseUrl}}/api/stocks/1/limit`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：
  - `productId = 1`
- Body 示例：

```json
{
  "minStock": 100,
  "maxStock": 10
}
```

- Postman 请求内容：

```http
PUT {{baseUrl}}/api/stocks/1/limit
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "minStock": 100,
  "maxStock": 10
}
```

- 预期结果：
  - 返回 `400`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "库存上限不能低于库存下限"`

响应结果：



测试结论：



### 6.7 库存上下限未登录访问

- 请求名称：库存上下限未登录访问
- 请求方式：`PUT`
- URL：`{{baseUrl}}/api/stocks/1/limit`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：
  - `productId = 1`
- Body 示例：

```json
{
  "minStock": 10,
  "maxStock": 100
}
```

- Postman 请求内容：

```http
PUT {{baseUrl}}/api/stocks/1/limit
Content-Type: application/json

{
  "minStock": 10,
  "maxStock": 100
}
```

- 预期结果：
  - 返回 `401`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "未登录或认证失败"`

响应结果：



测试结论：



## 7. 测试记录说明

- 本文档中的“响应结果”区域用于粘贴 Postman 实际响应正文
- 如有需要，可在测试结论区域补充截图路径、数据库校验结果或异常说明
- 当前阶段文档先提供标准测试模板，实际执行后再补录结果
