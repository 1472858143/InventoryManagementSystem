<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>维护系统用户账号与角色分配</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadUsers">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog">新增用户</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="users" v-loading="loading" stripe row-key="id" empty-text="暂无用户记录">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="真实姓名" min-width="120">
          <template #default="{ row }">{{ row.realName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="140">
          <template #default="{ row }">
            <el-tag v-for="code in (row.roleCodes || [])" :key="code" type="info" size="small" style="margin-right:4px">{{ code }}</el-tag>
            <span v-if="!row.roleCodes?.length">—</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ row.createTime?.slice(0, 19).replace('T', ' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'danger' : 'primary'"
              size="small" link
              :loading="statusUpdatingId === row.id"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增用户" width="480px" :close-on-click-modal="false" @open="handleDialogOpen">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" autocomplete="off" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="new-password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model.trim="form.realName" autocomplete="off" placeholder="选填" />
        </el-form-item>
        <el-form-item label="角色ID" prop="roleIdsInput">
          <el-input v-model.trim="form.roleIdsInput" placeholder="输入角色ID，多个用英文逗号分隔，如: 1 或 1,2" />
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
import { createUser, getUsers, updateUserStatus } from '../../api/user'

const users = ref([])
const loading = ref(false)
const statusUpdatingId = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '', realName: '', roleIdsInput: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIdsInput: [{ required: true, message: '请输入角色ID', trigger: 'blur' }],
}

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const result = await getUsers()
    users.value = Array.isArray(result) ? result : []
  } catch (e) {
    ElMessage.error(e.message || '查询用户列表失败')
    users.value = []
  } finally {
    loading.value = false
  }
}

function openDialog() { dialogVisible.value = true }

function handleDialogOpen() {
  Object.assign(form, { username: '', password: '', realName: '', roleIdsInput: '' })
  nextTick(() => formRef.value?.clearValidate())
}

function parseRoleIds(input) {
  return input.split(',').map(s => Number(s.trim())).filter(n => Number.isInteger(n) && n > 0)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const roleIds = parseRoleIds(form.roleIdsInput)
  if (roleIds.length === 0) {
    ElMessage.warning('角色ID格式不正确，请填写正整数，多个用英文逗号分隔')
    return
  }
  submitting.value = true
  try {
    await createUser({ username: form.username, password: form.password, realName: form.realName || undefined, roleIds })
    ElMessage.success('新增用户成功')
    dialogVisible.value = false
    await loadUsers()
  } catch (e) {
    ElMessage.error(e.message || '新增用户失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  statusUpdatingId.value = row.id
  try {
    await updateUserStatus({ userId: row.id, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('状态更新成功')
    await loadUsers()
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
