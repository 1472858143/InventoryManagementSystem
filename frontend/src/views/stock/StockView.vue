<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>库存总览</h2>
        <p>查看所有商品库存数量与陈列状态，可设置预警阈值</p>
      </div>
      <el-button :icon="RefreshCcw" :loading="loading" @click="loadStocks" size="small">刷新</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="stocks" v-loading="loading" stripe row-key="productId" empty-text="暂无库存记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column label="总库存" width="90" align="right">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.quantity < row.minStock }">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="陈列状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.shelfStatus)" size="small">{{ row.shelfStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="下限" width="70" align="right" />
        <el-table-column prop="maxStock" label="上限" width="70" align="right" />
        <el-table-column label="库存预警" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.quantity < row.minStock" type="danger" size="small">低于下限</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="150">
          <template #default="{ row }">{{ row.updateTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openLimitDialog(row)">设置上下限</el-button>
            <el-button type="primary" size="small" link @click="openStatusDialog(row)">改状态</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="limitVisible" title="设置库存上下限" width="400px" :close-on-click-modal="false">
      <el-form ref="limitFormRef" :model="limitForm" :rules="limitRules" label-width="80px">
        <el-form-item label="商品">
          <el-text>{{ currentRow?.productName }}</el-text>
        </el-form-item>
        <el-form-item label="库存下限" prop="minStock">
          <el-input-number v-model="limitForm.minStock" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="库存上限" prop="maxStock">
          <el-input-number v-model="limitForm.maxStock" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="limitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleLimitSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="修改陈列状态" width="380px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="商品">
          <el-text>{{ currentRow?.productName }}</el-text>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusType(currentRow?.shelfStatus)" size="small">{{ currentRow?.shelfStatus }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" style="width:100%">
            <el-option label="未上架" value="未上架" />
            <el-option label="缺货" value="缺货" />
            <el-option label="较少" value="较少" />
            <el-option label="充足" value="充足" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleStatusSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { RefreshCcw } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { getStocks, updateStockLimit, updateShelfStatus } from '../../api/stock'

const stocks = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentRow = ref(null)

const limitVisible = ref(false)
const limitFormRef = ref(null)
const limitForm = reactive({ minStock: 0, maxStock: 0 })
const limitRules = {
  minStock: [{ required: true, type: 'number', message: '请输入下限', trigger: 'change' }],
  maxStock: [
    { required: true, type: 'number', message: '请输入上限', trigger: 'change' },
    { validator: (_, v, cb) => v < limitForm.minStock ? cb(new Error('上限不能低于下限')) : cb(), trigger: 'change' },
  ],
}

const statusVisible = ref(false)
const newStatus = ref('充足')

function statusType(status) {
  const map = { '充足': 'success', '较少': 'warning', '缺货': 'danger', '未上架': 'info' }
  return map[status] || 'info'
}

onMounted(loadStocks)

async function loadStocks() {
  loading.value = true
  try {
    const result = await getStocks()
    stocks.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询库存失败')
    stocks.value = []
  } finally {
    loading.value = false
  }
}

function openLimitDialog(row) {
  currentRow.value = row
  limitForm.minStock = row.minStock
  limitForm.maxStock = row.maxStock
  limitVisible.value = true
  nextTick(() => limitFormRef.value?.clearValidate())
}

async function handleLimitSubmit() {
  const valid = await limitFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateStockLimit(currentRow.value.productId, { minStock: limitForm.minStock, maxStock: limitForm.maxStock })
    ElMessage.success('上下限更新成功')
    limitVisible.value = false
    await loadStocks()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

function openStatusDialog(row) {
  currentRow.value = row
  newStatus.value = row.shelfStatus || '充足'
  statusVisible.value = true
}

async function handleStatusSubmit() {
  submitting.value = true
  try {
    await updateShelfStatus(currentRow.value.productId, newStatus.value)
    ElMessage.success('陈列状态更新成功')
    statusVisible.value = false
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
.page-header h2 { font-size: 18px; font-weight: 600; color: var(--color-text); margin-bottom: 4px; }
.page-header p { font-size: 13px; color: var(--color-text-secondary); }
.table-card { border: 1px solid var(--color-border); }
.text-danger { color: var(--color-danger); font-weight: 600; }
</style>
