# Frontend Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the entire frontend UI with Element Plus components, a dark-sidebar + white-topbar layout, a Dashboard with ECharts charts, and fully implemented pages for all 9 business modules.

**Architecture:** Preserve all existing logic in `api/`, `stores/`, `utils/`, `router/`, and `App.vue`. Only the UI layer is replaced: `AdminLayout.vue`, all `views/`, `style.css`, and `main.js`. Element Plus is registered globally (full import) in `main.js` with Chinese locale and all icons registered. ECharts is imported directly in components that use charts. No test framework — verification is done by running the Vite dev server and checking pages in the browser.

**Tech Stack:** Vue 3, vue-router 4, Vite 8, Element Plus 2.x, @element-plus/icons-vue, ECharts 5.x

---

## File Map

**Modified:**
- `frontend/package.json`
- `frontend/src/main.js`
- `frontend/src/style.css`
- `frontend/src/layout/AdminLayout.vue`
- `frontend/src/views/login/LoginView.vue`
- `frontend/src/views/home/HomeView.vue`
- `frontend/src/views/user/UserView.vue`
- `frontend/src/views/product/ProductView.vue`
- `frontend/src/views/stock/StockView.vue`
- `frontend/src/views/inbound/InboundView.vue`
- `frontend/src/views/outbound/OutboundView.vue`
- `frontend/src/views/stockcheck/StockcheckView.vue`
- `frontend/src/views/report/ReportView.vue`
- `frontend/src/views/system/SystemView.vue`

**Created:**
- `frontend/src/api/stock.js`
- `frontend/src/api/inbound.js`
- `frontend/src/api/outbound.js`
- `frontend/src/api/stockcheck.js`

**Not changed:**
- `frontend/src/api/auth.js`, `api/user.js`, `api/product.js`
- `frontend/src/stores/auth.js`, `utils/request.js`, `utils/token.js`
- `frontend/src/router/index.js`, `src/App.vue`, `vite.config.js`

---

### Task 1: Install dependencies and configure Element Plus

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/src/main.js`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Install packages**

Run inside the `frontend/` directory:

```bash
npm install element-plus @element-plus/icons-vue echarts
```

Expected: packages added to `node_modules/`, `package.json` updated.

- [ ] **Step 2: Replace `frontend/src/main.js`**

```js
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.mount('#app')
```

- [ ] **Step 3: Replace `frontend/src/style.css`**

```css
*,
*::before,
*::after {
  box-sizing: border-box;
}

html,
body,
#app {
  margin: 0;
  padding: 0;
  height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", sans-serif;
  color: #1f2937;
  background: #f0f2f5;
}

h1, h2, h3, h4, h5, h6, p {
  margin: 0;
}
```

- [ ] **Step 4: Verify dev server starts with no errors**

```bash
cd frontend && npm run dev
```

Expected: server starts, browser shows existing pages without console errors. Check DevTools Network tab for Element Plus CSS file loaded.

- [ ] **Step 5: Commit**

```bash
git add frontend/package.json frontend/package-lock.json frontend/src/main.js frontend/src/style.css
git commit -m "feat: install element-plus, echarts and configure global setup"
```

---

### Task 2: Rebuild AdminLayout

**Files:**
- Modify: `frontend/src/layout/AdminLayout.vue`

- [ ] **Step 1: Replace AdminLayout.vue entirely**

```vue
<template>
  <div class="layout-root">
    <aside class="layout-sidebar">
      <div class="sidebar-brand">
        <el-icon size="18" color="#1890ff"><Box /></el-icon>
        <span>库存管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#ffffff"
        class="sidebar-menu"
      >
        <el-menu-item index="/"><el-icon><House /></el-icon><span>首页</span></el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/stocks"><el-icon><Box /></el-icon><span>库存管理</span></el-menu-item>
        <el-menu-item index="/inbounds"><el-icon><Download /></el-icon><span>入库管理</span></el-menu-item>
        <el-menu-item index="/outbounds"><el-icon><Upload /></el-icon><span>出库管理</span></el-menu-item>
        <el-menu-item index="/stockchecks"><el-icon><DocumentChecked /></el-icon><span>库存盘点</span></el-menu-item>
        <el-menu-item index="/reports"><el-icon><DataAnalysis /></el-icon><span>报表统计</span></el-menu-item>
        <el-menu-item index="/system"><el-icon><Setting /></el-icon><span>系统信息</span></el-menu-item>
      </el-menu>
    </aside>

    <div class="layout-body">
      <header class="layout-header">
        <span class="header-title">超市库存管理系统</span>
        <div class="header-right">
          <el-icon color="#595959"><User /></el-icon>
          <span class="header-username">{{ currentUsername }}</span>
          <el-divider direction="vertical" />
          <el-button text type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>
      <main class="layout-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '../api/auth'
