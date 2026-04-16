# Stockcheck 模块 Mapper 能力设计参考

## 1. 文档目的

本文档用于明确 `stockcheck` 模块第一阶段所需数据库访问能力，为后续编写 Mapper 接口与 MyBatis XML 映射提供直接参考。

当前阶段只做 Mapper 能力设计，不写 SQL 和代码实现。

## 2. 当前设计范围

当前只围绕以下接口设计 Mapper 能力：

1. `POST /api/stockchecks`
2. `GET /api/stockchecks`

## 3. 当前阶段不处理什么

当前阶段不纳入 Mapper 设计范围的内容包括：

- 盘点详情接口扩展
- 删除盘点记录
- 修改盘点记录
- 分页查询
- 条件筛选查询
- 盘点统计报表
- 库存日志查询
- 库存表直接更新

## 4. 设计约束

1. Mapper 只负责数据库访问
2. Mapper 不负责库存调整
3. Mapper 不负责库存日志写入
4. Mapper 不负责业务异常语义
5. Mapper 不直接返回 VO
6. Mapper 不计算库存差异之外的业务流程
7. 正式实现方式按项目书和现有库存相关模块风格采用 MyBatis XML

## 5. Mapper 划分建议

当前阶段建议只使用：

- `StockCheckMapper`

设计原因：

- 当前模块直接操作的主表只有 `stock_check`
- 库存调整由 `stock` 模块负责
- 商品信息只作为最小展示字段读取
- 当前阶段不需要额外拆分其它 Mapper

如需返回最小商品信息，可以增加内部查询结果模型：

- `StockCheckView`

约束：

- `StockCheckView` 只作为 Mapper 查询结果模型
- 不属于 VO
- 不直接返回前端
- 建议放在 `stockcheck.mapper.model`

## 6. 方法清单

### 6.1 插入盘点记录

建议方法名：

- `insert`

输入：

- `StockCheck stockCheck`

输出：

- `int`

用途：

- 插入 `stock_check` 主数据
- 支持主键回填，供新增成功后回查完整展示数据

### 6.2 查询盘点记录列表

建议方法名：

- `findAll`

输入：

- 无

输出：

- `List<StockCheckView>`

用途：

- 查询盘点记录列表
- 以 `stock_check` 为主表
- 最小联表 `product` 读取 `productCode`、`productName`

### 6.3 根据主键查询盘点记录

建议方法名：

- `findById`

输入：

- `Long id`

输出：

- `StockCheckView` 或 `null`

用途：

- 新增盘点记录成功后回查完整展示数据

## 7. 查询方案说明

当前阶段 `GET /api/stockchecks` 的查询方案可采用：

- 以 `stock_check` 为主表
- 最小联表 `product`
- 返回盘点记录和最小商品信息
- 按 `check_time DESC, id DESC` 排序

不允许：

- 联表 `stock_log`
- 联表入库、出库表
- 返回库存调整日志明细
- 做分页和条件筛选

## 8. 主键回填要求

`StockCheckMapper.insert(StockCheck stockCheck)` 后续实现时必须支持主键回填。

原因：

- 新增盘点成功后需要根据主键回查 `StockCheckDetailResponse`
- 便于返回包含商品最小信息的完整响应

## 9. XML 实现口径

后续编码时建议建立：

- `backend/src/main/resources/mapper/StockCheckMapper.xml`

要求：

- `namespace` 与 `StockCheckMapper` 接口完全一致
- SQL 字段使用下划线
- Java 字段使用驼峰
- 显式做字段别名映射

## 10. 本阶段结论

当前阶段 `stockcheck` 模块只需要一个 `StockCheckMapper`。

该 Mapper 只负责 `stock_check` 的读写，不承担库存调整、库存日志记录与库存差异流程编排职责。后续实现必须继续保持“盘点记录归 `stockcheck`，库存调整归 `stock`”的边界。
