import { clearAuth } from '../stores/auth'
import { getToken } from './token'

const BASE_URL = '/api'

export class RequestError extends Error {
  constructor(message, code, status) {
    super(message)
    this.name = 'RequestError'
    this.code = code
    this.status = status
  }
}

function redirectToLogin() {
  if (window.location.pathname === '/login') {
    return
  }

  const currentPath = `${window.location.pathname}${window.location.search}`
  window.location.assign(`/login?redirect=${encodeURIComponent(currentPath)}`)
}

function handleUnauthorized(redirectOnUnauthorized) {
  clearAuth()
  if (redirectOnUnauthorized) {
    redirectToLogin()
  }
}

async function parseResponse(response) {
  const text = await response.text()
  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch {
    throw new RequestError('响应数据格式错误', undefined, response.status)
  }
}

export async function request(path, options = {}) {
  const {
    method = 'GET',
    body,
    headers = {},
    skipAuth = false,
    redirectOnUnauthorized = true,
  } = options

  const requestHeaders = {
    Accept: 'application/json',
    ...headers,
  }

  if (body !== undefined) {
    requestHeaders['Content-Type'] = 'application/json'
  }

  const token = getToken()
  if (token && !skipAuth) {
    requestHeaders.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers: requestHeaders,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new RequestError('网络异常，请稍后重试')
  }

  const payload = await parseResponse(response)

  if (response.status === 401) {
    handleUnauthorized(redirectOnUnauthorized)
    throw new RequestError(payload?.message || '未登录或认证失败', 401, response.status)
  }

  if (!response.ok && !payload) {
    throw new RequestError('请求失败，请稍后重试', undefined, response.status)
  }

  if (payload && typeof payload.code === 'number') {
    if (payload.code === 0) {
      return payload.data
    }

    if (payload.code === 401) {
      handleUnauthorized(redirectOnUnauthorized)
    }

    throw new RequestError(payload.message || '请求失败', payload.code, response.status)
  }

  return payload
}
