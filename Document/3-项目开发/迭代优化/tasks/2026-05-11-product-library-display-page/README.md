# 任务 README

## 任务基本信息

- 任务名称：商品库展示页面优化（卡片化重构 + 检索能力 + 详情抽屉）
- 任务编号：IT-003
- 创建日期：2026-05-11
- 当前状态：待开发（已完成设计，待进入实现）
- 负责人或执行 Agent：Claude Code

## 背景

IT-002 完成后，全站视觉与导航体系（设计 token、Lucide 图标、AdminLayout、首页驾驶舱、库存双层流转、BI 仪表盘）已建立。商品库页面 (`ProductView.vue`) 仍为单文件传统表格形态，存在四类问题：

1. 表格形态视觉单调，与全站现代化基线脱节。
2. 商品状态识别度弱，详情字段散落在表格列中，缺乏后续功能扩展承载位（折扣、促销、库存联动等）。
3. 没有搜索、筛选、排序能力，商品检索成本随数据量上升急剧增加。
4. 新增商品要求手填「商品编号」，是无业务价值的登记摩擦点。

本任务对商品库页面进行整体展示与交互优化，是 IT-002 的视觉基线在 product 模块的延伸落地。

## 目标

1. 将商品列表从表格替换为卡片网格，状态用「边框颜色 + 文字」双通道表达。
2. 商品详情从表格列展示改为右侧抽屉悬浮层，独立组件承载，预留后续功能扩展槽位。
3. 在前端引入搜索、多条件筛选、多键排序，默认按销量降序展示。
4. 移除新增商品流程中的「商品编号」手工填写，由后端自动生成。
5. 视觉与 IT-002 设计 token 体系一致，保持现代化、简洁、美观的后台管理系统风格。

## 非目标

- 本任务不引入分页（毕设演示数据量在前端处理范围内）。
- 本任务不新增路由，详情仅以抽屉形式承载。
- 本任务不实现折扣管理、促销活动、操作历史等扩展模块（仅在抽屉中预留挂载点）。
- 本任务不变更 `product`、`stock_log`、`category` 任何表结构，不写 DDL。
- 本任务不修改库存模块、报表模块、首页驾驶舱等其他页面。
- 本任务不引入第三方设计系统组件库（继续基于 Element Plus + IT-002 token）。

## 范围

| 维度 | 范围 |
| --- | --- |
| 前端 | `frontend/src/views/product/` 目录全部、`frontend/package.json` 新增 pinyin-pro 依赖 |
| 后端 | `product` 模块的 ProductCreateRequest、ProductServiceImpl、ProductListItemResponse、ProductView model、ProductMapper.xml |
| 数据库 | 无变更 |
| 文档 | 本任务目录全部文件、`00-问题清单.md` 状态更新 |
| 测试 | 后端 ProductService 单测覆盖编号自动生成与销量字段；前端手工走查 23 项验收点 |

## 涉及文件

| 类型 | 文件或目录 | 说明 |
| --- | --- | --- |
| 前端 | `frontend/src/views/product/ProductView.vue` | 重构为页面容器：数据加载 + 工具栏 + 卡片网格 + 抽屉协调 |
| 前端 | `frontend/src/views/product/components/ProductCard.vue` | 新增：单个商品卡片 |
| 前端 | `frontend/src/views/product/components/ProductDetailDrawer.vue` | 新增：商品详情抽屉 |
| 前端 | `frontend/src/views/product/components/ProductFilterBar.vue` | 新增：搜索/筛选/排序工具栏 |
| 前端 | `frontend/src/utils/pinyinSort.js` | 新增：首字母排序辅助（基于 pinyin-pro） |
| 前端 | `frontend/package.json` | 新增依赖 `pinyin-pro` |
| 前端 | `frontend/src/api/product.js` | 不变（继续使用现有 4 个接口） |
| 后端 | `backend/.../product/dto/ProductCreateRequest.java` | productCode 改为可选 |
| 后端 | `backend/.../product/service/impl/ProductServiceImpl.java` | createProduct 自动生成编号；移除「商品编码不能为空」校验 |
| 后端 | `backend/.../product/vo/ProductListItemResponse.java` | 新增 `salesCount` 字段 |
| 后端 | `backend/.../product/model/ProductView.java` | 新增 `salesCount` 字段 |
| 后端 | `backend/.../product/mapper/ProductMapper.java` | 当前仓库为注解式 Mapper，findAllWithCategory 加销量子查询 |
| 数据库 | 无 | 不动表结构 |
| 文档 | 本任务目录 | 设计、影响、变更与开发记录 |

