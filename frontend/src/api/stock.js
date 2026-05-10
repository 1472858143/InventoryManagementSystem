import { request } from '../utils/request'

export function getStocks() {
  return request('/stocks')
}

export function getStock(productId) {
  return request(`/stocks/${productId}`)
}

export function updateStockLimit(productId, payload) {
  return request(`/stocks/${productId}/limit`, { method: 'PUT', body: payload })
}

export function updateShelfStatus(productId, shelfStatus) {
  return request(`/stocks/${productId}/shelf-status`, {
    method: 'PATCH',
    body: { shelfStatus },
  })
}

export function batchUpdateShelfStatus(productIds, shelfStatus) {
  return request('/stocks/shelf-status/batch', {
    method: 'POST',
    body: { productIds, shelfStatus },
  })
}
