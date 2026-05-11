# 任务 README

## 任务基本信息

- 任务名称：库存结构拆分与商品分类标准化
- 任务编号：IT-001
- 创建日期：2026-05-09
- 当前状态：待确认
- 负责人或执行 Agent：待分配

## 背景

当前系统库存（`stock` 表）只有单一 `quantity` 字段，无法区分仓库中存放的商品与已摆上货架供顾客购买的商品。现实超市运营中，仓库货品与货架货品是两种截然不同的库存状态，补货行为（从仓库搬到货架）应被单独记录和约束，否则系统无法反映真实的库存流转。

商品分类（`product.category`）当前为自由文本字段，用户手动输入，导致同一类别出现多种写法（如"饮料"/"饮品"/"drinks"），形成脏数据，无法支持按分类进行统计和报表。

库存数量缺乏计量单位，对于按重量、容积或包装规格售卖的商品，数量字段的含义不明确，影响业务表达准确性。

## 目标

1. 将 `stock` 表的 `quantity` 拆分为 `warehouse_quantity`（仓库库存）和 `shelf_quantity`（上架库存）
2. 明确三种库存操作的业务语义：
   - 入库（inbound）→ 仓库库存增加
   - 补货（restock）→ 仓库库存减少、上架库存增加（原子操作）
   - 购买/出库（outbound）→ 上架库存减少
3. 数据库层面对所有库存字段加非负约束，补货操作需在事务中保证原子性
4. 新增 `restock_order` 表记录补货流水
5. 新增独立 `category` 分类表，商品通过外键关联，禁止商品引用不存在的分类
6. 在 `product` 表新增 `unit`（计量单位）字段，支持件、箱、kg、g、L、mL、瓶、袋等常见单位

## 非目标

- 不涉及多仓库支持，仍为单仓库模型
- 不修改用户、角色、权限相关逻辑
- 不修改价格、成本等产品财务字段
- 不实现顾客端购买界面，出库操作仍由管理员操作
- 历史 `stock_check` 记录的 `system_quantity` 语义变更不溯源（新盘点记录将对应总库存）
- 本任务不拆分为子任务，但实施时可分阶段上线（数据库→后端→前端）

## 涉及文件

| 类型 | 文件或目录 | 说明 |
| --- | --- | --- |
| 数据库 | `sql/market.sql` | 基线参考，不直接修改 |
| 数据库 | 本任务 `数据库变更.sql` | 所有 DDL 和数据迁移脚本草稿 |
| 后端 | `src/main/java/.../entity/Stock.java` | 增加 warehouseQuantity、shelfQuantity 字段 |
| 后端 | `src/main/java/.../entity/Product.java` | 增加 unit、categoryId 字段 |
| 后端 | `src/main/java/.../entity/Category.java` | 新增实体 |
| 后端 | `src/main/java/.../entity/RestockOrder.java` | 新增补货单实体 |
| 后端 | `src/main/java/.../mapper/StockMapper.java` | 修改库存查询和更新语句 |
| 后端 | `src/main/java/.../mapper/CategoryMapper.java` | 新增分类 CRUD Mapper |
| 后端 | `src/main/java/.../mapper/RestockOrderMapper.java` | 新增补货单 Mapper |
| 后端 | `src/main/java/.../service/StockService.java` | 拆分库存操作逻辑，补货事务 |
| 后端 | `src/main/java/.../service/CategoryService.java` | 新增分类管理服务 |
| 后端 | `src/main/java/.../controller/StockController.java` | 新增补货接口 |
| 后端 | `src/main/java/.../controller/CategoryController.java` | 新增分类管理接口 |
| 前端 | `frontend/src/views/stock/StockView.vue` | 显示三种库存数量，新增补货操作 |
| 前端 | `frontend/src/views/inbound/InboundView.vue` | 说明入库仅增加仓库库存 |
| 前端 | `frontend/src/views/outbound/OutboundView.vue` | 说明出库仅减少上架库存 |
| 前端 | `frontend/src/views/product/ProductView.vue` | 分类改为下拉选择，新增单位字段 |
| 前端 | `frontend/src/api/product.js` | 新增分类列表接口调用 |
| 文档 | `Document/3-项目开发/迭代优化/00-问题清单.md` | 更新状态 |

## 验收标准

- **业务行为**：
  - 入库操作后，仓库库存增加，上架库存不变
  - 补货操作后，仓库库存减少，上架库存增加，两者之和不变
  - 出库/购买操作后，上架库存减少，仓库库存不变
  - 上架库存为 0 时，出库操作被系统拒绝
  - 补货数量超过仓库库存时，补货操作被系统拒绝
- **数据库约束**：
  - `warehouse_quantity >= 0` 和 `shelf_quantity >= 0` 由 CHECK 约束强制
  - `product.category_id` 外键约束到 `category.id`，不存在的分类无法关联
  - 补货操作在事务中完成，不存在中间状态
- **前端影响**：
  - 库存页面显示三列：仓库库存 / 上架库存 / 总计（=两者之和）
  - 商品新增/编辑表单中分类为下拉选择，单位为下拉或输入
  - 补货入口可见且功能可用
- **测试验证**：
  - 入库、补货、出库各场景下库存数字正确变化
  - 边界场景（库存为 0、补货超量）被正确拒绝并提示

## 文档清单

- `AGENT_PROMPT.md`
- `设计方案.md`
- `数据库变更.sql`
- `前端影响.md`
- `后端影响.md`
- `测试记录.md`
- `开发记录.md`
