# 任务 README

## 任务基本信息

- 任务名称：业务升级迭代 B：进货 + 销售 + StockChangeService
- 任务编号：IT-006
- 创建日期：2026-05-13
- 当前状态：已完成并归档
- 负责人或执行 Agent：Codex

## 背景

IT-004 已完成业务升级第一阶段设计，明确后续开发拆为 5 个独立迭代。IT-005（基础资料）已归档，供应商、客户、商品基础资料和 system 类基础表已作为本任务前置条件。

本任务（IT-006）是迭代 B，承担本次业务升级中最重的业务链路：把旧的单商品入库 / 出库模型升级为多商品进货、采购退货、销售、客户退货单据模型，并建立统一库存变更入口 `StockChangeService`。

详细设计依据：`Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/设计方案.md` 与 `迭代拆分.md` 中“迭代 B · 进货+销售 + StockChangeService”段。

## 目标

- 新建 8 张业务单据表：`purchase_order`、`purchase_order_item`、`purchase_return_order`、`purchase_return_order_item`、`sales_order`、`sales_order_item`、`sales_return_order`、`sales_return_order_item`。
- 扩展 `stock_log`：新增 `source_type`、`source_id`、`reason`、`operator_id`，并建立来源索引。
- 落地 `StockChangeService` 单点库存变更入口，所有新单据保存与作废都通过该入口更新库存和写入库存日志。
- 落地进货、采购退货、销售、客户退货 4 套后端模块（Controller / Service / Mapper / Entity / DTO / VO）。
- 落地 8 个前端页面：进货入库、采购退货、进货单查询、采购退货查询、销售出库、客户退货、销售单查询、客户退货查询。
- 支持多商品明细、保存即生效、单据作废反向冲销、按时间段 / 对方主体 / 单据号查询。

## 非目标

- 不做报表口径校准（留给 IT-008 / 迭代 D）。
- 不做报损报溢（留给 IT-007 / 迭代 C）。
- 不做角色权限、系统日志业务逻辑和旧菜单下线（留给 IT-009 / 迭代 E）。
- 不物理删除 `inbound_order` / `outbound_order`，旧 InboundService / OutboundService 保持可工作。
- 不修改正式项目书、需求分析、系统设计正文。

## 涉及文件

| 类型 | 文件或目录 | 说明 |
| --- | --- | --- |
| 前端 | `frontend/src/views/purchase/`、`frontend/src/views/sales/`、`frontend/src/api/`、`frontend/src/router/index.js`、`frontend/src/layout/AdminLayout.vue` | 新增进货 / 销售管理页面、API 封装、菜单和路由 |
| 后端 | `backend/src/main/java/com/supermarket/inventory/stock/`、`.../purchase/`、`.../sales/` | 新增 StockChangeService 与 4 套单据模块 |
| 数据库 | 本任务 `数据库变更.sql`、验收后再同步 `sql/market.sql` 与 `Document/3-项目开发/market.sql` | 新增 8 张表并扩展 `stock_log` |
| 文档 | 本任务目录全部文件 | 设计、影响分析、开发记录、测试记录 |

## 验收标准

- 业务行为：4 类单据可保存、可查询、可作废；保存立即影响库存；作废写入反向 `stock_log`，不删除原单据和原日志。
- 数据库影响：8 张新表与 `stock_log` 扩展字段已在任务 SQL 中明确；验收后再同步正式 SQL。
- 前端影响：进货管理与销售管理菜单各 4 项页面可用；查询页支持时间段 / 对方主体 / 单据号过滤与分页。
- 后端影响：所有新库存变化经过 `StockChangeService`；行锁并发校验下不会出现负库存；旧入库 / 出库服务仍可工作但新菜单不再调用。
- 测试验证：后端单元 / 集成测试覆盖库存正反向变更、多明细事务回滚、作废冲销、负库存拦截；前端构建通过；核心单据流程手动跑通。

## 文档清单

- `AGENT_PROMPT.md`
- `设计方案.md`
- `数据库变更.sql`
- `前端影响.md`
- `后端影响.md`
- `测试记录.md`
- `开发记录.md`
