<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p>维护商品基础信息和上下架状态</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增商品</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="products" v-loading="loading" stripe row-key="id" empty-text="暂无商品记录">
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="进价" width="90">
          <template #default="{ row }">¥ {{ Number(row.purchasePrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="售价" width="90">
          <template #default="{ row }">¥ {{ Number(row.salePrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'warning' : 'primary'"
              size="small" link
              :loading="statusUpdatingId === row.id"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增商品" width="520px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品编号" prop="productCode">
          <el-input v-model.trim="form.productCode" autocomplete="off" placeholder="如：P001" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model.trim="form.productName" autocomplete="off" placeholder="如：矿泉水" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model.trim="form.category" autocomplete="off" placeholder="如：饮料" />
        </el-form-item>
        <el-form-item label="进价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
        <el-form-item label="售价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { createProduct, getProducts, updateProductStatus } from '../../api/product'

const products = ref([])
const loading = ref(false)
const statusUpdatingId = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ productCode: '', productName: '', category: '', purchasePrice: null, salePrice: null })

const rules = {
  productCode: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  purchasePrice: [{ required: true, message: '请输入进价', trigger: 'change', type: 'number' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'change', type: 'number' }],
}

onMounted(loadProducts)

async function loadProducts() {
  loading.value = true
  try {
    const result = await getProducts()
    products.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询商品列表失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { productCode: '', productName: '', category: '', purchasePrice: null, salePrice: null })
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createProduct({ productCode: form.productCode, productName: form.productName, category: form.category, purchasePrice: form.purchasePrice, salePrice: form.salePrice })
    ElMessage.success('新增商品成功')
    dialogVisible.value = false
    await loadProducts()
  } catch (e) {
    ElMessage.error(e.message || '新增商品失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  statusUpdatingId.value = row.id
  try {
    await updateProductStatus({ productId: row.id, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('状态更新成功')
    await loadProducts()
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    statusUpdatingId.value = null
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
