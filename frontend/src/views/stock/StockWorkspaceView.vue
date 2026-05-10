<template>
  <div class="workspace">
    <div class="page-header">
      <h2>库存工作台</h2>
      <p>陈列状态总览与快速操作入口</p>
    </div>

    <div class="stat-grid" v-loading="loading">
      <div class="stat-card">
        <div class="stat-value">{{ total }}</div>
        <div class="stat-label">商品总 SKU</div>
      </div>
      <div class="stat-card warn">
        <div class="stat-value">{{ lowStockCount }}</div>
        <div class="stat-label">库存低于下限</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-value">{{ outOfShelfCount }}</div>
        <div class="stat-label">货架缺货</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ totalQty }}</div>
        <div class="stat-label">总库存数量</div>
      </div>
    </div>

    <el-card shadow="never" class="section-card">
      <template #header>陈列状态分布</template>
      <div class="status-grid">
        <div v-for="s in statusSummary" :key="s.label" class="status-item">
          <el-tag :type="s.type" size="large">{{ s.label }}</el-tag>
          <div class="status-count">{{ s.count }} 个商品</div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>快速操作</template>
      <div class="action-grid">
        <el-button @click="$router.push('/inbounds')">入库单据</el-button>
        <el-button @click="$router.push('/shelf-restock')">上架补货</el-button>
        <el-button @click="$router.push('/outbounds')">出库单据</el-button>
        <el-button @click="$router.push('/stockchecks')">库存盘点</el-button>
        <el-button @click="$router.push('/stocks')">库存总览</el-button>
      </div>
    </el-card>

    <el-card v-if="warnings.length > 0" shadow="never" class="section-card">
      <template #header>
        <span style="color: var(--color-danger)">缺货预警（{{ warnings.length }} 件）</span>
      </template>
      <el-table :data="warnings" size="small">
        <el-table-column prop="productCode" label="编号" width="110" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="库存" width="80" align="right" />
        <el-table-column prop="minStock" label="下限" width="70" align="right" />
        <el-table-column label="陈列状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.shelfStatus)" size="small">{{ row.shelfStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="" width="100">
          <template #default>
            <el-button size="small" link type="primary" @click="$router.push('/shelf-restock')">去补货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { getStocks } from '../../api/stock'

const stocks = ref([])
const loading = ref(false)

const total = computed(() => stocks.value.length)
const totalQty = computed(() => stocks.value.reduce((s, r) => s + (r.quantity || 0), 0))
const lowStockCount = computed(() => stocks.value.filter(r => r.quantity < r.minStock).length)
const outOfShelfCount = computed(() => stocks.value.filter(r => r.shelfStatus === '缺货').length)

const statusSummary = computed(() => {
  const counts = { '充足': 0, '较少': 0, '缺货': 0, '未上架': 0 }
  stocks.value.forEach(r => { if (r.shelfStatus in counts) counts[r.shelfStatus]++ })
  return [
    { label: '充足', count: counts['充足'], type: 'success' },
    { label: '较少', count: counts['较少'], type: 'warning' },
    { label: '缺货', count: counts['缺货'], type: 'danger' },
    { label: '未上架', count: counts['未上架'], type: 'info' },
  ]
})

const warnings = computed(() =>
  stocks.value.filter(r => r.quantity < r.minStock || r.shelfStatus === '缺货')
)

function statusType(status) {
  return { '充足': 'success', '较少': 'warning', '缺货': 'danger', '未上架': 'info' }[status] || 'info'
}

onMounted(async () => {
  loading.value = true
  try {
    const result = await getStocks()
    stocks.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '加载工作台数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.workspace { display: flex; flex-direction: column; gap: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: var(--color-text); margin-bottom: 4px; }
.page-header p { font-size: 13px; color: var(--color-text-secondary); }
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-card);
}
.stat-card.warn .stat-value { color: var(--color-warning); }
.stat-card.danger .stat-value { color: var(--color-danger); }
.stat-value { font-size: 32px; font-weight: 700; color: var(--color-text); line-height: 1.1; }
.stat-label { font-size: 13px; color: var(--color-text-secondary); margin-top: 4px; }
.section-card { border: 1px solid var(--color-border); }
.status-grid { display: flex; gap: 32px; padding: 8px 0; }
.status-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.status-count { font-size: 20px; font-weight: 600; color: var(--color-text); }
.action-grid { display: flex; flex-wrap: wrap; gap: 12px; }
</style>
