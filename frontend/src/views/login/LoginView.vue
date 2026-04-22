<template>
  <main class="login-page">
    <section class="login-panel">
      <h1>超市库存管理系统</h1>
      <p class="login-panel__subtitle">请登录后进入后台管理界面</p>

      <form class="login-form" @submit.prevent="handleSubmit">
        <label class="login-form__field">
          <span>用户名</span>
          <input
            v-model.trim="form.username"
            autocomplete="username"
            name="username"
            type="text"
            placeholder="请输入用户名"
          />
        </label>

        <label class="login-form__field">
          <span>密码</span>
          <input
            v-model="form.password"
            autocomplete="current-password"
            name="password"
            type="password"
            placeholder="请输入密码"
          />
        </label>

        <p v-if="errorMessage" class="login-form__error">{{ errorMessage }}</p>

        <button class="login-form__submit" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../../api/auth'
import { clearAuth, setLoginSession } from '../../stores/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  errorMessage.value = ''

  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  try {
    const loginResult = await login({
      username: form.username,
      password: form.password,
    })
    setLoginSession(loginResult)
    form.password = ''
    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirectPath)
  } catch (error) {
    clearAuth()
    errorMessage.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background: #f3f4f6;
}

.login-panel {
  width: 100%;
  max-width: 400px;
  padding: 28px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
}

.login-panel h1 {
  margin-bottom: 8px;
  font-size: 24px;
  color: #111827;
}

.login-panel__subtitle {
  margin-bottom: 24px;
  color: #4b5563;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #374151;
  font-size: 14px;
}

.login-form__field input {
  min-height: 40px;
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #111827;
}

.login-form__error {
  padding: 10px 12px;
  border: 1px solid #fecaca;
  border-radius: 6px;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 14px;
}

.login-form__submit {
  min-height: 42px;
  border: 1px solid #1d4ed8;
  border-radius: 6px;
  background: #2563eb;
  color: #ffffff;
  cursor: pointer;
}

.login-form__submit:disabled {
  border-color: #93c5fd;
  background: #93c5fd;
  cursor: not-allowed;
}
</style>
