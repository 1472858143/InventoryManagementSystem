import { request } from '../utils/request'

export function getOutbounds() {
  return request('/outbounds')
}

export function createOutbound(payload) {
  return request('/outbounds', {
    method: 'POST',
    body: payload,
  })
}
