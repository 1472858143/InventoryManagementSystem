import { request } from '../utils/request'

function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

export function createPurchaseReturn(payload) {
  return request('/purchase/returns', { method: 'POST', body: payload })
}

export function getPurchaseReturns(params) {
  return request(`/purchase/returns${buildQuery(params)}`)
}

export function getPurchaseReturnDetail(id) {
  return request(`/purchase/returns/${id}`)
}

export function cancelPurchaseReturn(id, operatorId) {
  return request(`/purchase/returns/${id}/cancel${buildQuery({ operatorId })}`, { method: 'PATCH' })
}
