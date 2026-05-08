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
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column label="仓库库存" width="100">
          <template #default="{ row }">
            <el-text>{{ row.warehouseQuantity }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="上架库存" width="100">
          <template #default="{ row }">
            <el-text :type="row.shelfQuantity < row.minStock ? 'danger' : ''">{{ row.shelfQuantity }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="下限" width="80" />
        <el-table-column prop="maxStock" label="上限" width="80" />
        <el-table-column label="预警状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.shelfQuantity < row.minStock" type="danger" size="small">上架不足</el-tag>
            <el-tag v-else-if="row.shelfQuantity > row.maxStock" type="warning" size="small">上架过多</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="160">
          <template #default="{ row }">{{ row.updateTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">设置上下限</el-button>
            <el-button type="primary" size="small" link :icon="Sort" @click="openRestockDialog(row)">补货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 设置库存上下限 Dialog -->
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

    <!-- 补货 Dialog -->
    <el-dialog
      v-model="restockDialogVisible"
      title="商品补货"
      width="400px"
      :close-on-click-modal="false"
      @open="handleRestockDialogOpen"
    >
      <el-form ref="restockFormRef" :model="restockForm" :rules="restockRules" label-width="100px">
        <el-form-item label="商品名称">
          <el-text>{{ currentRestockProduct?.productName }}</el-text>
        </el-form-item>
        <el-form-item label="当前仓库库存">
          <el-text>{{ currentRestockProduct?.warehouseQuantity }}</el-text>
        </el-form-item>
        <el-form-item label="当前上架库存">
          <el-text>{{ currentRestockProduct?.shelfQuantity }}</el-text>
        </el-form-item>
        <el-form-item label="补货数量" prop="quantity">
          <el-input-number
            v-model="restockForm.quantity"
            :min="1"
            :max="currentRestockProduct?.warehouseQuantity"
            :precision="0"
            :step="1"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="restockForm.operator" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="restockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="restockSubmitting" @click="handleRestockSubmit">确认补货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Refresh, Sort } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { getStocks, updateStockLimit, restockProduct } from '../../api/stock'
import { authState } from '../../stores/auth'

const stocks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const currentProduct = ref(null)
const form = reactive({ minStock: 0, maxStock: 0 })

const restockDialogVisible = ref(false)
const restockSubmitting = ref(false)
const restockFormRef = ref(null)
const currentRestockProduct = ref(null)
const restockForm = reactive({ quantity: 1, operator: '' })

const rules = {
  minStock: [{ required: true, message: '请输入库存下限', trigger: 'change', type: 'number' }],
  maxStock: [{ required: true, message: '请输入库存上限', trigger: 'change', type: 'number' }],
}

const restockRules = {
  quantity: [{ required: true, message: '请输入补货数量', trigger: 'change', type: 'number' }],
  operator: [{ required: true, message: '操作人不能为空', trigger: 'blur' }],
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

function openRestockDialog(row) {
  currentRestockProduct.value = row
  restockForm.quantity = 1
  restockDialogVisible.value = true
}

function handleRestockDialogOpen() {
  restockForm.operator = authState.currentUser?.username || ''
  nextTick(() => restockFormRef.value?.clearValidate())
}

async function handleRestockSubmit() {
  const valid = await restockFormRef.value.validate().catch(() => false)
  if (!valid) return
  restockSubmitting.value = true
  try {
    await restockProduct(currentRestockProduct.value.productId, {
      quantity: restockForm.quantity,
      operator: restockForm.operator,
    })
    ElMessage.success('补货成功')
    restockDialogVisible.value = false
    await loadStocks()
  } catch (e) {
    ElMessage.error(e.message || '补货失败')
  } finally {
    restockSubmitting.value = false
  }
}
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.page-header p { font-size: 13px; color: #8c8c8c; }
</style>