## 验收标准

业务行为验证逐条对应需求 1-23：

| 编号 | 验收点 | 验证方式 |
| --- | --- | --- |
| 1 | 商品列表以卡片形式展示，原表格被完全替换 | 打开 `/products` 页面观察 |
| 2 | 卡片尺寸合理，至少 1080p 屏幕下每行可容纳 4-5 张 | 浏览器响应式查看 |
| 3 | 卡片显示商品名称、商品分类、商品状态 | 视觉检查 |
| 4 | 状态用左侧边框颜色 + 状态文字双通道表达，上架绿、下架灰 | 视觉检查（含两种状态商品） |
| 5 | 卡片整体可点击，点击触发详情 | 鼠标点击 |
| 6 | 详情中展示商品全部字段（编号/名称/分类/单位/进价/售价/状态/创建时间） | 详情抽屉内容核对 |
| 7 | 详情中可执行上下架状态操作 | 在详情中切换状态后关闭抽屉，主页面卡片状态同步刷新 |
| 8 | 详情区域结构支持后续扩展折扣等模块 | 代码 review：分区结构 + 预留挂载点注释 |
| 9 | 详情兼具扩展能力与美观度 | 视觉 review |
| 10 | 详情以悬浮层形式展示 | 视觉检查（el-drawer 抽屉） |
| 11 | 详情不使用页面跳转 | URL 不变化 |
| 12 | 详情独立拆分为 ProductDetailDrawer.vue | 代码 review |
| 13 | 整体保持现代化简洁美观风格 | 与 IT-002 token 一致性 review |
| 14 | 新增商品弹窗删除「商品编号」字段 | 打开新增弹窗确认 |
| 15 | 新增流程不再要求手填编号 | 提交无编号字段也能成功 |
| 16-17 | 搜索框支持商品名称和分类的模糊匹配 | 输入关键词测试 |
| 18-20 | 筛选支持售价区间、进价区间、分类、状态四种条件多选组合 | 组合多条件验证 |
| 21-22 | 排序支持销量、商品名首字母、分类首字母、售价升降 5 种 | 切换排序键验证顺序 |
| 23 | 默认按销量降序展示 | 首次进入页面默认排序为销量 |

其他验收：

- 后端：ProductService 单测覆盖「创建时不传 productCode 自动生成」「列表返回销量字段」两个用例。
- 后端：`mvn test` 全部通过。
- 前端：`npm run build` 通过；新增依赖 pinyin-pro 已在 package.json 与 lock 文件登记。
- 文档：本任务目录设计、前端影响、后端影响、数据库变更、开发记录均已填充。
- 任务结束后，`00-问题清单.md` 中 IT-003 状态由「待确认」改为「已完成」（验收后）。

## 文档清单

- `AGENT_PROMPT.md`
- `设计方案.md`
- `数据库变更.sql`
- `前端影响.md`
- `后端影响.md`
- `测试记录.md`
- `开发记录.md`

## 已确认实现细节

2026-05-12 用户确认：

1. 商品编号自动生成格式：`P` + `yyyyMMddHHmmss` + 4 位随机数字。
2. 卡片底部显示销量数字（小字辅助信息）。
3. 详情抽屉扩展槽位以注释预留挂载点，不渲染「即将上线」占位。

设计阶段全部决策已闭环，可直接进入 AGENT_PROMPT 中 P1-P4 实施流程。
