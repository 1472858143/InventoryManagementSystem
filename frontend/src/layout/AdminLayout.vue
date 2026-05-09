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
        <el-menu-item index="/categories"><el-icon><Menu /></el-icon><span>商品分类</span></el-menu-item>
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
