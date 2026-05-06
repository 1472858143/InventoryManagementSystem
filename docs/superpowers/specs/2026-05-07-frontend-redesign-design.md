# 前端重构设计文档

**日期**：2026-05-07  
**项目**：超市库存管理系统  
**分支**：feature/frontend-product  
**状态**：已确认，待实现

---

## 一、背景与目标

后端已全部开发完成。前端现有骨架（路由、认证、请求封装）可保留，但所有页面样式过于朴素，需要全面重制为视觉更佳的后台管理系统界面。已完成的页面（LoginView、UserView、ProductView）也可删除重做。

**目标**：
- 引入 Element Plus 组件库，提升视觉品质
- 重制 AdminLayout 为深色侧栏 + 白色顶栏风格
- 完成全部 9 个模块页面（login / user / product / stock / inbound / outbound / stockcheck / report / system）
- 首页 Dashboard 展示统计卡片 + ECharts 图表
- 报表页展示 ECharts 图表

---

## 二、技术栈变更

### 新增依赖

| 包 | 用途 |
|---|---|
| `element-plus` | UI 组件库 |
| `@element-plus/icons-vue` | Element Plus 配套图标 |
| `echarts` | 图表（Dashboard + 报表页） |
| `unplugin-auto-import` | 自动按需导入 Element Plus 组合式 API |
| `unplugin-vue-components` | 自动按需导入 Element Plus 组件 |

### 保留不变

- Vue 3、vue-router、vite、@vitejs/plugin-vue
- `src/api/`、`src/stores/`、`src/utils/`、`src/router/` 业务逻辑层保持现有结构

---

## 三、整体布局设计

### AdminLayout 三栏结构

```
┌─────────────────────────────────────────┐
│  TopBar（白色，64px）                   │
│  左：系统名称   右：用户名 + 退出按钮   │
├──────────┬──────────────────────────────┤
│          │                              │
│ Sidebar  │       Main Content           │
│ 220px    │      el-scrollbar            │
│ 深色背景 │      padding: 20px           │
│ #001529  │      background: #f0f2f5     │
│          │                              │
│ 菜单带   │   RouterView 渲染各业务页    │
│ 图标高亮 │                              │
└──────────┴──────────────────────────────┘
```

### 侧边栏配色

- 背景：`#001529`（深海蓝）
- 文字：`#a6adb4`（未选中），`#ffffff`（选中）
- 选中高亮：`#1890ff`（蓝色）
- 菜单项带 Element Plus 图标

### 顶栏

- 背景：`#ffffff`，底部 `1px solid #e8e8e8`
- 左侧系统名称：深色字体，字重 600
- 右侧：用户名（`el-text`）+ 退出按钮（`el-button` text 类型）

---

## 四、登录页设计

- 全屏深色渐变背景（`#1a1f35` → `#2d3561`）
- 居中白色卡片，宽 420px，`border-radius: 12px`，带阴影
- 顶部系统标题 + 副标题
- `el-form` + `el-input` 表单
- 登录按钮：`el-button type="primary"` 全宽
- 错误提示：`el-alert type="error"`

---

## 五、Dashboard 首页设计

> 注：后端实际已实现的接口不含 `/api/reports/*`，Dashboard 统计数据从现有列表接口派生。

### 第一行：统计卡片（4个）

| 卡片 | 数据来源 | 说明 |
|---|---|---|
| 商品总数 | `GET /api/products` | 返回列表长度 |
| 库存预警数 | `GET /api/stocks` | quantity < minStock 的记录数 |
| 入库总笔数 | `GET /api/inbounds` | 返回列表长度 |
| 出库总笔数 | `GET /api/outbounds` | 返回列表长度 |

每个卡片：白色背景、圆角、阴影、顶部彩色图标、数字醒目展示、标题。

### 第二行：ECharts 图表（2个并排）

数据从 `GET /api/inbounds` 和 `GET /api/outbounds` 列表中取 `createTime` 字段，按日期聚合后展示：
- 近7天每日入库数量折线图
- 近7天每日出库数量折线图

---

## 六、业务列表页统一结构

所有业务页（user / product / stock / inbound / outbound / stockcheck）遵循统一结构：

```
页面标题区（标题 + 副标题）
    ↓
操作栏（新增按钮 左 | 刷新按钮 右）
    ↓
el-table（斑马纹 stripe，loading 状态，空数据提示）
    ↓
  操作列：状态用 el-tag，操作用 el-button size="small"
    ↓
el-dialog 弹窗（新增 / 编辑表单）
  内含 el-form + el-form-item + el-input / el-select
  底部：取消 + 提交按钮（提交中 loading 状态）
```

### 各页面说明

**用户管理（UserView）**
- 列：用户名、真实姓名、状态（el-tag）、角色编码、操作（启用/禁用）
- 新增弹窗：用户名、密码、真实姓名、角色ID

**商品管理（ProductView）**
- 列：商品编号、商品名称、分类、进价、售价、状态（el-tag）、操作（上架/下架）
- 新增弹窗：商品编号、名称、分类、进价、售价

**库存管理（StockView）**
- 列：商品编号、商品名称、当前库存、下限、上限、更新时间、操作（设置上下限）
- 编辑弹窗：设置 min_stock / max_stock（不允许直接修改 quantity）

**入库管理（InboundView）**
- 列：商品名称、入库数量、操作人、入库时间
- 新增弹窗：商品ID（选择）、数量、操作人

**出库管理（OutboundView）**
- 列：商品名称、出库数量、操作人、出库时间
- 新增弹窗：商品ID（选择）、数量、操作人

**库存盘点（StockcheckView）**
- 列：商品名称、系统库存、实际库存、差异值、盘点时间
- 新增弹窗：商品ID（选择）、实际库存数量

---

## 七、报表页设计（ReportView）

三个 ECharts 图表布局：

```
┌──────────────────────────────────────────┐
│   库存总览（各商品当前库存 横向条形图）  │
├────────────────┬─────────────────────────┤
│ 入库记录趋势   │   出库记录趋势          │
│ （折线图）     │   （折线图）            │
└────────────────┴─────────────────────────┘
```

数据来源（使用现有列表接口，前端聚合）：
- 库存条形图：`GET /api/stocks`，取 productName + quantity
- 入库趋势：`GET /api/inbounds`，按 createTime 日期聚合
- 出库趋势：`GET /api/outbounds`，按 createTime 日期聚合

---

## 八、系统信息页设计（SystemView）

> 注：后端无 `/api/system/info` 接口，系统信息页全部使用前端静态数据。

- 系统基础信息卡片：系统名称（超市库存管理系统）、版本号（V1.0.0）、技术栈（Vue 3 + Spring Boot + MySQL）等静态展示
- 当前登录账号信息（来自前端 authState：username、roles）
- 系统功能模块说明列表

---

## 九、API 文件补全

需要新增的 api 文件：

| 文件 | 接口 |
|---|---|
| `api/stock.js` | `getStocks()`, `updateStockLimit()` |
| `api/inbound.js` | `getInbounds()`, `createInbound()` |
| `api/outbound.js` | `getOutbounds()`, `createOutbound()` |
| `api/stockcheck.js` | `getStockchecks()`, `createStockcheck()` |

> 报表页和系统页不新增 API 文件，直接复用上述列表接口在组件内聚合数据。

---

## 十、不变的约束

- 前端不计算库存，不修改库存数量
- token 统一通过 Authorization 请求头传递
- 所有错误优先展示后端 message
- 不展示密码字段
- 不实现项目书范围外的功能