import { authState, clearAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()

const currentUsername = computed(() => authState.currentUser?.username || '用户')

async function handleLogout() {
  try {
    await logout()
  } catch {
    // ignore — clear local state regardless
  } finally {
    clearAuth()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout-root {
  display: flex;
  min-height: 100vh;
}

.layout-sidebar {
  width: 220px;
  min-height: 100vh;
  background: #001529;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  overflow-x: hidden;
  overflow-y: auto;
}

.sidebar-brand {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #002040;
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-menu {
  border-right: none !important;
}

:deep(.sidebar-menu .el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 2px 8px;
  border-radius: 6px;
}

:deep(.sidebar-menu .el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #ffffff !important;
}

:deep(.sidebar-menu .el-menu-item:hover:not(.is-active)) {
  background-color: rgba(255, 255, 255, 0.08) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

.layout-body {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout-header {
  height: 64px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  flex-shrink: 0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-username {
  font-size: 14px;
  color: #595959;
}

.layout-main {
  flex: 1;
  padding: 20px;
  background: #f0f2f5;
}
</style>
```

- [ ] **Step 2: Verify AdminLayout**

Log in and verify: dark sidebar (#001529) on left with white-text menu items; white topbar with title and logout button; active menu item highlighted with blue (#1890ff); content area is light gray.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/layout/AdminLayout.vue
git commit -m "feat: rebuild AdminLayout with dark sidebar and white topbar"
```

---

### Task 3: Rebuild LoginView

**Files:**
- Modify: `frontend/src/views/login/LoginView.vue`

- [ ] **Step 1: Replace LoginView.vue entirely**

```vue
<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon size="40" color="#1890ff"><Box /></el-icon>
        <h1>超市库存管理系统</h1>
        <p>Supermarket Inventory Management System</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            :prefix-icon="UserIcon"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            :prefix-icon="LockIcon"
            show-password
            @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />

        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { Lock as LockIcon, User as UserIcon } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../../api/auth'
import { clearAuth, setLoginSession } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleSubmit() {
  errorMessage.value = ''
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const loginResult = await login({ username: form.username, password: form.password })
    setLoginSession(loginResult)
    form.password = ''
    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirectPath)
  } catch (error) {
    clearAuth()
    errorMessage.value = error.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #0d1117 0%, #1a2744 50%, #2d3561 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 32px;
  text-align: center;
}

.login-header h1 {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.login-header p {
  font-size: 12px;
  color: #9ca3af;
  letter-spacing: 0.5px;
}
</style>
```

- [ ] **Step 2: Verify LoginView**

Open `http://localhost:5173/login` and verify: dark navy-to-blue gradient background; white rounded card with shadow; icon + title + subtitle; input fields with prefix icons; error alert on wrong credentials (if backend running).

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/login/LoginView.vue
git commit -m "feat: rebuild LoginView with dark gradient and card style"
```

---

### Task 4: Create missing API files

**Files:**
- Create: `frontend/src/api/stock.js`
- Create: `frontend/src/api/inbound.js`
- Create: `frontend/src/api/outbound.js`
- Create: `frontend/src/api/stockcheck.js`

- [ ] **Step 1: Create `frontend/src/api/stock.js`**

```js
import { request } from '../utils/request'

export function getStocks() {
  return request('/stocks')
}

export function getStock(productId) {
  return request(`/stocks/${productId}`)
}

export function updateStockLimit(productId, payload) {
  return request(`/stocks/${productId}/limit`, {
    method: 'PUT',
    body: payload,
  })
}
```

- [ ] **Step 2: Create `frontend/src/api/inbound.js`**

```js
import { request } from '../utils/request'

export function getInbounds() {
  return request('/inbounds')
}

export function createInbound(payload) {
  return request('/inbounds', {
    method: 'POST',
    body: payload,
  })
}
```

- [ ] **Step 3: Create `frontend/src/api/outbound.js`**

```js
import { request } from '../utils/request'

export function getOutbounds() {
  return request('/outbounds')
}

export function createOutbound(payload) {
  return request('/outbounds', {
    method: 'POST',
    body: payload,
  })
}
```

- [ ] **Step 4: Create `frontend/src/api/stockcheck.js`**

```js
import { request } from '../utils/request'

export function getStockchecks() {
  return request('/stockchecks')
}

export function createStockcheck(payload) {
  return request('/stockchecks', {
    method: 'POST',
    body: payload,
  })
}
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/api/stock.js frontend/src/api/inbound.js frontend/src/api/outbound.js frontend/src/api/stockcheck.js
git commit -m "feat: add API files for stock, inbound, outbound, stockcheck modules"
```

---

### Task 5: Build HomeView Dashboard

**Files:**
- Modify: `frontend/src/views/home/HomeView.vue`

- [ ] **Step 1: Replace HomeView.vue entirely**

```vue
<template>
  <div class="dashboard">
    <div class="page-title">
      <h2>首页概览</h2>
      <p>欢迎使用超市库存管理系统</p>
    </div>

    <el-row :gutter="16">
      <el-col v-for="card in statCards" :key="card.key" :xs="24" :sm="12" :xl="6">
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon-wrap" :style="{ background: card.color }">
            <el-icon size="26" color="#ffffff"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ loading ? '—' : card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>近 7 天入库趋势</template>
          <div ref="inboundChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>近 7 天出库趋势</template>
          <div ref="outboundChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { Download, Goods, Upload, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { getProducts } from '../../api/product'
import { getInbounds } from '../../api/inbound'
import { getOutbounds } from '../../api/outbound'
import { getStocks } from '../../api/stock'

const loading = ref(true)
const stats = reactive({ productCount: 0, warningCount: 0, inboundCount: 0, outboundCount: 0 })

const statCards = computed(() => [
  { key: 'product', label: '商品总数', value: stats.productCount, icon: Goods, color: '#1890ff' },
  { key: 'warning', label: '库存预警', value: stats.warningCount, icon: Warning, color: '#ff4d4f' },
  { key: 'inbound', label: '入库总笔数', value: stats.inboundCount, icon: Download, color: '#52c41a' },
  { key: 'outbound', label: '出库总笔数', value: stats.outboundCount, icon: Upload, color: '#722ed1' },
])

const inboundChartRef = ref(null)
const outboundChartRef = ref(null)
let inboundChart = null
let outboundChart = null

function getLast7Days() {
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (6 - i))
    return d.toISOString().slice(0, 10)
  })
}

function aggregateByDay(records, dateField) {
  const days = getLast7Days()
  const counts = Object.fromEntries(days.map(d => [d, 0]))
  for (const r of records) {
    const day = String(r[dateField] || '').slice(0, 10)
    if (day in counts) counts[day]++
  }
  return { days, values: days.map(d => counts[d]) }
}

function makeLineOption(data, color) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 32 },
    xAxis: {
      type: 'category',
      data: data.days.map(d => d.slice(5)),
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '笔数',
      type: 'line',
      data: data.values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color },
      areaStyle: { color, opacity: 0.08 },
    }],
  }
}

