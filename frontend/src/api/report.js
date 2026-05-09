import { request } from '../utils/request'

export function getStockOverview() {
  return request('/reports/stock-overview')
}

export function getInboundTrend() {
  return request('/reports/inbound-trend')
}

export function getOutboundTrend() {
  return request('/reports/outbound-trend')
}
