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
