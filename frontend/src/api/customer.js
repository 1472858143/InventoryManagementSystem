import { request } from '../utils/request'

export function getCustomers(keyword) {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  return request(`/customers${query}`)
}

export function createCustomer(payload) {
  return request('/customers', { method: 'POST', body: payload })
}

export function updateCustomer(id, payload) {
  return request(`/customers/${id}`, { method: 'PUT', body: payload })
}

export function updateCustomerStatus(id, status) {
  return request(`/customers/${id}/status`, { method: 'PATCH', body: { status } })
}
