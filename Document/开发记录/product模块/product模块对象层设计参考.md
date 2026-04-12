# Product 模块对象层设计参考

## 1. 文档目的

本文档用于明确 `product` 模块第一批接口所需的对象层设计，包括 Entity、DTO、VO 的名单、字段范围与职责边界。

当前阶段先完成对象设计，不进入 `Mapper`、`Service`、`Controller` 的实现。

## 2. 当前设计范围

当前只为以下接口准备对象层设计：

1. 新增商品
2. 查询商品列表
3. 上架/下架商品

## 3. 当前阶段不处理什么

当前阶段暂不为以下内容扩展对象：

- 删除商品
- 修改商品完整信息
- 商品图片
- 库存数量
- 入库/出库统计
- 分页查询对象
- 条件筛选对象

## 4. 对象层设计原则

当前阶段必须统一遵守以下原则：

1. Entity 用于数据库映射
2. DTO 用于接收请求
3. VO 用于返回前端
4. 不允许直接返回 Entity
5. `product` 模块对象中不要混入库存相关字段
6. 第一阶段保持对象简单，不引入过度嵌套结构

## 5. Entity 设计

### 5.1 Product

用途说明：

- 映射 `product` 表，用于承载商品基础数据

字段建议：

- `Long id`
- `String productCode`
- `String productName`
- `String category`
- `BigDecimal purchasePrice`
- `BigDecimal salePrice`
- `Integer status`
- `LocalDateTime createTime`

字段用途说明：

- `id`：商品主键
- `productCode`：商品编码，用于唯一标识商品
- `productName`：商品名称，用于业务展示与识别
- `category`：商品分类，用于基础分类归属
- `purchasePrice`：进价，用于表示商品采购成本
- `salePrice`：售价，用于表示商品对外销售价格
- `status`：商品状态，`0` 为下架，`1` 为上架
- `createTime`：商品创建时间

边界说明：

- 字段应严格对应数据库表结构
- 不添加业务字段
- 不编写业务方法

## 6. DTO 设计

### 6.1 ProductCreateRequest

用途说明：

- 用于接收“新增商品”请求参数

字段建议：

- `String productCode`
- `String productName`
- `String category`
- `BigDecimal purchasePrice`
- `BigDecimal salePrice`

设计说明：

- 当前阶段建议不要把 `status` 放入新增请求
- 新增商品的初始状态可在后续 Service 层统一设置
- 后续应重点对以下字段增加非空约束：
  - `productCode`
  - `productName`
  - `category`
  - `purchasePrice`
  - `salePrice`

### 6.2 ProductStatusUpdateRequest

用途说明：

- 用于接收“上架/下架商品”请求参数

字段建议：

- `Long productId`
- `Integer status`

设计说明：

- 该 DTO 只负责状态更新
- 不混入商品名称、价格、分类等字段

## 7. VO 设计

### 7.1 ProductListItemResponse

用途说明：

- 用于返回商品列表中的单个商品展示项

字段建议：

- `Long id`
- `String productCode`
- `String productName`
- `String category`
- `BigDecimal purchasePrice`
- `BigDecimal salePrice`
- `Integer status`
- `LocalDateTime createTime`

设计说明：

- 当前阶段仅返回商品基础展示字段
- 不混入库存数量
- 不混入图片、统计信息与扩展属性

### 7.2 ProductDetailResponse

用途说明：

- 当前阶段主要用于“新增商品成功后的返回对象”
- 后续如果增加商品详情接口，也可以继续复用

字段建议：

- 与 `ProductListItemResponse` 保持一致即可

设计说明：

- 当前阶段不需要额外复杂字段
- 不要加入库存字段
- 不要加入图片、统计信息或扩展属性

## 8. 对象清单汇总

### 8.1 Entity 清单

- `Product`
  - 用于映射 `product` 表，承载商品基础数据

### 8.2 DTO 清单

- `ProductCreateRequest`
  - 用于接收新增商品请求参数
- `ProductStatusUpdateRequest`
  - 用于接收商品上架/下架状态更新请求参数

### 8.3 VO 清单

- `ProductListItemResponse`
  - 用于返回商品列表中的单个展示项
- `ProductDetailResponse`
  - 用于返回新增商品成功后的商品详情结果

## 9. 本阶段结论

当前 `product` 模块第一批接口的对象层设计已经明确。

后续编码时应严格按照本文件中的 `Entity / DTO / VO` 范围推进，并保持当前阶段对象简单，不提前引入库存联动与复杂扩展字段。
