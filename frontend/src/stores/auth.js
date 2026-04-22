import { reactive } from 'vue'
import { getToken, removeToken, saveToken } from '../utils/token'

export const authState = reactive({
  token: getToken(),
  currentUser: null,
  roles: [],
  isAuthenticated: false,
  initialized: false,
})

export function setLoginSession(loginResult) {
  saveToken(loginResult.token)
  authState.token = loginResult.token
  authState.currentUser = {
    username: loginResult.username,
  }
  authState.roles = Array.isArray(loginResult.roles) ? loginResult.roles : []
  authState.isAuthenticated = true
  authState.initialized = true
}

export function setCurrentUser(currentUser) {
  authState.currentUser = currentUser
  authState.isAuthenticated = true
  authState.initialized = true
}

export function clearAuth() {
  removeToken()
  authState.token = null
  authState.currentUser = null
  authState.roles = []
  authState.isAuthenticated = false
  authState.initialized = true
}
