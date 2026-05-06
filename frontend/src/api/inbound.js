import { request } from '../utils/request'

export function getInbounds() {
  return request('/inbounds')
}

export function createInbound(payload) {
  return request('/inbounds', {
    method: 'POST',
    body: payload,
  })
}
