import { request } from '../utils/request'

export function getUsers() {
  return request('/users')
}

export function createUser(payload) {
  return request('/users', {
    method: 'POST',
    body: payload,
  })
}

export function updateUserStatus(payload) {
  return request('/users/status', {
    method: 'PUT',
    body: payload,
  })
}