function handleResize() {
  inboundChart?.resize()
  outboundChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  try {
    const [products, stocks, inbounds, outbounds] = await Promise.all([
      getProducts(),
      getStocks(),
      getInbounds(),
      getOutbounds(),
    ])
    stats.productCount = Array.isArray(products) ? products.length : 0
    stats.warningCount = (Array.isArray(stocks) ? stocks : []).filter(s => s.quantity < s.minStock).length
    stats.inboundCount = Array.isArray(inbounds) ? inbounds.length : 0
    stats.outboundCount = Array.isArray(outbounds) ? outbounds.length : 0

    inboundChart = echarts.init(inboundChartRef.value)
    inboundChart.setOption(makeLineOption(aggregateByDay(inbounds || [], 'createTime'), '#1890ff'))

    outboundChart = echarts.init(outboundChartRef.value)
    outboundChart.setOption(makeLineOption(aggregateByDay(outbounds || [], 'createTime'), '#52c41a'))
  } catch (e) {
    ElMessage.error(e.message || '加载数据失败')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  inboundChart?.dispose()
  outboundChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.page-title p {
  font-size: 14px;
  color: #8c8c8c;
}

.stat-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
  min-height: 90px;
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body { flex: 1; }

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 4px;
}

.chart-box { height: 260px; }
</style>
```

- [ ] **Step 2: Verify Dashboard**

Navigate to `/` after login: 4 stat cards in a row with colored icons; 2 ECharts line charts side by side; numbers populated from backend data.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/home/HomeView.vue
git commit -m "feat: build Dashboard with stat cards and ECharts line charts"
```

---

### Task 6: Rebuild UserView

**Files:**
- Modify: `frontend/src/views/user/UserView.vue`

- [ ] **Step 1: Replace UserView.vue entirely**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>维护系统用户账号与角色分配</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadUsers">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增用户</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="users" v-loading="loading" stripe row-key="id" empty-text="暂无用户记录">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="真实姓名" min-width="120">
          <template #default="{ row }">{{ row.realName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="140">
          <template #default="{ row }">
            <el-tag v-for="code in (row.roleCodes || [])" :key="code" type="info" size="small" style="margin-right:4px">{{ code }}</el-tag>
            <span v-if="!row.roleCodes?.length">—</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'danger' : 'primary'"
              size="small" link
              :loading="statusUpdatingId === row.id"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增用户" width="480px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" autocomplete="off" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="new-password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model.trim="form.realName" autocomplete="off" placeholder="选填" />
        </el-form-item>
        <el-form-item label="角色ID" prop="roleIdsInput">
          <el-input v-model.trim="form.roleIdsInput" placeholder="输入角色ID，多个用英文逗号分隔，如: 1 或 1,2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { createUser, getUsers, updateUserStatus } from '../../api/user'

const users = ref([])
const loading = ref(false)
const statusUpdatingId = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '', realName: '', roleIdsInput: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIdsInput: [{ required: true, message: '请输入角色ID', trigger: 'blur' }],
}

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const result = await getUsers()
    users.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询用户列表失败')
    users.value = []
  } finally {
    loading.value = false
  }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { username: '', password: '', realName: '', roleIdsInput: '' })
  nextTick(() => formRef.value?.clearValidate())
}

