# Agent 执行提示

## 任务目标

请先阅读本任务目录的全部文件，再开始实现工作。本任务为 IT-003 商品库展示页面优化的**正式实现阶段**，包含前端卡片化重构、详情抽屉、搜索/筛选/排序、新增表单调整，以及后端商品编号自动生成与销量字段补充。

## 执行约束

- 不直接修改正式项目书、需求分析和系统设计文档正文。
- 不直接覆盖 `sql/market.sql` 或 `Document/3-项目开发/market.sql`。
- 不修改任何数据库表结构（本任务无 DDL）。
- 不新增对外接口路径（继续使用 `GET /api/products`、`POST /api/products`、`PUT /api/products/status`、`GET /api/categories/enabled` 四个现有接口）。
- 不把库存计算或库存修改规则写入商品页面。
- 不在本任务中实现折扣、促销、操作历史等扩展模块（仅在 ProductDetailDrawer 中预留挂载点注释）。
- 不引入额外路由（详情走抽屉）。
- 修改前先 `git status` 检查，避免覆盖用户未提交内容。
- 每次提交保持范围小而明确，按设计方案中的 P1-P4 阶段拆分。

## 必读文件

1. 根目录 `AGENTS.md`
2. `Document/3-项目开发/迭代优化/AGENT_WORKFLOW.md`
3. 当前任务 `README.md`
4. 当前任务 `设计方案.md`
5. 当前任务 `前端影响.md`、`后端影响.md`、`数据库变更.sql`
6. `Document/开发记录/frontend/前端开发规划.md`
7. `frontend/src/styles/tokens.css`（沿用 IT-002 设计 token）
8. `frontend/src/views/product/ProductView.vue`（当前实现，重构基准）
9. `backend/.../product/service/impl/ProductServiceImpl.java`（编号生成逻辑插入点）
10. `backend/.../product/mapper/ProductMapper.xml`（销量子查询插入点）

## 实施阶段

按以下顺序推进，每完成一阶段先验证再进入下一阶段：

### P1 后端微调

- `ProductCreateRequest.productCode` 字段保留但允许为空（去除「商品编码不能为空」校验）。
- `ProductServiceImpl.createProduct` 中：当 `request.productCode()` 为空时，调用编号生成器（格式：`P` + `yyyyMMddHHmmss` + 4 位随机数字）；唯一索引冲突时重试一次。
- `ProductView` model 与 `ProductListItemResponse` 增加 `Integer salesCount` 字段。
- `ProductMapper.xml` 的 `findAllWithCategory` 加销量子查询：
  ```sql
  LEFT JOIN (
    SELECT product_id, COALESCE(SUM(change_quantity), 0) AS sales_count
    FROM stock_log
    WHERE change_type = 'OUTBOUND'
    GROUP BY product_id
  ) s ON s.product_id = p.id
  ```
  在 SELECT 列加 `COALESCE(s.sales_count, 0) AS salesCount`。
- 单测覆盖：(a) 不传 productCode 创建商品成功且 productCode 非空；(b) 列表返回 salesCount 非 null。
- 验证：`cd backend && mvn test`。

### P2 前端基础组件骨架

- 新增 `frontend/src/utils/pinyinSort.js`，封装基于 `pinyin-pro` 的首字母提取与中文 localeCompare。
- 在 `frontend/package.json` 新增 `pinyin-pro` 依赖并 `npm install`。
- 新增 `frontend/src/views/product/components/ProductCard.vue`：props `{ product }`，emit `click`，按设计方案中的卡片结构与状态视觉实现。
- 新增 `frontend/src/views/product/components/ProductDetailDrawer.vue`：props `{ visible, product }`，emit `update:visible`、`status-changed`，分区结构（信息分区 / 状态管理分区 / 扩展占位注释）。
- 新增 `frontend/src/views/product/components/ProductFilterBar.vue`：props `{ categories }`，emit `change`（推送 `{ keyword, salePriceRange, purchasePriceRange, categoryIds, status, sortKey }`）。

### P3 ProductView 重构

- 移除 `el-table` 与原 `el-dialog` 中的「商品编号」表单项。
- 接入 ProductFilterBar，订阅 change 事件维护本地筛选状态。
- 用 computed 派生 `filteredAndSortedProducts`：先 filter 再 sort。默认 `sortKey = 'salesDesc'`。
- 使用 CSS Grid 渲染 `ProductCard` 列表。
- 接入 ProductDetailDrawer：卡片 click 时设置 `selectedProduct`、`drawerVisible = true`；详情内状态变更后调用 `loadProducts()` 刷新。

### P4 验证与回归

- 启动前后端，按 README 验收表逐条手工走查。
- 视觉与首页/库存工作台/报表 4-Tab 页面对比，确认 token 一致。
- 冒烟测试：新增商品（不填编号）→ 列表出现新卡片 → 点击进入详情 → 切换状态 → 关闭抽屉 → 卡片状态同步。
- 检查浏览器 Console 无报错、无未处理的 Promise。

## 当前阶段限制

- 本阶段实施范围仅限本设计方案；不顺手优化其他无关页面。
- 不重构 `frontend/src/api/product.js`（接口契约保持不变）。
- 不修改 IT-002 已建立的 AdminLayout 与 tokens.css。

## 输出要求

- 汇报实际创建或修改的文件清单。
- 汇报后端 `mvn test` 与前端 `npm run build` 结果（通过 / 失败原因）。
- 汇报手工验收 23 项的逐条结果。
- 汇报未完成事项与已知风险。
- 不把未验证内容表述为已完成。
- 完成后将 `00-问题清单.md` 中 IT-003 状态从「待确认」更新为「已完成」（验收通过后）。
