import { request } from '../utils/request'

function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

export function createPurchaseOrder(payload) {
  return request('/purchase/orders', { method: 'POST', body: payload })
}

export function getPurchaseOrders(params) {
  return request(`/purchase/orders${buildQuery(params)}`)
}

export function getPurchaseOrderDetail(id) {
  return request(`/purchase/orders/${id}`)
}

export function cancelPurchaseOrder(id, operatorId) {
  return request(`/purchase/orders/${id}/cancel${buildQuery({ operatorId })}`, { method: 'PATCH' })
}
