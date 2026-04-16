# Product 模块设计总览

## 1. 文档目的

本文档是 `product` 模块第一阶段开发设计的统一入口文档，用于集中说明当前阶段的开发范围、模块边界、关键设计结论与相关文档的引用关系。

## 2. 当前开发范围

当前第一批接口只包括：

1. 新增商品
2. 查询商品列表
3. 上架/下架商品

当前阶段聚焦商品基础数据主线能力，不提前引入库存联动与复杂扩展能力。

## 3. 模块职责与边界总结

### 3.1 product 模块的长期职责

`product` 模块长期负责以下内容：

- 商品新增
- 商品查询
- 商品修改
- 商品上下架状态管理

### 3.2 product 模块第一阶段实际实现范围

当前第一阶段实际范围仅包括：

1. 新增商品
2. 查询商品列表
3. 上架/下架商品

### 3.3 当前阶段边界结论

当前阶段需要明确：

- 登录、token、认证不属于 `product` 模块
- 库存数量不属于 `product` 模块
- 入库、出库、盘点不属于 `product` 模块
- 库存相关能力归 `stock`、`inbound`、`outbound`、`stockcheck` 等模块处理

## 4. 统一约束总结

当前阶段必须统一遵守以下规则：

1. 所有设计以当前 `product` 表结构为准
2. 不允许擅自扩表
3. DTO 只用于接收请求
4. VO 只返回商品基础信息
5. 不允许把库存字段混入 `product` 模块 VO
6. Controller 不允许直接访问 Mapper
7. Service 负责业务校验与 VO 组装
8. Mapper 只负责数据库访问

## 5. 对象层结论摘要

### 5.1 Entity

- `Product`

### 5.2 DTO

- `ProductCreateRequest`
- `ProductStatusUpdateRequest`

### 5.3 VO

- `ProductListItemResponse`
- `ProductDetailResponse`

### 5.4 当前阶段对象结论

- `ProductCreateRequest` 当前阶段不包含 `status`
- 新增商品的默认 `status` 由 Service 层统一设置
- `Product` 相关 VO 不包含库存字段
- `Product` 相关 VO 不包含图片
- `Product` 相关 VO 不包含统计信息

## 6. Mapper 结论摘要

当前阶段 `ProductMapper` 的最小能力包括：

- `findByProductCode`
- `insert`
- `findAll`
- `findById`
- `updateStatusById`

当前阶段说明如下：

- 当前阶段只需要一个 `ProductMapper`
- 当前阶段列表查询可以直接使用单表查询
- 当前阶段不做分页
- 当前阶段不做筛选

## 7. Service 结论摘要

当前阶段 `ProductService` 的核心方法包括：

- `createProduct`
- `listProducts`
- `updateProductStatus`

### 7.1 createProduct 关键结论

主要校验点包括：

- 商品编码唯一
- 价格合法性
- 售价不能低于进价

### 7.2 listProducts 关键结论

- 当前阶段只做简单的 VO 转换
- 当前阶段不需要复杂聚合逻辑

### 7.3 updateProductStatus 关键结论

- 当前阶段只更新 `status`
- 不混入商品名称、价格、分类等字段修改

### 7.4 事务结论

- 当前第一阶段接口基于单表主线，不引入复杂事务编排
- 若后续扩展商品图片、分类扩展表、库存初始化等能力，再重新评估事务需求

## 8. API 结论摘要

当前阶段接口包括：

1. `POST /api/products`
2. `GET /api/products`
3. `PUT /api/products/status`

统一约束如下：

- 三个接口都必须经过 `auth` 模块认证
- Controller 不解析 token
- 当前阶段不做分页
- 当前阶段不做筛选
- 当前阶段不做复杂查询

## 9. 当前阶段未纳入范围的内容

当前阶段暂不纳入：

- 删除商品
- 修改商品完整信息
- 商品图片
- 分页查询
- 条件筛选查询
- 库存联动
- 分类扩展

## 10. 文档引用关系

本总览文档对应以下详细设计文档：

1. `product模块开发注意事项.md`
2. `product模块对象层设计参考.md`
3. `product模块Mapper能力设计参考.md`
4. `product模块Service流程设计参考.md`
5. `product模块Controller_API设计参考.md`

使用方式说明：

- 总览文档用于快速查看整体设计结论
- 详细文档用于后续编码前逐项核对

## 11. 开发顺序

当前阶段建议的开发顺序为：

1. 开发注意事项
2. 对象层设计
3. Mapper 能力设计
4. Service 流程设计
5. Controller / API 设计
6. 进入编码实现与测试

必须强调：

- 不允许跳过顺序
- 不允许直接从 Controller 开始开发

## 12. 本阶段结论

`product` 模块第一阶段设计已经完成收口，当前已形成可进入编码阶段的统一设计基线。

后续编码应严格依据本目录中的设计文档推进。

当前第一阶段设计已完成落地，并已通过代码结构检查与编译验证。

## 13. 本文件作用

本文档用于快速查看 `product` 模块当前阶段整体设计结论。

编码前应优先从本文件开始，再按需查看对象层、Mapper、Service 与 Controller / API 的详细设计文档。
