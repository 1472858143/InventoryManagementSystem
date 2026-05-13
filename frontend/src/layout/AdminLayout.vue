<template>
  <div class="layout-root">
    <aside class="layout-sidebar">
      <div class="sidebar-brand">
        <Warehouse :size="20" color="var(--color-primary)" />
        <span>库存管理系统</span>
      </div>

      <el-menu
        :default-active="route.path"
        router
        :default-openeds="['master-group', 'stocks-group', 'system-group']"
        class="sidebar-menu"
      >
        <el-menu-item index="/">
          <Home :size="15" class="menu-icon" />
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="master-group">
          <template #title>
            <BookOpen :size="15" class="menu-icon" />
            <span>基础资料</span>
          </template>
          <el-menu-item index="/products">
            <Package :size="14" class="menu-icon" />
            <template #title>商品管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="stocks-group">
          <template #title>
            <Archive :size="15" class="menu-icon" />
            <span>库存管理</span>
          </template>
          <el-menu-item index="/stock-workspace">
            <LayoutDashboard :size="14" class="menu-icon" />
            <template #title>工作台</template>
          </el-menu-item>
          <el-menu-item index="/stocks">
            <List :size="14" class="menu-icon" />
            <template #title>库存总览</template>
          </el-menu-item>
          <el-menu-item index="/inbounds">
            <PackagePlus :size="14" class="menu-icon" />
            <template #title>入库单据</template>
          </el-menu-item>
          <el-menu-item index="/shelf-restock">
            <ArrowUpFromLine :size="14" class="menu-icon" />
            <template #title>上架补货</template>
          </el-menu-item>
          <el-menu-item index="/outbounds">
            <PackageMinus :size="14" class="menu-icon" />
            <template #title>出库单据</template>
          </el-menu-item>
          <el-menu-item index="/stockchecks">
            <ClipboardList :size="14" class="menu-icon" />
            <template #title>库存盘点</template>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/reports">
          <BarChart3 :size="15" class="menu-icon" />
          <template #title>报表分析</template>
        </el-menu-item>

        <el-sub-menu index="system-group">
          <template #title>
            <Settings :size="15" class="menu-icon" />
            <span>系统</span>
          </template>
          <el-menu-item index="/users">
            <Users :size="14" class="menu-icon" />
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/system">
            <Info :size="14" class="menu-icon" />
            <template #title>系统信息</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <div class="layout-body">
      <header class="layout-header">
        <span class="header-title">超市库存管理系统</span>
        <div class="header-right">
          <User :size="16" color="var(--color-text-secondary)" />
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
import {
  Archive, ArrowUpFromLine, BarChart3, BookOpen, ClipboardList, Home, Info,
  LayoutDashboard, List, Package, PackageMinus, PackagePlus,
  Settings, User, Users, Warehouse,
} from 'lucide-vue-next'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '../api/auth'
import { authState, clearAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const currentUsername = computed(() => authState.currentUser?.username || '用户')

async function handleLogout() {
  try { await logout() } catch { /* ignore */ } finally {
    clearAuth()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout-root {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

.layout-sidebar {
  width: 220px;
  min-height: 100vh;
  background: var(--color-sidebar);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  z-index: 100;
  overflow-x: hidden;
  overflow-y: auto;
}

.sidebar-brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text);
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-menu {
  border-right: none !important;
  background-color: transparent !important;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--color-text-secondary);
  --el-menu-active-color: var(--color-primary);
  --el-menu-hover-bg-color: var(--color-primary-bg);
  --el-menu-item-height: 42px;
  --el-menu-sub-item-height: 38px;
}

.menu-icon {
  margin-right: 8px;
  flex-shrink: 0;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--color-primary-bg) !important;
  color: var(--color-primary) !important;
  font-weight: 600;
  border-right: 3px solid var(--color-primary);
}

:deep(.el-sub-menu__title) {
  color: var(--color-text-secondary);
  font-size: 13px;
}

:deep(.el-sub-menu__title:hover) {
  background-color: var(--color-primary-bg) !important;
}

:deep(.el-menu-item) {
  font-size: 13px;
  border-radius: 0;
}

:deep(.el-menu--inline) {
  background-color: var(--color-submenu-bg) !important;
}

.layout-body {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout-header {
  height: 56px;
  background: var(--color-white);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: var(--shadow-card);
  flex-shrink: 0;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-username {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.layout-main {
  flex: 1;
  padding: 20px;
  background: var(--color-bg);
}
</style>