function parseRoleIds(input) {
  return input.split(',').map(s => Number(s.trim())).filter(n => Number.isInteger(n) && n > 0)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const roleIds = parseRoleIds(form.roleIdsInput)
  if (roleIds.length === 0) {
    ElMessage.warning('角色ID格式不正确，请填写正整数，多个用英文逗号分隔')
    return
  }
  submitting.value = true
  try {
    await createUser({ username: form.username, password: form.password, realName: form.realName || undefined, roleIds })
    ElMessage.success('新增用户成功')
    dialogVisible.value = false
    await loadUsers()
  } catch (e) {
    ElMessage.error(e.message || '新增用户失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  statusUpdatingId.value = row.id
  try {
    await updateUserStatus({ userId: row.id, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('状态更新成功')
    await loadUsers()
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    statusUpdatingId.value = null
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; flex-shrink: 0; }
</style>
```

- [ ] **Step 2: Verify UserView**

Navigate to `/users`: table shows users with status tags (green=启用, red=禁用) and role code tags; "新增用户" dialog validates fields; 禁用/启用 toggle updates status.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/user/UserView.vue
git commit -m "feat: rebuild UserView with Element Plus table and dialog"
```

---

### Task 7: Rebuild ProductView

**Files:**
- Modify: `frontend/src/views/product/ProductView.vue`

- [ ] **Step 1: Replace ProductView.vue entirely**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p>维护商品基础信息和上下架状态</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增商品</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="products" v-loading="loading" stripe row-key="id" empty-text="暂无商品记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="进价" width="90">
          <template #default="{ row }">¥ {{ Number(row.purchasePrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="售价" width="90">
          <template #default="{ row }">¥ {{ Number(row.salePrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'warning' : 'primary'"
              size="small" link
              :loading="statusUpdatingId === row.id"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增商品" width="520px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品编号" prop="productCode">
          <el-input v-model.trim="form.productCode" autocomplete="off" placeholder="如：P001" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model.trim="form.productName" autocomplete="off" placeholder="如：矿泉水" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model.trim="form.category" autocomplete="off" placeholder="如：饮料" />
        </el-form-item>
        <el-form-item label="进价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
        <el-form-item label="售价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { createProduct, getProducts, updateProductStatus } from '../../api/product'

const products = ref([])
const loading = ref(false)
const statusUpdatingId = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ productCode: '', productName: '', category: '', purchasePrice: null, salePrice: null })

const rules = {
  productCode: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  purchasePrice: [{ required: true, message: '请输入进价', trigger: 'change', type: 'number' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'change', type: 'number' }],
}

onMounted(loadProducts)

async function loadProducts() {
  loading.value = true
  try {
    const result = await getProducts()
    products.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询商品列表失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { productCode: '', productName: '', category: '', purchasePrice: null, salePrice: null })
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createProduct({ productCode: form.productCode, productName: form.productName, category: form.category, purchasePrice: form.purchasePrice, salePrice: form.salePrice })
    ElMessage.success('新增商品成功')
    dialogVisible.value = false
    await loadProducts()
  } catch (e) {
    ElMessage.error(e.message || '新增商品失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  statusUpdatingId.value = row.id
  try {
    await updateProductStatus({ productId: row.id, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('状态更新成功')
    await loadProducts()
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    statusUpdatingId.value = null
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; flex-shrink: 0; }
</style>
```

- [ ] **Step 2: Verify ProductView**

Navigate to `/products`: table with status tags (上架=green, 下架=gray); dialog with number inputs for price; backend error (sale < purchase price) shown via ElMessage.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/product/ProductView.vue
git commit -m "feat: rebuild ProductView with Element Plus table and dialog"
```

---

### Task 8: Build StockView

**Files:**
- Modify: `frontend/src/views/stock/StockView.vue`

- [ ] **Step 1: Replace StockView.vue**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>库存管理</h2>
        <p>查看当前库存，设置上下限预警阈值（不可直接修改库存数量）</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadStocks">刷新</el-button>
    </div>

    <el-card>
      <el-table :data="stocks" v-loading="loading" stripe row-key="productId" empty-text="暂无库存记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column label="当前库存" width="100">
          <template #default="{ row }">
            <el-text :type="row.quantity < row.minStock ? 'danger' : 'success'" tag="b">{{ row.quantity }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="下限" width="80" />
        <el-table-column prop="maxStock" label="上限" width="80" />
        <el-table-column label="预警状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.quantity < row.minStock" type="danger" size="small">库存不足</el-tag>
            <el-tag v-else-if="row.quantity > row.maxStock" type="warning" size="small">库存过多</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="160">
          <template #default="{ row }">{{ row.updateTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">设置上下限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="设置库存上下限" width="400px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品">
          <el-text>{{ currentProduct?.productName }}</el-text>
        </el-form-item>
        <el-form-item label="库存下限" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="库存上限" prop="maxStock">
          <el-input-number v-model="form.maxStock" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { getStocks, updateStockLimit } from '../../api/stock'

const stocks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const currentProduct = ref(null)
const form = reactive({ minStock: 0, maxStock: 0 })

const rules = {
  minStock: [{ required: true, message: '请输入库存下限', trigger: 'change', type: 'number' }],
  maxStock: [{ required: true, message: '请输入库存上限', trigger: 'change', type: 'number' }],
}

onMounted(loadStocks)

async function loadStocks() {
  loading.value = true
  try {
    const result = await getStocks()
    stocks.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询库存列表失败')
    stocks.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  currentProduct.value = row
  form.minStock = row.minStock
  form.maxStock = row.maxStock
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateStockLimit(currentProduct.value.productId, { minStock: form.minStock, maxStock: form.maxStock })
    ElMessage.success('上下限更新成功')
    dialogVisible.value = false
    await loadStocks()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
</style>
```

- [ ] **Step 2: Verify StockView**

Navigate to `/stocks`: quantity shown red if below minStock; 预警状态 tag shows 库存不足/库存过多/正常; "设置上下限" pre-fills current values; backend validation error shown.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/stock/StockView.vue
git commit -m "feat: build StockView with inventory list and limit editor"
```

---

### Task 9: Build InboundView

**Files:**
- Modify: `frontend/src/views/inbound/InboundView.vue`

- [ ] **Step 1: Replace InboundView.vue**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>入库管理</h2>
        <p>记录商品入库操作，自动增加对应商品库存</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadInbounds">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增入库</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="inbounds" v-loading="loading" stripe row-key="id" empty-text="暂无入库记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column label="入库数量" width="100">
          <template #default="{ row }"><el-text type="success">+{{ row.quantity }}</el-text></template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="入库时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增入库记录" width="480px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="`${p.productCode} - ${p.productName}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model.trim="form.operator" autocomplete="off" placeholder="请输入操作人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { getProducts } from '../../api/product'
import { createInbound, getInbounds } from '../../api/inbound'
import { authState } from '../../stores/auth'

const inbounds = ref([])
const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ productId: null, quantity: 1, operator: '' })

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'change', type: 'number' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
}

onMounted(() => Promise.all([loadInbounds(), loadProducts()]))

async function loadInbounds() {
  loading.value = true
  try {
    const result = await getInbounds()
    inbounds.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询入库记录失败')
    inbounds.value = []
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const result = await getProducts()
    products.value = Array.isArray(result) ? result : []
  } catch { products.value = [] }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { productId: null, quantity: 1, operator: authState.currentUser?.username || '' })
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createInbound({ productId: form.productId, quantity: form.quantity, operator: form.operator })
    ElMessage.success('入库成功')
    dialogVisible.value = false
    await loadInbounds()
  } catch (e) {
    ElMessage.error(e.message || '入库失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; flex-shrink: 0; }
</style>
```

- [ ] **Step 2: Verify InboundView**

Navigate to `/inbounds`: table shows records with green +N quantity; dialog has filterable product dropdown (auto-filled operator from current user); after submit, table refreshes.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/inbound/InboundView.vue
git commit -m "feat: build InboundView with records table and inbound dialog"
```

---

### Task 10: Build OutboundView

**Files:**
- Modify: `frontend/src/views/outbound/OutboundView.vue`

- [ ] **Step 1: Replace OutboundView.vue**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>出库管理</h2>
        <p>记录商品出库操作，库存不足时系统拒绝出库</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadOutbounds">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增出库</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="outbounds" v-loading="loading" stripe row-key="id" empty-text="暂无出库记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column label="出库数量" width="100">
          <template #default="{ row }"><el-text type="danger">-{{ row.quantity }}</el-text></template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="出库时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增出库记录" width="480px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="`${p.productCode} - ${p.productName}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model.trim="form.operator" autocomplete="off" placeholder="请输入操作人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { getProducts } from '../../api/product'
import { createOutbound, getOutbounds } from '../../api/outbound'
import { authState } from '../../stores/auth'

const outbounds = ref([])
const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ productId: null, quantity: 1, operator: '' })

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入出库数量', trigger: 'change', type: 'number' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
}

onMounted(() => Promise.all([loadOutbounds(), loadProducts()]))

async function loadOutbounds() {
  loading.value = true
  try {
    const result = await getOutbounds()
    outbounds.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询出库记录失败')
    outbounds.value = []
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const result = await getProducts()
    products.value = Array.isArray(result) ? result : []
  } catch { products.value = [] }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { productId: null, quantity: 1, operator: authState.currentUser?.username || '' })
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createOutbound({ productId: form.productId, quantity: form.quantity, operator: form.operator })
    ElMessage.success('出库成功')
    dialogVisible.value = false
    await loadOutbounds()
  } catch (e) {
    ElMessage.error(e.message || '出库失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; flex-shrink: 0; }
</style>
```

- [ ] **Step 2: Verify OutboundView**

Navigate to `/outbounds`: table shows red -N quantity; backend rejects out-of-stock with ElMessage.error.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/outbound/OutboundView.vue
git commit -m "feat: build OutboundView with records table and outbound dialog"
```

---

### Task 11: Build StockcheckView

**Files:**
- Modify: `frontend/src/views/stockcheck/StockcheckView.vue`

- [ ] **Step 1: Replace StockcheckView.vue**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>库存盘点</h2>
        <p>录入实盘数量，系统自动计算差异并调整库存</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadStockchecks">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增盘点</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="stockchecks" v-loading="loading" stripe row-key="id" empty-text="暂无盘点记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="systemQuantity" label="系统库存" width="100" />
        <el-table-column prop="actualQuantity" label="实际库存" width="100" />
        <el-table-column label="差异" width="90">
          <template #default="{ row }">
            <el-text :type="row.difference > 0 ? 'success' : row.difference < 0 ? 'danger' : 'info'">
              {{ row.difference > 0 ? '+' : '' }}{{ row.difference }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="盘点时间" min-width="160">
          <template #default="{ row }">{{ row.checkTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增盘点记录" width="480px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="`${p.productCode} - ${p.productName}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际库存" prop="actualQuantity">
          <el-input-number v-model="form.actualQuantity" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" title="提交后系统将自动计算与当前系统库存的差异，并将库存调整为实际盘点值" style="margin-top:8px" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交盘点</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { getProducts } from '../../api/product'
import { createStockcheck, getStockchecks } from '../../api/stockcheck'

const stockchecks = ref([])
const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ productId: null, actualQuantity: 0 })

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  actualQuantity: [{ required: true, message: '请输入实际库存', trigger: 'change', type: 'number' }],
}

onMounted(() => Promise.all([loadStockchecks(), loadProducts()]))

async function loadStockchecks() {
  loading.value = true
  try {
    const result = await getStockchecks()
    stockchecks.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询盘点记录失败')
    stockchecks.value = []
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const result = await getProducts()
    products.value = Array.isArray(result) ? result : []
  } catch { products.value = [] }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { productId: null, actualQuantity: 0 })
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createStockcheck({ productId: form.productId, actualQuantity: form.actualQuantity })
    ElMessage.success('盘点记录已提交，库存已调整')
    dialogVisible.value = false
    await loadStockchecks()
  } catch (e) {
    ElMessage.error(e.message || '盘点提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.page-actions { display: flex; gap: 8px; flex-shrink: 0; }
</style>
```

- [ ] **Step 2: Verify StockcheckView**

Navigate to `/stockchecks`: difference column shows green +N or red -N; dialog shows info alert; after submit, list refreshes.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/stockcheck/StockcheckView.vue
git commit -m "feat: build StockcheckView with records table and stockcheck dialog"
```

---

### Task 12: Build ReportView

**Files:**
- Modify: `frontend/src/views/report/ReportView.vue`

- [ ] **Step 1: Replace ReportView.vue**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>报表统计</h2>
        <p>库存分布与入出库趋势图表（仅供查看）</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </div>

    <div v-loading="loading" class="charts-wrap">
      <el-card>
        <template #header>库存总览（当前库存量，红色表示低于下限）</template>
        <div ref="stockChartRef" class="chart-tall"></div>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>近 30 天入库趋势</template>
            <div ref="inboundChartRef" class="chart-normal"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>近 30 天出库趋势</template>
            <div ref="outboundChartRef" class="chart-normal"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { onMounted, onUnmounted, ref } from 'vue'
import { getInbounds } from '../../api/inbound'
import { getOutbounds } from '../../api/outbound'
import { getStocks } from '../../api/stock'

const loading = ref(false)
const stockChartRef = ref(null)
const inboundChartRef = ref(null)
const outboundChartRef = ref(null)
let stockChart = null
let inboundChart = null
let outboundChart = null

function getLast30Days() {
  return Array.from({ length: 30 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (29 - i))
    return d.toISOString().slice(0, 10)
  })
}

function aggregateByDay(records, dateField) {
  const days = getLast30Days()
  const counts = Object.fromEntries(days.map(d => [d, 0]))
  for (const r of records) {
    const day = String(r[dateField] || '').slice(0, 10)
    if (day in counts) counts[day]++
  }
  return { days, values: days.map(d => counts[d]) }
}

function renderStockChart(stocks) {
  const sorted = [...stocks].sort((a, b) => b.quantity - a.quantity).slice(0, 20)
  stockChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '22%', right: '5%', top: 16, bottom: 16 },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: sorted.map(s => s.productName),
      axisLabel: { fontSize: 11, width: 120, overflow: 'truncate' },
    },
    series: [{
      name: '库存量',
      type: 'bar',
      data: sorted.map(s => ({
        value: s.quantity,
        itemStyle: { color: s.quantity < s.minStock ? '#ff4d4f' : '#1890ff' },
      })),
    }],
  }, true)
}

