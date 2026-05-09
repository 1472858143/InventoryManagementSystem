<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>商品分类管理</h2>
        <p>维护商品分类体系，分类将在新增商品时使用</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadCategories">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增分类</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="categories" v-loading="loading" stripe row-key="id" empty-text="暂无分类记录">
        <el-table-column prop="categoryName" label="分类名称" min-width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              link
              :loading="statusUpdatingId === row.id"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增分类" width="400px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model.trim="form.categoryName" autocomplete="off" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { createCategory, getCategories, updateCategoryStatus } from '../../api/category'

const categories = ref([])
const loading = ref(false)
const statusUpdatingId = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ categoryName: '' })

const rules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

onMounted(loadCategories)

async function loadCategories() {
  loading.value = true
  try {
    const result = await getCategories()
    categories.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询分类列表失败')
    categories.value = []
  } finally {
    loading.value = false
  }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  form.categoryName = ''
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createCategory({ categoryName: form.categoryName })
    ElMessage.success('新增分类成功')
    dialogVisible.value = false
    await loadCategories()
  } catch (e) {
    ElMessage.error(e.message || '新增分类失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  statusUpdatingId.value = row.id
  try {
    await updateCategoryStatus(row.id, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success(row.status === 1 ? '已禁用该分类' : '已启用该分类')
    await loadCategories()
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
