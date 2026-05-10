<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>上架补货</h2>
        <p>手动更新商品的货架陈列状态（不影响库存数量）</p>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="batch-bar" v-show="selected.length > 0">
        <span class="batch-tip">已选 {{ selected.length }} 个商品，批量改为：</span>
        <el-select v-model="batchStatus" size="small" style="width:120px">
          <el-option label="未上架" value="未上架" />
          <el-option label="缺货" value="缺货" />
          <el-option label="较少" value="较少" />
          <el-option label="充足" value="充足" />
        </el-select>
        <el-button type="primary" size="small" :loading="batchLoading" @click="handleBatchUpdate">批量更新</el-button>
        <el-button size="small" @click="tableRef?.clearSelection()">取消选择</el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="stocks"
        v-loading="loading"
        stripe
        row-key="productId"
        @selection-change="selected = $event"
        empty-text="暂无库存记录"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="quantity" label="总库存" width="90" align="right" />
        <el-table-column label="陈列状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.shelfStatus)" size="small">{{ row.shelfStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="改为" width="150">
          <template #default="{ row }">
            <el-select v-model="row._newStatus" size="small" style="width:110px" :placeholder="row.shelfStatus">
              <el-option label="未上架" value="未上架" />
              <el-option label="缺货" value="缺货" />
              <el-option label="较少" value="较少" />
              <el-option label="充足" value="充足" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary" size="small" link
              :disabled="!row._newStatus || row._newStatus === row.shelfStatus"
              @click="handleSingleUpdate(row)"
            >更新</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { getStocks, updateShelfStatus, batchUpdateShelfStatus } from '../../api/stock'

const stocks = ref([])
const loading = ref(false)
const batchLoading = ref(false)
const selected = ref([])
const batchStatus = ref('充足')
const tableRef = ref(null)

function statusType(status) {
  return { '充足': 'success', '较少': 'warning', '缺货': 'danger', '未上架': 'info' }[status] || 'info'
}

onMounted(loadStocks)

async function loadStocks() {
  loading.value = true
  try {
    const result = await getStocks()
    stocks.value = (Array.isArray(result) ? result : []).map(r => ({ ...r, _newStatus: null }))
  } catch (e) {
    ElMessage.error(e.message || '加载库存列表失败')
  } finally {
    loading.value = false
  }
}

async function handleSingleUpdate(row) {
  try {
    await updateShelfStatus(row.productId, row._newStatus)
    ElMessage.success(`${row.productName} 状态更新为"${row._newStatus}"`)
    await loadStocks()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  }
}

async function handleBatchUpdate() {
  if (!selected.value.length) return
  batchLoading.value = true
  try {
    const ids = selected.value.map(r => r.productId)
    await batchUpdateShelfStatus(ids, batchStatus.value)
    ElMessage.success(`已将 ${ids.length} 个商品状态更新为"${batchStatus.value}"`)
    tableRef.value?.clearSelection()
    await loadStocks()
  } catch (e) {
    ElMessage.error(e.message || '批量更新失败')
  } finally {
    batchLoading.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: var(--color-text); margin-bottom: 4px; }
.page-header p { font-size: 13px; color: var(--color-text-secondary); }
.table-card { border: 1px solid var(--color-border); }
.batch-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0 14px;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 8px;
}
.batch-tip { font-size: 13px; color: var(--color-text-secondary); }
</style>
