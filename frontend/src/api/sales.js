import { request } from '../utils/request'

function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

export function createSalesOrder(payload) {
  return request('/sales/orders', { method: 'POST', body: payload })
}

export function getSalesOrders(params) {
  return request(`/sales/orders${buildQuery(params)}`)
}

export function getSalesOrderDetail(id) {
  return request(`/sales/orders/${id}`)
}

export function cancelSalesOrder(id, operatorId) {
  return request(`/sales/orders/${id}/cancel${buildQuery({ operatorId })}`, { method: 'PATCH' })
}
