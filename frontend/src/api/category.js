import { request } from '../utils/request'

export const getCategories = () => request('/categories')
export const getEnabledCategories = () => request('/categories/enabled')
export const createCategory = (payload) => request('/categories', { method: 'POST', body: payload })
export const updateCategoryStatus = (id, payload) => request(`/categories/${id}/status`, { method: 'PUT', body: payload })
