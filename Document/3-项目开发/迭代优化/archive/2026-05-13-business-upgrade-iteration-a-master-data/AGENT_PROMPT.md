# Agent 执行提示

## 任务目标

本任务（IT-005）执行业务升级迭代 A：基础资料。所有设计决策已在 IT-004 完成，本任务**执行**而非**设计**。开发前必读 IT-004 设计稿，禁止重新决策已确定事项。

## 执行约束

- 不直接修改正式项目书、需求分析和系统设计文档正文。
- 不直接覆盖 `sql/market.sql` 或 `Document/3-项目开发/market.sql`，先写本任务目录的 `数据库变更.sql`，验收后再同步。
- 修改前检查 Git 状态，避免覆盖用户未提交内容。
- 每次变更保持范围小而明确。
- 未验证内容必须标明为待确认或待验证。
- 不进入迭代 B / C / D / E 的范围（详见 README 非目标）。

## 必读文件

1. 根目录 `AGENTS.md`
2. `Document/3-项目开发/迭代优化/AGENT_WORKFLOW.md`
3. **IT-004 设计稿**（重点）：
   - `Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/设计方案.md`（核心决策清单、§1 菜单结构、§2 数据模型）
   - `Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/数据库变更.sql`（"迭代 A 落地"段为本次 DDL 蓝本）
   - `Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/迭代拆分.md`（迭代 A 段为范围 / 验收边界 / 不算完成项）
4. 当前任务 `README.md`
5. 当前任务影响分析文件

## 执行步骤建议

1. **入口动作 — 字段对账（必先完成）**
   - 核对运行库、`sql/market.sql`、`Document/3-项目开发/market.sql` 三方差异。
   - 重点字段：`stock.shelf_status`、`stock_log.stock_type`。
   - 重点 Mapper：`restock_order` 相关。
   - 修复策略写入本任务 `开发记录.md` 决策记录段。

2. **数据库变更**
   - 把 IT-004 `数据库变更.sql` 中"迭代 A 落地"段的 5 张表 DDL 复制到本任务 `数据库变更.sql`，按需微调字段类型 / 索引。
   - 落地到运行库 + `sql/market.sql` + `Document/3-项目开发/market.sql`。

3. **后端开发**
   - 新增 supplier / customer 两个模块（Controller / Service / Mapper / Entity / DTO / VO）。
   - 不引入 StockChangeService（留给迭代 B）。

4. **前端开发**
   - 新增基础资料菜单组（供应商管理 / 客户管理 / 商品管理）。
   - 商品管理菜单从原位置迁入基础资料组。
   - 原"商品分类管理"菜单下线；分类选择内嵌商品表单。
   - 新增 supplier / customer API 封装与页面。

5. **测试与验证**
   - supplier / customer CRUD 手动跑通。
   - 商品页内嵌分类选择回归。
   - 字段对账修复后既有 stock / report 接口不退化。
   - 验证结果写入 `测试记录.md`。

## 输出要求

- 汇报实际修改文件清单。
- 汇报验证命令和结果。
- 汇报未完成事项和风险。
- 不把未验证内容表述为已完成。
- 完成后等待用户验收，再决定是否归档到 `archive/`。
