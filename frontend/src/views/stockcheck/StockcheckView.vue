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
