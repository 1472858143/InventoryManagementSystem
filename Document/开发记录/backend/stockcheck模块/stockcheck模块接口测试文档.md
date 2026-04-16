# Stockcheck 模块接口测试文档

## 1. 文档目的

本文档用于记录 `stockcheck` 模块第一阶段公开接口的 Postman 测试内容，统一测试入口、请求内容、预期结果和实际响应记录位置。

当前阶段只覆盖已经实现的 2 个公开接口：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

库存查询接口 `GET /api/stocks/{productId}` 仅作为盘点成功后的辅助验证接口使用。

## 2. 测试环境说明

- 后端服务地址：`http://localhost:8080`
- 测试工具：`Postman`
- 数据库：当前项目本地数据库
- 认证方式：`Authorization: Bearer <token>`

建议 Postman 环境变量：

- `baseUrl = http://localhost:8080`
- `token = 登录后获取的 token`
- `productId = 已存在库存记录的商品 ID`
- `missingProductId = 999999`

## 3. 认证说明

所有 `stockcheck` 模块公开接口都属于受保护接口，必须先通过登录接口获取 token，再在 Postman 请求头中添加：

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
4. 至少一个可用于盘点的商品 ID
5. 至少一个不存在的商品或库存记录 ID

注意：

- `stockcheck` 模块不会自动创建库存记录
- 如果商品没有对应库存记录，新增盘点会失败
- 盘点成功后，`stock.quantity` 会被调整为请求中的 `actualQuantity`

## 5. 接口测试清单

当前阶段建议覆盖以下测试用例：

1. 登录获取 token
2. 查询库存获取可用 `productId`
3. 查询盘点列表成功
4. 新增盘点成功
5. 新增后查询库存确认库存数量已调整
6. 查询盘点列表未登录访问
7. 新增盘点未登录访问
8. 缺少 `productId`
9. 缺少 `actualQuantity`
10. `actualQuantity < 0`
11. 库存记录不存在
12. JSON 参数格式错误

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
  - `code = 0`
  - `message = success`
  - `data.token` 不为空

响应结果：



测试结论：通过

### 6.2 查询库存获取可用 productId

- 请求名称：查询库存获取可用商品 ID
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stocks`
- 请求头：

```http
Authorization: Bearer {{token}}
```

- Path 参数：无
- Body 示例：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stocks
Authorization: Bearer {{token}}
```

- 预期结果：
  - `code = 0`
  - `data` 为库存列表
  - 至少存在一条可用于盘点的库存记录

响应结果：



测试结论：通过

### 6.3 查询盘点列表成功

- 请求名称：查询盘点列表成功
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stockchecks`
- 请求头：

```http
Authorization: Bearer {{token}}
```

- Path 参数：无
- Body 示例：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
```

- 预期结果：
  - `code = 0`
  - `message = success`
  - `data` 为盘点记录列表
  - 列表项包含 `id`、`productId`、`productCode`、`productName`、`systemQuantity`、`actualQuantity`、`difference`、`checkTime`

响应结果：



测试结论：通过

### 6.4 新增盘点成功

- 请求名称：新增盘点成功
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
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
  "actualQuantity": 20
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}},
  "actualQuantity": 20
}
```

- 预期结果：
  - `code = 0`
  - `message = success`
  - 响应结构为 `ApiResponse<StockCheckDetailResponse>`
  - `systemQuantity` 为盘点前系统库存
  - `actualQuantity = 20`
  - `difference = actualQuantity - systemQuantity`
  - `stock_check` 新增记录
  - `stock.quantity` 被调整为 `actualQuantity`
  - `stock_log` 新增一条 `CHECK` 记录

响应结果：



测试结论：通过

### 6.5 新增后查询库存确认数量已调整

- 请求名称：新增盘点后查询库存
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stocks/{{productId}}`
- 请求头：

```http
Authorization: Bearer {{token}}
```

- Path 参数：
  - `productId`：已盘点商品 ID
- Body 示例：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stocks/{{productId}}
Authorization: Bearer {{token}}
```

- 预期结果：
  - `code = 0`
  - `data.quantity` 等于新增盘点请求中的 `actualQuantity`

响应结果：



测试结论：通过

### 6.6 查询盘点列表未登录访问

- 请求名称：查询盘点列表未登录访问
- 请求方式：`GET`
- URL：`{{baseUrl}}/api/stockchecks`
- 请求头：无
- Path 参数：无
- Body 示例：无
- Postman 请求内容：

```http
GET {{baseUrl}}/api/stockchecks
```

- 预期结果：
  - `code = 401`
  - `data = null`

响应结果：



测试结论：通过

### 6.7 新增盘点未登录访问

- 请求名称：新增盘点未登录访问
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
- 请求头：

```http
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}},
  "actualQuantity": 10
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Content-Type: application/json

{
  "productId": {{productId}},
  "actualQuantity": 10
}
```

- 预期结果：
  - `code = 401`
  - `data = null`

响应结果：



测试结论：通过

### 6.8 缺少 productId

- 请求名称：缺少 productId
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "actualQuantity": 10
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "actualQuantity": 10
}
```

- 预期结果：
  - `code = 400`
  - `message = 商品ID不能为空`

响应结果：



测试结论：通过

### 6.9 缺少 actualQuantity

- 请求名称：缺少 actualQuantity
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
- 请求头：

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

- Path 参数：无
- Body 示例：

```json
{
  "productId": {{productId}}
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}}
}
```

- 预期结果：
  - `code = 400`
  - `message = 实际库存不能为空`

响应结果：



测试结论：通过

### 6.10 actualQuantity 小于 0

- 请求名称：实际库存小于 0
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
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
  "actualQuantity": -1
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{productId}},
  "actualQuantity": -1
}
```

- 预期结果：
  - `code = 400`
  - `message = 实际库存不能小于0`

响应结果：



测试结论：通过

### 6.11 库存记录不存在

- 请求名称：库存记录不存在
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
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
  "actualQuantity": 10
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": {{missingProductId}},
  "actualQuantity": 10
}
```

- 预期结果：
  - 返回业务异常
  - 常见结果为 `code = 404`
  - `message = 库存记录不存在`
  - 不新增 `stock_check`
  - 不调整库存

响应结果：



测试结论：通过

### 6.12 JSON 参数格式错误

- 请求名称：JSON 参数格式错误
- 请求方式：`POST`
- URL：`{{baseUrl}}/api/stockchecks`
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
  "actualQuantity": 10
}
```

- Postman 请求内容：

```http
POST {{baseUrl}}/api/stockchecks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productId": "abc",
  "actualQuantity": 10
}
```

- 预期结果：
  - `code = 400`
  - `message = 请求参数格式错误`

响应结果：



测试结论：通过

## 7. 测试结论

根据当前 Postman 手工测试结果，`stockcheck` 模块第一阶段公开接口测试通过。

当前已确认：

- 登录后可访问盘点接口
- 未登录访问盘点接口返回 `401`
- 盘点列表查询正常
- 新增盘点记录正常
- 盘点差异计算正常
- 新增盘点后库存数量调整正常
- 非法参数能够返回明确错误
- 库存记录不存在时不会新增盘点记录
- 当前对外接口范围未超出项目书要求
