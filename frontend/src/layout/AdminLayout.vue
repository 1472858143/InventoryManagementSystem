<template>
  <div class="admin-layout">
    <header class="admin-layout__header">
      <div class="admin-layout__brand">超市库存管理系统</div>
      <div class="admin-layout__user-area">
        <span class="admin-layout__user">{{ currentUsername }}</span>
        <button class="admin-layout__logout" type="button" @click="handleLogout">
          退出登录
        </button>
      </div>
    </header>

    <aside class="admin-layout__sidebar">
      <div class="admin-layout__menu-title">后台菜单</div>
      <nav class="admin-layout__menu" aria-label="后台菜单">
        <RouterLink
          v-for="menuItem in menuItems"
          :key="menuItem.name"
          class="admin-layout__menu-item"
          :class="{ 'admin-layout__menu-item--active': route.path === menuItem.path }"
          :to="menuItem.path"
        >
          {{ menuItem.name }}
        </RouterLink>
      </nav>
    </aside>

    <main class="admin-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '../api/auth'
import { authState, clearAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()

const currentUsername = computed(() => authState.currentUser?.username || '当前用户')

const menuItems = [
  { name: '首页', path: '/' },
  { name: '用户管理', path: '/users' },
  { name: '商品管理', path: '/products' },
  { name: '库存管理', path: '/stocks' },
  { name: '入库管理', path: '/inbounds' },
  { name: '出库管理', path: '/outbounds' },
  { name: '库存盘点', path: '/stockchecks' },
  { name: '报表统计', path: '/reports' },
  { name: '系统信息', path: '/system' },
]

async function handleLogout() {
  try {
    await logout()
  } catch {
    // 退出时即使服务端会话已失效，也必须清理前端登录状态。
  } finally {
    clearAuth()
    router.replace('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  display: grid;
  grid-template-areas:
    "header header"
    "sidebar main";
  grid-template-columns: 220px 1fr;
  grid-template-rows: 64px 1fr;
  min-height: 100vh;
  color: #1f2937;
  background: #f3f4f6;
}

.admin-layout__header {
  grid-area: header;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #1f2937;
  color: #ffffff;
  border-bottom: 1px solid #111827;
}

.admin-layout__brand {
  font-size: 20px;
  font-weight: 700;
}

.admin-layout__user {
  font-size: 14px;
  color: #d1d5db;
}

.admin-layout__user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-layout__logout {
  min-height: 32px;
  padding: 4px 12px;
  border: 1px solid #4b5563;
  border-radius: 6px;
  background: transparent;
  color: #ffffff;
  cursor: pointer;
}

.admin-layout__sidebar {
  grid-area: sidebar;
  padding: 20px 16px;
  background: #ffffff;
  border-right: 1px solid #d1d5db;
}

.admin-layout__menu-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #4b5563;
}

.admin-layout__menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-layout__menu-item {
  width: 100%;
  min-height: 38px;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #f9fafb;
  color: #1f2937;
  font: inherit;
  text-align: left;
  text-decoration: none;
  cursor: default;
}

.admin-layout__menu-item--active {
  border-color: #2563eb;
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 700;
}

.admin-layout__main {
  grid-area: main;
  padding: 24px;
  background: #f3f4f6;
}

@media (max-width: 720px) {
  .admin-layout {
    grid-template-areas:
      "header"
      "sidebar"
      "main";
    grid-template-columns: 1fr;
    grid-template-rows: auto auto 1fr;
  }

  .admin-layout__header {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
    padding: 16px;
  }

  .admin-layout__user-area {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .admin-layout__sidebar {
    border-right: 0;
    border-bottom: 1px solid #d1d5db;
  }

  .admin-layout__menu {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
