import { request } from '../utils/request'

export function getStocks() {
  return request('/stocks')
}

export function getStock(productId) {
  return request(`/stocks/${productId}`)
}

export function updateStockLimit(productId, payload) {
  return request(`/stocks/${productId}/limit`, {
    method: 'PUT',
    body: payload,
  })
}
