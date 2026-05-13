# 任务 README

## 任务基本信息

- 任务名称：业务升级迭代 A：基础资料
- 任务编号：IT-005
- 创建日期：2026-05-13
- 当前状态：待开发
- 负责人或执行 Agent：待分配

## 背景

IT-004 已完成业务升级第一阶段设计（commit `a372648`），明确了将系统升级为完整进销存结构，并把后续开发拆为 5 个独立迭代（A 基础资料 / B 进货+销售 / C 库存管理 / D 统计报表 / E 系统管理收尾）。

本任务（IT-005）是迭代 A，承担所有后续迭代的前置工作：建立主数据基座（供应商、客户）、迁移商品菜单、为后续 system 类功能预建表结构、并修复运行库与正式 SQL 的字段偏差。

详细设计依据：`Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/设计方案.md` 与 `迭代拆分.md` 中"迭代 A · 基础资料"段。

## 目标

- 新建主数据表：`supplier`、`customer`，含编码唯一约束、状态字段。
- 新建系统类表 DDL（仅建表，不接入业务逻辑）：`system_log`、`permission`、`role_permission`。
- 落地 supplier / customer 后端 CRUD：Controller / Service / Mapper / DTO / VO。
- 落地 supplier / customer 前端管理页，挂入"基础资料"菜单组。
- 商品管理菜单迁入"基础资料"，原"商品分类管理"独立菜单下线，分类选择内嵌到商品表单。
- 完成字段对账：修复 `stock.shelf_status`、`stock_log.stock_type`、`restock_order` 在代码与正式 SQL 间的偏差。

## 非目标

- 不实现角色 / 权限 / 系统日志 / 密码修改的业务逻辑（留给迭代 E）。
- 不实现进货 / 销售 / 退货 / 报损报溢的任何表与接口（留给迭代 B/C）。
- 不下线旧"入库 / 出库"前端菜单（留给迭代 E）。
- 不修改 `StockChangeService`（迭代 B 才创建）。
- 不删除既有 `inbound_order` / `outbound_order` 表。
- 不修改正式项目书、需求分析、系统设计正文。

## 涉及文件

| 类型 | 文件或目录 | 说明 |
| --- | --- | --- |
| 前端 | `frontend/src/views/master/`、`frontend/src/router/index.js`、`frontend/src/layout/AdminLayout.vue`、`frontend/src/api/` | 新增供应商/客户页面与 API 封装；商品页菜单位置调整；分类入口内嵌商品表单 |
| 后端 | `backend/src/main/java/com/supermarket/inventory/supplier/`、`.../customer/` | 新增模块；同时修复字段对账涉及的现有 Mapper / Entity |
| 数据库 | `Document/3-项目开发/迭代优化/tasks/2026-05-13-business-upgrade-iteration-a-master-data/数据库变更.sql`、`sql/market.sql`、`Document/3-项目开发/market.sql` | 本次 5 张新表 + stock/stock_log 偏差字段补齐；通过验收后同步正式 SQL |
| 文档 | 本任务目录全部文件 | 设计、影响分析、开发记录、测试记录 |

## 验收标准

- 业务行为：供应商、客户管理页支持新增、编辑、停用 / 启用、按名称 / 编码搜索；编码唯一约束生效；商品页可在表单内选分类，原分类菜单不可达。
- 数据库影响：5 张新表（`supplier`/`customer`/`system_log`/`permission`/`role_permission`）已落地运行库与正式 SQL；字段对账偏差已修复；新增表的 DDL 已 review；外键 / 索引正确。
- 前端影响：基础资料菜单组内含 3 项（供应商管理、客户管理、商品管理）；旧"商品分类管理"菜单移除；旧路由要么 redirect 要么 404。
- 后端影响：supplier / customer 模块通过单元 / 集成测试；不引入 `StockChangeService`；不改动既有 stock / inbound / outbound 业务行为。
- 测试验证：手动跑通 supplier / customer CRUD；商品页内嵌分类选择无回归；旧分类菜单不可达；字段对账修复后既有 stock / report 接口仍正常。

## 文档清单

- `AGENT_PROMPT.md`
- `设计方案.md`
- `数据库变更.sql`
- `前端影响.md`
- `后端影响.md`
- `测试记录.md`
- `开发记录.md`
