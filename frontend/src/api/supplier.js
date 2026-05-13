import { request } from '../utils/request'

export function getSuppliers(keyword) {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  return request(`/suppliers${query}`)
}

export function createSupplier(payload) {
  return request('/suppliers', { method: 'POST', body: payload })
}

export function updateSupplier(id, payload) {
  return request(`/suppliers/${id}`, { method: 'PUT', body: payload })
}

export function updateSupplierStatus(id, status) {
  return request(`/suppliers/${id}/status`, { method: 'PATCH', body: { status } })
}
