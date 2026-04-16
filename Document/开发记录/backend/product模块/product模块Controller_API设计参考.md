# Product 模块 Controller / API 设计参考

## 1. 文档目的

本文档用于明确 `product` 模块第一批接口的 Controller 结构、API 定义、认证要求、参数校验要求与统一响应规范。

当前阶段只做接口设计，不编写 Controller 代码。

## 2. 当前设计范围

当前只覆盖以下第一批接口：

1. 新增商品
2. 查询商品列表
3. 上架/下架商品

## 3. 当前阶段不处理什么

当前阶段暂不处理：

- 删除商品
- 修改商品完整信息
- 商品图片
- 分页与筛选
- 库存联动
- 分类扩展

## 4. 统一接口规范

当前阶段统一遵循以下规则：

1. 所有接口统一使用 `ApiResponse<T>`
2. 成功返回统一使用 `ApiResponse.success(data)` 或 `ApiResponse.success()`
3. 失败响应由全局异常处理器统一处理
4. 所有接口统一路径前缀为 `/api/products`
5. 所有接口均需要认证
6. token 校验由 `auth` 模块拦截器完成
7. Controller 不负责解析 token

## 5. 接口清单设计

### 5.1 新增商品

接口定义：

- URL：`POST /api/products`
- HTTP 方法：`POST`
- 请求体：`ProductCreateRequest`
- 返回类型：`ApiResponse<ProductDetailResponse>`

设计说明：

- 使用 `@PostMapping`
- 使用 `@RequestBody`
- 使用 `@Valid`
- Controller 不写业务逻辑
- 商品创建由 Service 层完成

### 5.2 查询商品列表

接口定义：

- URL：`GET /api/products`
- HTTP 方法：`GET`
- 请求参数：无
- 返回类型：`ApiResponse<List<ProductListItemResponse>>`

设计说明：

- 使用 `@GetMapping`
- 当前阶段无分页、无筛选
- 当前阶段仅返回商品基础信息

### 5.3 上架/下架商品

接口定义：

- URL：`PUT /api/products/status`
- HTTP 方法：`PUT`
- 请求体：`ProductStatusUpdateRequest`
- 返回类型：`ApiResponse<Void>`

设计说明：

- 使用 `@PutMapping`
- 只负责状态更新
- 不混入商品其他字段修改

## 6. 参数校验说明

当前阶段参数校验规则如下：

1. 使用 `@Valid` 进行基础参数校验
2. DTO 层负责非空校验与基础格式约束
3. Service 层负责业务规则校验

补充说明：

- Controller 不负责价格关系校验
- Controller 不负责商品编码唯一性校验

## 7. 认证与拦截说明

当前阶段 3 个接口均为受保护接口：

1. `POST /api/products`
2. `GET /api/products`
3. `PUT /api/products/status`

认证链路说明：

- 认证由拦截器完成
- Controller 不解析 `Authorization`
- 未登录访问返回 `401`

## 8. 响应字段约束

当前阶段返回 VO 只允许包含以下字段：

- `id`
- `productCode`
- `productName`
- `category`
- `purchasePrice`
- `salePrice`
- `status`
- `createTime`

必须强调：

- 不返回库存数量
- 不返回统计信息
- 不返回扩展字段

## 9. 接口设计总结

当前阶段接口设计具有以下特点：

- 简单 REST 风格
- 单表主线
- 无分页
- 无复杂查询
- 与 `user` 模块风格保持一致

## 10. 本阶段结论

当前 `product` 模块第一批接口的 API 设计已经完成。

后续可以进入编码实现阶段，当前接口方案已满足第一阶段最小可用能力。
