# Inbound 模块接口测试文档

## 1. 文档目的

本文档用于记录 `inbound` 模块第一阶段公开接口的 Postman 测试内容，统一测试入口、请求内容、预期结果和实际响应记录位置。

当前阶段只覆盖已经实现的 2 个公开接口：

1. `POST /api/inbounds`
2. `GET /api/inbounds`

## 2. 测试环境说明

- 后端服务地址：`http://localhost:8080`
- 测试工具：`Postman`
- 数据库：使用当前项目 `market.sql` 初始化后的本地数据库
- 认证方式：`Authorization: Bearer <token>`

## 3. 认证说明

所有 `inbound` 模块公开接口都属于受保护接口，必须先通过登录接口获取 token，再在 Postman 中添加请求头：

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
4. 至少一个可正常入库的商品 ID
5. 至少一个不存在的商品 ID
6. 至少一个存在商品但不存在库存记录的商品 ID（如需要验证库存记录不存在场景）

建议示例数据：

- 有效 `productId`：`1`
- 无效 `productId`：`999999`
- 操作人：`管理员`

## 5. 接口测试清单

当前阶段建议覆盖以下测试用例：

1. 新增入库记录成功
2. 新增入库记录未登录访问
3. 新增入库记录商品不存在
4. 新增入库记录数量非法
5. 新增入库记录库存记录不存在
6. 入库记录列表正常查询
7. 入库记录列表未登录访问

## 6. 测试用例详情

### 6.1 新增入库记录成功

- 请求名称：新增入库记录成功
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": 1,
  "quantity": 50,
  "operator": "管理员"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/inbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 50,
  "operator": "管理员"
}
```

- 预期结果：
  - 返回 `200`
  - 响应结构为 `ApiResponse<InboundDetailResponse>`
  - `code = 0`
  - `inbound_order` 新增一条记录
  - 对应 `stock.quantity` 增加 `50`
  - `stock_log` 新增一条 `INBOUND` 记录

响应结果：



数据库校验记录：



测试结论：



### 6.2 新增入库记录未登录访问

- 请求名称：新增入库记录未登录访问
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": 1,
  "quantity": 50,
  "operator": "管理员"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/inbounds
Content-Type: application/json

{
  "productId": 1,
  "quantity": 50,
  "operator": "管理员"
}
```

- 预期结果：
  - 返回 `401`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "未登录或认证失败"`

响应结果：



测试结论：



### 6.3 新增入库记录商品不存在

- 请求名称：新增入库记录商品不存在
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": 999999,
  "quantity": 50,
  "operator": "管理员"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/inbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": 999999,
  "quantity": 50,
  "operator": "管理员"
}
```

- 预期结果：
  - 返回 `404`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "商品不存在"`
  - 不写入 `inbound_order`
  - 不修改 `stock`
  - 不写入 `stock_log`

响应结果：



数据库校验记录：



测试结论：



### 6.4 新增入库记录数量非法

- 请求名称：新增入库记录数量非法
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": 1,
  "quantity": 0,
  "operator": "管理员"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/inbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 0,
  "operator": "管理员"
}
```

- 预期结果：
  - 返回 `400`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "请求参数错误"` 或 `message = "入库数量必须大于0"`
  - 不写入 `inbound_order`
  - 不修改 `stock`

响应结果：



数据库校验记录：



测试结论：



### 6.5 新增入库记录库存记录不存在

- 请求名称：新增入库记录库存记录不存在
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": 2,
  "quantity": 50,
  "operator": "管理员"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/inbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": 2,
  "quantity": 50,
  "operator": "管理员"
}
```

- 预期结果：
  - 返回 `404`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "库存记录不存在"`
  - `inbound_order` 写入应回滚
  - 不写入 `stock_log`

响应结果：



数据库校验记录：



测试结论：



### 6.6 入库记录列表正常查询

- 请求名称：入库记录列表正常查询
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/inbounds
Authorization: Bearer {{token}}
```

- 预期结果：
  - 返回 `200`
  - 响应结构为 `ApiResponse<List<InboundListItemResponse>>`
  - `code = 0`
  - 返回字段包含 `id`、`productId`、`productCode`、`productName`、`quantity`、`operator`、`createTime`

响应结果：



测试结论：



### 6.7 入库记录列表未登录访问

- 请求名称：入库记录列表未登录访问
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/inbounds`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/inbounds
```

- 预期结果：
  - 返回 `401`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "未登录或认证失败"`

响应结果：



测试结论：



## 7. 测试记录说明

- 本文档中的“响应结果”区域用于粘贴 Postman 实际响应正文
- “数据库校验记录”区域用于记录 `inbound_order`、`stock`、`stock_log` 的实际核对结果
- 如有需要，可在测试结论区域补充截图路径、数据库校验结果或异常说明
- 当前阶段文档先提供标准测试模板，实际执行后再补录结果
