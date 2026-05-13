# Agent 执行提示

## 任务目标

请先阅读本任务目录中的全部文件，再开始分析或修改。执行目标以 `README.md` 的目标和验收标准为准。

## 执行约束

- 修改前必须检查 Git 状态，避免覆盖用户未提交内容。
- 不直接修改已验收正式文档正文，除非用户明确要求。
- 不直接覆盖 `sql/market.sql` 或 `Document/3-项目开发/market.sql`。
- 数据库结构调整先写入本任务目录的 `数据库变更.sql`，待用户确认后再评估同步正式 SQL。
- 报损 / 报溢库存变更必须复用 IT-006 已落地的 `StockChangeService`。
- 不直接在业务 Service 中 `UPDATE stock SET quantity = ...`。
- 每次变更保持范围小而明确，不混入报表、权限或系统日志收口。

## 必读文件

1. 根目录 `AGENTS.md`
2. `Document/3-项目开发/迭代优化/AGENT_WORKFLOW.md`
3. 本任务 `README.md`
4. 本任务 `设计方案.md`
5. 本任务 `前端影响.md`、`后端影响.md`、`数据库变更.sql`
6. `archive/2026-05-13-business-upgrade-phase-1-design/迭代拆分.md` 的“迭代 C”段

## 输出要求

- 汇报实际修改文件。
- 汇报验证命令和结果。
- 汇报未完成事项和风险。
- 未验证内容必须明确标记为“未验证”。
