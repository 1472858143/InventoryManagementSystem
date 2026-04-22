<template>
  <section class="user-page">
    <header class="user-page__header">
      <div>
        <h1>用户管理</h1>
        <p>用于维护系统用户和状态</p>
      </div>
    </header>

    <div class="user-page__actions">
      <button type="button" @click="showCreateForm = !showCreateForm">
        {{ showCreateForm ? '收起表单' : '新增用户' }}
      </button>
    </div>

    <form v-if="showCreateForm" class="user-form" @submit.prevent="handleCreateUser">
      <label class="user-form__field">
        <span>用户名</span>
        <input v-model.trim="createForm.username" type="text" autocomplete="off" />
      </label>

      <label class="user-form__field">
        <span>密码</span>
        <input v-model="createForm.password" type="password" autocomplete="new-password" />
      </label>

      <label class="user-form__field">
        <span>真实姓名</span>
        <input v-model.trim="createForm.realName" type="text" autocomplete="off" />
      </label>

      <label class="user-form__field">
        <span>状态</span>
        <select v-model.number="createForm.status" disabled>
          <option :value="1">启用</option>
        </select>
        <small>新增用户由后端创建为启用状态，状态变更请使用列表操作。</small>
      </label>

      <label class="user-form__field">
        <span>角色ID</span>
        <input v-model.trim="createForm.roleIds" type="text" placeholder="例如：1 或 1,2" />
      </label>

      <p v-if="formError" class="user-page__error">{{ formError }}</p>
      <p v-if="formSuccess" class="user-page__success">{{ formSuccess }}</p>

      <div class="user-form__actions">
        <button type="submit" :disabled="creating">
          {{ creating ? '提交中...' : '提交新增' }}
        </button>
      </div>
    </form>

    <section class="user-list">
      <div class="user-list__title">
        <h2>用户列表</h2>
        <button type="button" :disabled="loading" @click="loadUsers">
          {{ loading ? '查询中...' : '刷新' }}
        </button>
      </div>

      <p v-if="listError" class="user-page__error">{{ listError }}</p>
      <p v-if="statusError" class="user-page__error">{{ statusError }}</p>

      <div v-if="loading" class="user-list__state">正在加载用户列表...</div>
      <div v-else-if="users.length === 0" class="user-list__state">暂无用户记录</div>
      <table v-else class="user-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>真实姓名</th>
            <th>状态</th>
            <th>角色编码</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.username || '-' }}</td>
            <td>{{ user.realName || '-' }}</td>
            <td>{{ formatStatus(user.status) }}</td>
            <td>{{ formatRoleCodes(user.roleCodes) }}</td>
            <td>
              <button
                type="button"
                :disabled="statusUpdatingUserId === user.id"
                @click="handleToggleStatus(user)"
              >
                {{ statusUpdatingUserId === user.id ? '处理中...' : getStatusActionText(user.status) }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createUser, getUsers, updateUserStatus } from '../../api/user'

const users = ref([])
const loading = ref(false)
const creating = ref(false)
const statusUpdatingUserId = ref(null)
const showCreateForm = ref(false)
const listError = ref('')
const formError = ref('')
const formSuccess = ref('')
const statusError = ref('')

const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  status: 1,
  roleIds: '',
})

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  listError.value = ''
  statusError.value = ''

  try {
    const result = await getUsers()
    users.value = Array.isArray(result) ? result : []
  } catch (error) {
    listError.value = error.message || '查询用户列表失败'
    users.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreateUser() {
  formError.value = ''
  formSuccess.value = ''

  const roleIds = parseRoleIds(createForm.roleIds)
  if (!createForm.username || !createForm.password || roleIds.length === 0) {
    formError.value = '请填写用户名、密码和角色ID'
    return
  }

  if (roleIds.some((roleId) => !Number.isInteger(roleId) || roleId <= 0)) {
    formError.value = '角色ID格式不正确，请使用逗号分隔的数字'
    return
  }

  creating.value = true
  try {
    await createUser({
      username: createForm.username,
      password: createForm.password,
      realName: createForm.realName,
      roleIds,
    })
    resetCreateForm()
    showCreateForm.value = false
    formSuccess.value = '新增用户成功'
    await loadUsers()
  } catch (error) {
    formError.value = error.message || '新增用户失败'
  } finally {
    creating.value = false
  }
}

async function handleToggleStatus(user) {
  statusError.value = ''
  statusUpdatingUserId.value = user.id

  try {
    await updateUserStatus({
      userId: user.id,
      status: user.status === 1 ? 0 : 1,
    })
    await loadUsers()
  } catch (error) {
    statusError.value = error.message || '更新用户状态失败'
  } finally {
    statusUpdatingUserId.value = null
  }
}

function parseRoleIds(value) {
  const parts = value
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item)

  return parts.map((item) => Number(item))
}

function resetCreateForm() {
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.status = 1
  createForm.roleIds = ''
}

function formatStatus(status) {
  if (status === 1) {
    return '启用'
  }
  if (status === 0) {
    return '禁用'
  }
  return status ?? '-'
}

function getStatusActionText(status) {
  return status === 1 ? '禁用' : '启用'
}

function formatRoleCodes(roleCodes) {
  if (!Array.isArray(roleCodes) || roleCodes.length === 0) {
    return '-'
  }
  return roleCodes.join(', ')
}
</script>

<style scoped>
.user-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-page__header h1 {
  margin-bottom: 8px;
  font-size: 24px;
  color: #111827;
}

.user-page__header p {
  color: #4b5563;
}

.user-page__actions,
.user-list__title,
.user-form__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-page button {
  min-height: 36px;
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #ffffff;
  color: #111827;
  cursor: pointer;
}

.user-page button:disabled {
  color: #6b7280;
  cursor: not-allowed;
}

.user-form,
.user-list {
  padding: 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
}

.user-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.user-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #374151;
  font-size: 14px;
}

.user-form__field input,
.user-form__field select {
  min-height: 38px;
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.user-form__field small {
  color: #6b7280;
}

.user-form__actions,
.user-page__error,
.user-page__success {
  grid-column: 1 / -1;
}

.user-page__error {
  padding: 10px 12px;
  border: 1px solid #fecaca;
  border-radius: 6px;
  background: #fef2f2;
  color: #b91c1c;
}

.user-page__success {
  padding: 10px 12px;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  background: #f0fdf4;
  color: #15803d;
}

.user-list {
  overflow-x: auto;
}

.user-list__title {
  justify-content: space-between;
  margin-bottom: 12px;
}

.user-list__title h2 {
  font-size: 18px;
  color: #111827;
}

.user-list__state {
  padding: 24px;
  color: #6b7280;
  text-align: center;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  white-space: nowrap;
}

.user-table th {
  background: #f9fafb;
  color: #374151;
  font-weight: 700;
}

@media (max-width: 720px) {
  .user-form {
    grid-template-columns: 1fr;
  }
}
</style>
