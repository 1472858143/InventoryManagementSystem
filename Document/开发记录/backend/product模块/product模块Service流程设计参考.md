# Product 模块 Service 流程设计参考

## 1. 文档目的

本文档用于明确 `product` 模块当前阶段的 Service 层流程设计，包括方法清单、业务流程、校验点、异常语义与事务边界。

当前阶段只做流程设计，不实现代码。

## 2. 当前设计范围

当前只设计以下第一批接口对应的 Service 流程：

1. 新增商品
2. 查询商品列表
3. 上架/下架商品

## 3. 当前阶段不处理什么

当前阶段暂不纳入 Service 流程设计的内容包括：

- 删除商品
- 修改商品完整信息
- 商品图片
- 分页查询
- 条件筛选
- 库存联动
- 分类管理扩展

## 4. ProductService 方法清单

当前阶段建议提供以下 Service 方法：

### 4.1 createProduct

- 方法名：`createProduct`
- 输入：`ProductCreateRequest`
- 输出：`ProductDetailResponse`
- 用途：创建商品并返回新增后的商品详情结果

### 4.2 listProducts

- 方法名：`listProducts`
- 输入：无
- 输出：`List<ProductListItemResponse>`
- 用途：查询商品基础信息列表

### 4.3 updateProductStatus

- 方法名：`updateProductStatus`
- 输入：`ProductStatusUpdateRequest`
- 输出：`void`
- 用途：更新商品上架/下架状态

## 5. 新增商品（createProduct）流程设计

### 5.1 输入与输出

- 输入：`ProductCreateRequest`
- 输出：`ProductDetailResponse`

### 5.2 业务流程

1. 接收 `ProductCreateRequest`
2. 调用 `ProductMapper.findByProductCode` 校验商品编码是否已存在
3. 若商品编码已存在，抛出 `BusinessException`
4. 校验商品名称、分类、价格字段是否合法
5. 校验价格关系：
   - `purchasePrice >= 0`
   - `salePrice >= 0`
   - `salePrice >= purchasePrice`
6. 组装 `Product`
   - `productCode`
   - `productName`
   - `category`
   - `purchasePrice`
   - `salePrice`
   - `status` 建议统一设置为 `1`
7. `createTime` 如由数据库生成，则不手动赋值
8. 调用 `ProductMapper.insert(product)`
9. 如有必要，再调用 `ProductMapper.findById(product.getId())` 获取完整数据
10. 组装 `ProductDetailResponse` 并返回

### 5.3 参数/格式校验说明

更偏参数校验的内容包括：

- 商品编码是否为空
- 商品名称是否为空
- 分类是否为空
- 进价是否为空
- 售价是否为空

### 5.4 业务校验说明

更偏业务校验的内容包括：

- 商品编码是否唯一
- 进价不能小于 `0`
- 售价不能小于 `0`
- 售价不能低于进价

### 5.5 为什么当前阶段通常不需要复杂事务编排

当前阶段新增商品只涉及 `product` 单表写入，因此通常不需要复杂事务编排。

补充说明：

- 后续如果商品模块扩展到图片、分类扩展表或库存初始化等多表联动场景，再视业务扩展重新评估事务需求

### 5.6 异常语义

当前阶段至少需要覆盖以下异常语义：

- 商品编码已存在
- 进价非法
- 售价非法
- 售价低于进价

## 6. 查询商品列表（listProducts）流程设计

### 6.1 输入与输出

- 输入：无
- 输出：`List<ProductListItemResponse>`

### 6.2 业务流程

1. 调用 `ProductMapper.findAll()`
2. 若结果为空，直接返回空列表
3. 遍历 `Product` 列表
4. 组装 `List<ProductListItemResponse>`
5. 返回结果

### 6.3 当前阶段说明

- 当前阶段不做分页
- 当前阶段不做筛选
- 当前阶段只返回商品基础信息
- 当前阶段不混入库存数量或库存统计字段

### 6.4 为什么当前阶段只需要简单 VO 转换

当前阶段 `product` 模块只依赖单表，不涉及角色关系聚合、库存联动或复杂统计，因此 Service 层只需要完成基础的对象转换与返回结构组装。

### 6.5 为什么当前阶段不需要复杂聚合逻辑

当前阶段商品列表只包含商品自身字段，不需要跨表拼装，因此不需要复杂聚合逻辑。

## 7. 上架/下架商品（updateProductStatus）流程设计

### 7.1 输入与输出

- 输入：`ProductStatusUpdateRequest`
- 输出：成功结果或空返回

### 7.2 业务流程

1. 接收 `ProductStatusUpdateRequest`
2. 校验 `status` 是否合法，只允许 `0` 或 `1`
3. 调用 `ProductMapper.findById(productId)` 校验商品是否存在
4. 若商品不存在，抛出 `BusinessException`
5. 调用 `ProductMapper.updateStatusById(productId, status)`
6. 返回成功结果

### 7.3 约束说明

- 该流程只允许修改 `status`
- 不混入商品名称、价格、分类修改
- 当前阶段不需要事务

### 7.4 异常语义

当前阶段至少需要覆盖以下异常语义：

- `status` 非法
- 商品不存在

## 8. 业务规则与校验点总结

当前阶段 Service 层需要负责的核心业务规则包括：

1. 商品编码唯一
2. 商品名称不能为空
3. 分类不能为空
4. 进价不能小于 `0`
5. 售价不能小于 `0`
6. 售价不能低于进价
7. `status` 只能为 `0` 或 `1`

区分说明：

### 8.1 更偏参数校验的内容

- 商品编码非空
- 商品名称非空
- 分类非空
- 进价非空
- 售价非空
- `status` 非空

### 8.2 Service 必须兜底的业务校验

- 商品编码唯一
- 进价与售价关系合法
- `status` 取值合法

## 9. 异常处理规则

当前阶段 `product` 模块至少需要以下 `BusinessException` 语义：

- 商品编码已存在
- 商品不存在
- 进价非法
- 售价非法
- 售价低于进价
- `status` 非法

说明：

- 本阶段只描述异常语义
- 不在本文件中展开具体异常实现代码

## 10. 事务边界总结

### 10.1 新增商品

- 单表写入
- 当前阶段通常不需要复杂事务编排

### 10.2 查询商品列表

- 不需要事务

### 10.3 上架/下架商品

- 单表更新
- 不需要事务

补充说明：

- 若后续业务扩展为多表联动，再视情况重新评估事务边界

## 11. 职责边界总结

### 11.1 Controller 负责

- 接收请求
- 参数校验
- 调用 Service
- 返回统一响应

### 11.2 Service 负责

- 业务校验
- 异常语义控制
- `Product` 组装
- VO 组装

### 11.3 Mapper 负责

- 数据库访问

补充说明：

- Service 不负责 token 解析
- Service 不负责 HTTP 细节

## 12. 本阶段结论

当前 `product` 模块第一批接口的 Service 流程已经明确。

后续编码时应严格按照本文件推进，并坚持当前阶段“单表主线、简单对象、简单查询”的原则，不提前引入库存联动与复杂扩展能力。
