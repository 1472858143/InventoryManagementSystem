<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon size="40" color="#1890ff"><Box /></el-icon>
        <h1>超市库存管理系统</h1>
        <p>Supermarket Inventory Management System</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            :prefix-icon="UserIcon"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            :prefix-icon="LockIcon"
            show-password
            @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />

        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { Lock as LockIcon, User as UserIcon } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../../api/auth'
import { clearAuth, setLoginSession } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleSubmit() {
  errorMessage.value = ''
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const loginResult = await login({ username: form.username, password: form.password })
    setLoginSession(loginResult)
    form.password = ''
    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirectPath)
  } catch (error) {
    clearAuth()
    errorMessage.value = error.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #0d1117 0%, #1a2744 50%, #2d3561 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 32px;
  text-align: center;
}

.login-header h1 {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.login-header p {
  font-size: 12px;
  color: #9ca3af;
  letter-spacing: 0.5px;
}
</style>
