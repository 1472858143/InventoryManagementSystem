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
