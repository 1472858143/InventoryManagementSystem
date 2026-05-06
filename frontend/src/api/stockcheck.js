import { request } from '../utils/request'

export function getStockchecks() {
  return request('/stockchecks')
}

export function createStockcheck(payload) {
  return request('/stockchecks', {
    method: 'POST',
    body: payload,
  })
}
