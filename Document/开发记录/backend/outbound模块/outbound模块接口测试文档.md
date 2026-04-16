# Outbound 模块接口测试文档

## 1. 文档目的

本文档用于记录 `outbound` 模块第一阶段公开接口的 Postman 测试内容，统一测试入口、请求内容、预期结果和实际响应记录位置。

当前阶段只覆盖已经实现的 2 个公开接口：

1. `POST /api/outbounds`
2. `GET /api/outbounds`

## 2. 测试环境说明

- 后端服务地址：`http://localhost:8080`
- 测试工具：`Postman`
- 数据库：使用当前项目 `market.sql` 初始化后的本地数据库
- 认证方式：`Authorization: Bearer <token>`

## 3. 认证说明

所有 `outbound` 模块公开接口都属于受保护接口，必须先通过登录接口获取 token，再在 Postman 中添加请求头：

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
4. 至少一条库存数量充足的商品记录
5. 至少一条可用于库存不足测试的商品记录
6. 至少一个不存在的商品 ID

建议 Postman 环境变量：

- `baseUrl = http://localhost:8080`
- `token = 登录后获取的 token`
- `productId = 有库存记录且库存充足的商品 ID`
- `missingProductId = 999999`

## 5. 接口测试清单

当前阶段建议覆盖以下测试用例：

1. 登录获取 token
2. 新增出库成功
3. 查询出库列表成功
4. 新增出库未登录访问
5. 查询出库列表未登录访问
6. 商品不存在
7. 出库数量非法
8. 操作人为空
9. 库存记录不存在
10. 库存不足
11. JSON 参数格式错误

## 6. 测试用例详情

### 6.1 登录获取 token

- 请求名称：登录获取 token
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/auth/login`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

- 预期结果：
  - 返回 `200`
  - `code = 0`
  - `data.token` 不为空

响应结果：



测试结论：



### 6.2 新增出库成功

- 请求名称：新增出库成功
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}},
  "quantity": 5,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}},
  "quantity": 5,
  "operator": "admin"
}
```

- 预期结果：
  - 返回 `200`
  - `code = 0`
  - 响应结构为 `ApiResponse<OutboundDetailResponse>`
  - `outbound_order` 新增记录
  - `stock.quantity` 正确减少
  - `stock_log` 新增一条 `OUTBOUND` 记录

响应结果：



测试结论：



### 6.3 查询出库列表成功

- 请求名称：查询出库列表成功
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
```

- 预期结果：
  - 返回 `200`
  - `code = 0`
  - 响应结构为 `ApiResponse<List<OutboundListItemResponse>>`
  - 返回出库记录和最小商品信息
  - 不返回当前库存数量
  - 不返回库存日志明细

响应结果：



测试结论：



### 6.4 新增出库未登录访问

- 请求名称：新增出库未登录访问
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}},
  "quantity": 5,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Content-Type: application/json

{
  "productId": {{productId}},
  "quantity": 5,
  "operator": "admin"
}
```

- 预期结果：
  - 返回 `401`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "未登录或认证失败"`

响应结果：



测试结论：



### 6.5 查询出库列表未登录访问

- 请求名称：查询出库列表未登录访问
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/outbounds
```

- 预期结果：
  - 返回 `401`
  - 响应结构为 `ApiResponse<Void>`
  - `message = "未登录或认证失败"`

响应结果：



测试结论：



### 6.6 商品不存在

- 请求名称：商品不存在
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{missingProductId}},
  "quantity": 1,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{missingProductId}},
  "quantity": 1,
  "operator": "admin"
}
```

- 预期结果：
  - 返回业务失败
  - `code = 404`
  - `message = "商品不存在"`
  - 不写入 `outbound_order`

响应结果：



测试结论：



### 6.7 出库数量非法

- 请求名称：出库数量非法
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}},
  "quantity": 0,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}},
  "quantity": 0,
  "operator": "admin"
}
```

- 预期结果：
  - 返回业务失败
  - `code = 400`
  - `message = "出库数量必须大于0"`
  - 不写入 `outbound_order`

响应结果：



测试结论：



### 6.8 操作人为空

- 请求名称：操作人为空
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}},
  "quantity": 1,
  "operator": ""
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}},
  "quantity": 1,
  "operator": ""
}
```

- 预期结果：
  - 返回业务失败
  - `code = 400`
  - `message = "操作人不能为空"`
  - 不写入 `outbound_order`

响应结果：



测试结论：



### 6.9 库存记录不存在

- 请求名称：库存记录不存在
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productIdWithoutStock}},
  "quantity": 1,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productIdWithoutStock}},
  "quantity": 1,
  "operator": "admin"
}
```

- 预期结果：
  - 返回业务失败
  - `code = 404`
  - `message = "库存记录不存在"`
  - `outbound_order` 不保留本次失败记录
  - `stock_log` 不新增记录

响应结果：



测试结论：



### 6.10 库存不足

- 请求名称：库存不足
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}},
  "quantity": 999999,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}},
  "quantity": 999999,
  "operator": "admin"
}
```

- 预期结果：
  - 返回业务失败
  - `code = 400`
  - 当前代码 message 为库存相关业务错误，例如 `"库存数量非法"`
  - `outbound_order` 不保留本次失败记录
  - `stock.quantity` 不被扣成负数
  - `stock_log` 不新增 `OUTBOUND` 记录

响应结果：



测试结论：



### 6.11 JSON 参数格式错误

- 请求名称：JSON 参数格式错误
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/outbounds`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": "abc",
  "quantity": 1,
  "operator": "admin"
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/outbounds
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": "abc",
  "quantity": 1,
  "operator": "admin"
}
```

- 预期结果：
  - 返回业务失败
  - `code = 400`
  - `message = "请求参数格式错误"`
  - 不写入 `outbound_order`

响应结果：



测试结论：



## 7. 测试记录说明

- 本文档中的“响应结果”区域用于粘贴 Postman 实际响应正文
- 如有需要，可在“测试结论”区域补充截图路径、数据库校验结果或异常说明
- 当前阶段文档先提供标准测试模板，实际执行后再补录结果
