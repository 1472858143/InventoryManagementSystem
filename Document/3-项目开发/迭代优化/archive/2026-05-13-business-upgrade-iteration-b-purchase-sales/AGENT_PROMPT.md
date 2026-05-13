# Agent 执行提示

## 任务目标

本任务（IT-006）执行业务升级迭代 B：进货 + 销售 + `StockChangeService`。所有关键设计决策已在 IT-004 完成，本任务**执行**而非重新设计。开发前必须阅读 IT-004 设计稿，禁止绕开 `StockChangeService` 直接写库存。

## 执行约束

- 不直接修改正式项目书、需求分析和系统设计文档正文。
- 不直接覆盖 `sql/market.sql` 或 `Document/3-项目开发/market.sql`，先维护本任务目录的 `数据库变更.sql`，验收后再同步。
- 修改前检查 Git 状态，避免覆盖用户未提交内容。
- 每次变更保持范围小而明确；若迭代 B 体量过大，可在本任务内记录 B1 / B2 拆分建议，但第一阶段不得跳过 `StockChangeService`。
- 旧 `InboundService` / `OutboundService` 保持可工作；旧菜单下线留给 IT-009。
- 未验证内容必须标明为待确认或待验证。

## 必读文件

1. 根目录 `AGENTS.md`
2. `Document/3-项目开发/迭代优化/AGENT_WORKFLOW.md`
3. IT-004 设计稿：
   - `Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/设计方案.md`
   - `Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/数据库变更.sql`
   - `Document/3-项目开发/迭代优化/archive/2026-05-13-business-upgrade-phase-1-design/迭代拆分.md`
4. 当前任务 `README.md`
5. 当前任务影响分析文件

## 执行步骤建议

1. 数据库变更：先将本任务 `数据库变更.sql` 中的 8 张单据表和 `stock_log` 扩展落地到运行库，验收后再同步正式 SQL。
2. 库存核心：新增 `StockChangeService` / `StockChangeCommand` / `SourceType`，实现行锁、负库存校验、`stock_log` 来源字段写入。
3. 进货链路：实现进货入库与采购退货的后端模块和前端页面。
4. 销售链路：实现销售出库与客户退货的后端模块和前端页面。
5. 查询与作废：实现 4 类单据查询页、分页过滤、作废反向冲销。
6. 验证：覆盖多商品明细、事务回滚、负库存、作废冲销、旧入库 / 出库回归。

## 输出要求

- 汇报实际修改文件清单。
- 汇报验证命令和结果。
- 汇报未完成事项和风险。
- 不把未验证内容表述为已完成。
