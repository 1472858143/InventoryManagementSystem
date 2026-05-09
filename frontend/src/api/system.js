import { request } from '../utils/request'

export function getSystemInfo() {
  return request('/system/info')
}
