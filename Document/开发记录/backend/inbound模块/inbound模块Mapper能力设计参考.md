# Inbound 模块 Mapper 能力设计参考

## 1. 文档目的

本文档用于明确 `inbound` 模块当前阶段最小 Mapper 能力，为后续编写 Mapper 接口与 XML 映射文件提供直接参考。

当前阶段只做能力设计，不写 SQL 和代码实现。

## 2. 当前设计范围

当前只围绕以下接口设计 Mapper 能力：

1. `POST /api/inbounds`
2. `GET /api/inbounds`

## 3. 当前阶段不处理什么

当前阶段不纳入以下 Mapper 设计范围：

- 入库详情查询扩展
- 分页查询
- 条件筛选查询
- 统计报表查询
- 库存日志查询
- 直接库存更新

## 4. 设计约束

当前阶段必须遵守以下约束：

1. Mapper 只负责数据库访问
2. Mapper 不负责业务校验
3. Mapper 不负责库存增加
4. Mapper 不负责事务控制
5. Mapper 不直接返回 VO
6. 正式实现方式按项目书采用 MyBatis XML

## 5. Mapper 划分建议

根据项目书，`inbound` 模块当前阶段只需要：

- `InboundOrderMapper`

原因如下：

- `inbound` 当前阶段只直接操作 `inbound_order`
- `stock` 和 `stock_log` 的写操作属于 `stock` 模块职责
- `inbound` 不应额外声明库存表写入 Mapper 能力

## 6. 方法清单

### 6.1 插入入库记录

建议方法名：

- `insert`

输入：

- `InboundOrder inboundOrder`

输出：

- `int`

用途：

- 插入入库主数据
- 后续实现时必须支持主键回填

### 6.2 查询入库列表

建议方法名：

- `findAll`

输入：

- 无

输出：

- `List<InboundOrderView>` 或等价的内部查询结果模型

用途：

- 查询入库记录列表
- 如列表需要展示商品编码、商品名称，可做最小商品信息关联查询

### 6.3 根据主键查询入库记录

建议方法名：

- `findById`

输入：

- `Long id`

输出：

- `InboundOrderView` 或 `InboundOrder`

用途：

- 新增成功后回显完整结果时复用

说明：

- 若实现阶段确认无需单独回查，可保留为可选方法
- 设计文档中先允许保留，不强制要求最终一定实现

## 7. 查询方案说明

当前阶段 `GET /api/inbounds` 的查询方案可采用：

- 以 `inbound_order` 为主表
- 按展示需要最小化关联 `product` 读取 `productCode`、`productName`

必须强调：

- 这是入库展示层面的最小信息补充
- 不代表 `inbound` 模块承担商品或库存管理职责
- 不联动 `stock_log`
- 不做分页
- 不做条件筛选

## 8. XML 映射要求

根据项目书，后续正式实现应采用 MyBatis XML：

- `InboundOrderMapper.xml`

位置应在：

- `src/main/resources/mapper/`

必须保证：

- `namespace` 与 Mapper 接口一致
- 方法名与接口方法一致
- 参数名与 XML 中使用的参数名一致

## 9. 主键回填要求

`insert(InboundOrder inboundOrder)` 后续实现时必须支持主键回填。

原因如下：

- 新增入库记录成功后，可能需要回显新增结果
- Service 层需要具备拿到新记录主键的能力

## 10. 本阶段结论

`inbound` 模块第一阶段当前只需要一个 `InboundOrderMapper`。

该 Mapper 只负责 `inbound_order` 的读写，不承担库存增加与库存日志记录职责。后续实现时必须继续保持“入库记录归 `inbound`，库存变更归 `stock`”的边界。
