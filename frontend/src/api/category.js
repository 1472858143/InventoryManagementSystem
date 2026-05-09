import { request } from '../utils/request'

export function getCategories() {
  return request('/categories')
}
export function getEnabledCategories() {
  return request('/categories/enabled')
}
export function createCategory(payload) {
  return request('/categories', { method: 'POST', body: payload })
}
export function updateCategoryStatus(id, payload) {
  return request(`/categories/${id}/status`, { method: 'PUT', body: payload })
}