function makeTrendOption(data, color) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.days.map(d => d.slice(5)),
      axisLabel: { rotate: 45, fontSize: 10 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '笔数',
      type: 'line',
      data: data.values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      itemStyle: { color },
      areaStyle: { color, opacity: 0.08 },
    }],
  }
}

async function loadData() {
  loading.value = true
  try {
    const [stocks, inbounds, outbounds] = await Promise.all([getStocks(), getInbounds(), getOutbounds()])

    if (!stockChart) stockChart = echarts.init(stockChartRef.value)
    if (!inboundChart) inboundChart = echarts.init(inboundChartRef.value)
    if (!outboundChart) outboundChart = echarts.init(outboundChartRef.value)

    renderStockChart(Array.isArray(stocks) ? stocks : [])
    inboundChart.setOption(makeTrendOption(aggregateByDay(inbounds || [], 'createTime'), '#52c41a'), true)
    outboundChart.setOption(makeTrendOption(aggregateByDay(outbounds || [], 'createTime'), '#fa8c16'), true)
  } catch (e) {
    ElMessage.error(e.message || '加载报表数据失败')
  } finally {
    loading.value = false
  }
}

function handleResize() {
  stockChart?.resize()
  inboundChart?.resize()
  outboundChart?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  stockChart?.dispose()
  inboundChart?.dispose()
  outboundChart?.dispose()
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
.charts-wrap { display: flex; flex-direction: column; gap: 16px; }
.chart-tall { height: 360px; }
.chart-normal { height: 260px; }
</style>
```

- [ ] **Step 2: Verify ReportView**

Navigate to `/reports`: horizontal bar chart for stock (red bars = below minStock); two 30-day line charts for inbound (green) and outbound (orange); refresh button reloads all.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/report/ReportView.vue
git commit -m "feat: build ReportView with stock bar chart and 30-day trend charts"
```

---

### Task 13: Build SystemView

**Files:**
- Modify: `frontend/src/views/system/SystemView.vue`

- [ ] **Step 1: Replace SystemView.vue**

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统信息</h2>
      <p>系统基础信息与当前账号</p>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header><el-icon style="vertical-align:middle;margin-right:6px"><Monitor /></el-icon>系统信息</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">超市库存管理系统</el-descriptions-item>
            <el-descriptions-item label="当前版本">V1.0.0</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3 + Element Plus</el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0.44</el-descriptions-item>
            <el-descriptions-item label="部署环境">华为云 ECS</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header><el-icon style="vertical-align:middle;margin-right:6px"><User /></el-icon>当前账号</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ currentUser?.username || '—' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="role in roles" :key="role" size="small" style="margin-right:4px">{{ role }}</el-tag>
              <span v-if="!roles.length">—</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><el-icon style="vertical-align:middle;margin-right:6px"><Grid /></el-icon>功能模块说明</template>
      <el-table :data="modules" stripe>
        <el-table-column prop="name" label="模块名称" width="120" />
        <el-table-column prop="route" label="访问路径" width="130" />
        <el-table-column prop="description" label="功能说明" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { authState } from '../../stores/auth'

const currentUser = computed(() => authState.currentUser)
const roles = computed(() => authState.roles)

const modules = [
  { name: '用户管理', route: '/users', description: '维护系统用户账号与角色分配，支持启用/禁用' },
  { name: '商品管理', route: '/products', description: '维护商品基础信息（编号、名称、分类、价格），支持上架/下架' },
  { name: '库存管理', route: '/stocks', description: '查看当前库存数量，设置上下限预警阈值，不可直接修改库存' },
  { name: '入库管理', route: '/inbounds', description: '记录商品入库操作，提交后自动增加对应商品库存' },
  { name: '出库管理', route: '/outbounds', description: '记录商品出库操作，库存不足时系统拒绝出库' },
  { name: '库存盘点', route: '/stockchecks', description: '录入实盘数量，系统计算差异并将库存调整为实际值' },
  { name: '报表统计', route: '/reports', description: '展示库存分布条形图和入出库趋势折线图，仅供查看' },
  { name: '系统信息', route: '/system', description: '展示系统基础配置信息和当前登录账号' },
]
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
</style>
```

- [ ] **Step 2: Verify SystemView**

Navigate to `/system`: two info cards side by side (system info + current account); module table lists all 8 modules with descriptions.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/system/SystemView.vue
git commit -m "feat: build SystemView with system info, account info, and module table"
```

---

## Final Verification

- [ ] Run `cd frontend && npm run build` — expected: build succeeds, `dist/` folder created with no errors

- [ ] Browse through all pages and confirm each loads without console errors

- [ ] Confirm logout redirects to login; unauthenticated access to protected routes redirects to login
