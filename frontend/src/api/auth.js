import { request } from '../utils/request'

export function login(credentials) {
  return request('/auth/login', {
    method: 'POST',
    body: credentials,
    skipAuth: true,
    redirectOnUnauthorized: false,
  })
}

export function logout() {
  return request('/auth/logout', {
    method: 'POST',
    redirectOnUnauthorized: false,
  })
}

export function getCurrentUser(options = {}) {
  return request('/auth/me', {
    method: 'GET',
    ...options,
  })
}
