import { request } from '../utils/request'

function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

export function createSalesReturn(payload) {
  return request('/sales/returns', { method: 'POST', body: payload })
}

export function getSalesReturns(params) {
  return request(`/sales/returns${buildQuery(params)}`)
}

export function getSalesReturnDetail(id) {
  return request(`/sales/returns/${id}`)
}

export function cancelSalesReturn(id, operatorId) {
  return request(`/sales/returns/${id}/cancel${buildQuery({ operatorId })}`, { method: 'PATCH' })
}
