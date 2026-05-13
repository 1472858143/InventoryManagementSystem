# 任务 README

## 任务基本信息

- 任务名称：业务升级迭代 C：库存管理（报损报溢 + 库存报警）
- 任务编号：IT-007
- 创建日期：2026-05-14
- 当前状态：待开发
- 负责人或执行 Agent：待分配

## 背景

IT-006 已完成进货、采购退货、销售、客户退货和 `StockChangeService` 统一库存变更入口。迭代 C 在该基础上继续完善库存管理能力，将商品报损、商品报溢和库存报警纳入统一库存口径。

设计依据来自 `archive/2026-05-13-business-upgrade-phase-1-design/迭代拆分.md` 中“迭代 C · 库存管理”段，以及同目录 `设计方案.md` 中报损报溢数据模型和 `source_type=STOCK_LOSS / STOCK_OVERFLOW` 规则。

## 目标

- 新增 `stock_adjustment_order` 和 `stock_adjustment_order_item` 两张任务 SQL 草案表。
- 后端新增 `StockAdjustmentService`，创建报损 / 报溢单据时调用 `StockChangeService`。
- 报损写入 `source_type=STOCK_LOSS`，库存减少；报溢写入 `source_type=STOCK_OVERFLOW`，库存增加。
- 支持多商品明细、保存即生效、作废反向冲销。
- 新增报损、报溢、报损报溢查询和库存报警前端页面。
- 库存报警基于 `stock.quantity < min_stock OR stock.quantity > max_stock` 实时查询，不新增报警历史表。

## 非目标

- 不实现独立报警历史表、定时任务或通知推送。
- 不改造统计报表口径，留给 IT-008 / 迭代 D。
- 不实现角色权限、系统日志和旧入库 / 出库菜单下线，留给 IT-009 / 迭代 E。
- 不直接覆盖 `sql/market.sql` 或 `Document/3-项目开发/market.sql`。

## 涉及文件

| 类型 | 文件或目录 | 说明 |
| --- | --- | --- |
| 前端 | `frontend/src/views/stock/`、`frontend/src/api/`、`frontend/src/router/index.js`、`frontend/src/layout/AdminLayout.vue` | 新增报损、报溢、查询和库存报警页面入口 |
| 后端 | `backend/src/main/java/com/supermarket/inventory/stock/`、`backend/src/main/java/com/supermarket/inventory/stock/adjustment/` | 新增库存调整业务模块，复用 `StockChangeService` |
| 数据库 | 本任务 `数据库变更.sql` | 仅作为任务草案，验收后再确认是否同步正式 SQL |
| 文档 | 本任务目录全部文件 | 记录设计、影响、测试和开发过程 |

## 验收标准

- 业务行为：报损 / 报溢可创建、查询详情、分页筛选、作废；保存和作废均正确影响库存。
- 数据库影响：任务 SQL 明确两张库存调整表；不新增报警历史表。
- 前端影响：报损、报溢、报损报溢查询、库存报警 4 个入口可用。
- 后端影响：报损 / 报溢库存变更全部通过 `StockChangeService`，不直接更新 `stock.quantity`。
- 测试验证：后端单元测试覆盖报损、报溢、作废冲销、库存不足拦截；前端构建通过；核心接口手动烟测通过。

## 文档清单

- `AGENT_PROMPT.md`
- `设计方案.md`
- `数据库变更.sql`
- `前端影响.md`
- `后端影响.md`
- `测试记录.md`
- `开发记录.md`
