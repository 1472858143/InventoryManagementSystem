import { request } from '../utils/request'

export function getProducts() {
  return request('/products')
}

export function createProduct(payload) {
  return request('/products', {
    method: 'POST',
    body: payload,
  })
}

export function updateProductStatus(payload) {
  return request('/products/status', {
    method: 'PUT',
    body: payload,
  })
}
