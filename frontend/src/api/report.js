import { request } from '../utils/request'

export function getStockOverview() { return request('/reports/stock-overview') }
export function getInboundTrend() { return request('/reports/inbound-trend') }
export function getOutboundTrend() { return request('/reports/outbound-trend') }
export function getShelfStatusDistribution() { return request('/reports/shelf-status-distribution') }
export function getCategoryDistribution() { return request('/reports/category-distribution') }
export function getRestockSuggestions() { return request('/reports/restock-suggestions') }
export function getSlowMoving(days = 30) { return request(`/reports/slow-moving?days=${days}`) }
export function getSalesHotspot(days = 30, limit = 10) { return request(`/reports/sales-hotspot?days=${days}&limit=${limit}`) }
