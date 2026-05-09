# 任务 README

## 任务基本信息

- 任务名称：前端整体重设计
- 任务编号：IT-002
- 创建日期：2026-05-10
- 当前状态：设计已确认，待实施
- 负责人或执行 Agent：Claude Code (Sonnet 4.6 / Opus 4.7)

## 背景

项目前端在 IT-001 完成"库存结构拆分（仓库/上架）+ 商品分类标准化"后，进入持续迭代优化阶段。用户在使用现有前端时识别出以下事实：

- 侧边栏 10 项菜单平铺无分组，业务相关项（商品/分类、库存/入库/出库/盘点）没有归类，"用户管理"作为系统功能却排在第二位。
- 页面以表格 + 表单为主，缺少对超市运营场景的业务表达（缺货预警、补货提示、滞销商品、决策辅助）。
- 报表页只有 3 个图表，无法体现多维度商业分析和决策辅助。
- IT-001 引入的 `shelf_quantity` 精确数量字段在真实超市运营中并不合理：货架上的数量随顾客购买实时变化，无法精确追踪，理货员实际工作中只关注"陈列状态"（缺货/较少/充足/未上架）。
- 整体视觉沿用 Element Plus 默认主题，配色和图标偏后台管理通用风格，缺少现代感和差异化。

本任务定位为毕业设计演示系统的前端重构，重点是"真实超市质感 + 完整业务故事 + 关键场景跑通"，不追求全场景覆盖或真实运营级容错。

## 目标

- 重构前端整体信息架构，建立 4 个一级菜单分组：商品管理 / 库存管理 / 报表分析 / 系统。
- 修正 IT-001 的部分数据模型：将"仓库数量 + 货架数量"双精确数字改为"总库存数量 + 陈列状态枚举"，更贴近真实超市运营。
- 引入现代视觉系统：浅色 + 靛蓝 `#6366F1` 主调、Lucide 线性图标、统一设计令牌。
- 端到端实现两个核心业务场景：
  1. 库存数量 + 陈列状态双层流转（入库 → 上架 → 销售 → 缺货预警 → 补货）
  2. 商业智能仪表盘（库存周转、滞销识别、补货建议、销售热点等）
- 保持原有页面功能不丢失，所有原 10 个页面在新骨架上跑通。

## 非目标

- 不引入会员/客户管理。
- 不做供应商管理（保留为未来扩展）。
- 不做高频键盘快捷键、批量扫码、多角色权限矩阵。
- 不做移动端适配，保留桌面端为主。
- 不重写后端业务逻辑，只做配合数据模型修正的最小改动。

## 涉及文件

| 类型 | 文件或目录 | 说明 |
| --- | --- | --- |
| 前端 | `frontend/src/router/index.js` | 路由结构按新 IA 调整 |
| 前端 | `frontend/src/layout/AdminLayout.vue` | 侧边栏分组、视觉令牌、Lucide 图标 |
| 前端 | `frontend/src/views/**/*.vue` | 各页面在新视觉骨架上重构 |
| 前端 | `frontend/src/api/stock.js` | 字段变更适配（quantity / shelfStatus） |
| 前端 | `frontend/src/api/report.js` | 仪表盘新接口封装 |
| 前端 | 新增 `frontend/src/styles/tokens.css` | 设计令牌（颜色、间距、圆角、阴影） |
| 前端 | 新增 `frontend/src/views/stock/StockWorkspaceView.vue` | 库存管理工作台 |
| 前端 | 新增 `frontend/src/views/stock/ShelfRestockView.vue` | 上架补货页面 |
| 前端 | 新增 `frontend/src/views/report/*` | 仪表盘多 Tab 子页 |
| 后端 | `backend/src/main/java/com/supermarket/inventory/stock/entity/Stock.java` | 字段重命名 + 新增 shelfStatus |
| 后端 | `backend/src/main/java/com/supermarket/inventory/stock/mapper/StockMapper.java` 与 XML | 字段适配 |
| 后端 | `backend/src/main/java/com/supermarket/inventory/stock/service/**` | 业务逻辑适配 |
| 后端 | `backend/src/main/java/com/supermarket/inventory/report/**` | 仪表盘接口扩展 |
| 数据库 | `tasks/2026-05-10-frontend-redesign/数据库变更.sql` | schema 迁移草案 |
| 文档 | `Document/2-系统设计/API接口设计文档.md` | 验收后同步更新 |
| 文档 | `Document/2-系统设计/数据库设计汇总.md` | 验收后同步更新 |

## 验收标准

- 业务行为：
  - 5 个 Phase 全部完成并可独立演示对应场景。
  - 库存数量 + 陈列状态双层流转端到端跑通：入库增加 quantity，上架补货改 shelf_status，出库扣减 quantity，缺货时工作台显示预警。
  - 商业智能仪表盘可演示库存周转率、滞销识别、补货建议、销售热点 4 类决策辅助。
- 数据库影响：
  - `stock` 表 `warehouse_quantity` 重命名为 `quantity`，`shelf_quantity` 删除，新增 `shelf_status` 枚举字段（默认 `未上架`）。
  - 数据迁移脚本可重复执行（IF EXISTS 检查），且包含从 `warehouse_quantity + shelf_quantity` 计算 `quantity` 的回填语句。
- 前端影响：
  - 侧边栏分组按新 IA 呈现，用户管理不再出现在第二位。
  - 全站视觉统一为浅色 + 靛蓝 + Lucide 图标。
  - 库存工作台、上架补货为新增页面，库存总览和报表分析重设计。
- 后端影响：
  - Stock entity / Mapper / Service / Controller 字段适配。
  - 报表模块增加仪表盘所需聚合接口。
  - 单元测试 + Spring Boot 启动测试通过。
- 测试验证：
  - `mvn test` 通过。
  - `npm run build` 通过。
  - 手动测试覆盖两个核心场景的端到端流程。

## 文档清单

- `AGENT_PROMPT.md`
- `设计方案.md`
- `数据库变更.sql`
- `前端影响.md`
- `后端影响.md`
- `测试记录.md`
- `开发记录.md`
