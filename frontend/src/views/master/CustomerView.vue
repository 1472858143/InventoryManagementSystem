<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>客户管理</h2>
        <p>维护客户基础资料</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadCustomers">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增客户</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model.trim="keyword"
        placeholder="搜索编码或名称"
        clearable
        style="width: 240px"
        @keyup.enter="loadCustomers"
      />
      <el-button :icon="Search" @click="loadCustomers">搜索</el-button>
    </div>

    <el-card>
      <el-table :data="customers" v-loading="loading" stripe row-key="id" empty-text="暂无客户记录">
        <el-table-column prop="code" label="编码" min-width="120" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column label="联系人" min-width="100">
          <template #default="{ row }">{{ row.contactPerson || '—' }}</template>
        </el-table-column>
        <el-table-column label="联系电话" min-width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              size="small" link
              :type="row.status === 1 ? 'danger' : 'primary'"
              :loading="statusUpdatingId === row.id"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑客户' : '新增客户'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item v-if="!editMode" label="编码" prop="code">
          <el-input v-model.trim="form.code" placeholder="选填，留空自动生成" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model.trim="form.contactPerson" placeholder="选填" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model.trim="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model.trim="form.address" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="form.remark" type="textarea" :rows="2" placeholder="选填" />
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
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { createCustomer, getCustomers, updateCustomer, updateCustomerStatus } from '../../api/customer'

const customers = ref([])
const loading = ref(false)
const keyword = ref('')
const statusUpdatingId = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const editMode = ref(false)
const editId = ref(null)
const formRef = ref(null)
const form = reactive({ code: '', name: '', contactPerson: '', phone: '', address: '', remark: '' })

const rules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
}

onMounted(loadCustomers)

async function loadCustomers() {
  loading.value = true
  try {
    const result = await getCustomers(keyword.value || undefined)
    customers.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询客户列表失败')
    customers.value = []
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editMode.value = false
  editId.value = null
  Object.assign(form, { code: '', name: '', contactPerson: '', phone: '', address: '', remark: '' })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  editMode.value = true
  editId.value = row.id
  Object.assign(form, {
    code: row.code || '',
    name: row.name || '',
    contactPerson: row.contactPerson || '',
    phone: row.phone || '',
    address: row.address || '',
    remark: row.remark || '',
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editMode.value) {
      const payload = {
        name: form.name,
        contactPerson: form.contactPerson || undefined,
        phone: form.phone || undefined,
        address: form.address || undefined,
        remark: form.remark || undefined,
      }
      await updateCustomer(editId.value, payload)
      ElMessage.success('编辑客户成功')
    } else {
      const payload = {
        code: form.code || undefined,
        name: form.name,
        contactPerson: form.contactPerson || undefined,
        phone: form.phone || undefined,
        address: form.address || undefined,
        remark: form.remark || undefined,
      }
      await createCustomer(payload)
      ElMessage.success('新增客户成功')
    }
    dialogVisible.value = false
    await loadCustomers()
  } catch (e) {
    ElMessage.error(e.message || (editMode.value ? '编辑客户失败' : '新增客户失败'))
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  statusUpdatingId.value = row.id
  try {
    await updateCustomerStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    await loadCustomers()
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
.search-bar { display: flex; gap: 8px; align-items: center; }
</style>
