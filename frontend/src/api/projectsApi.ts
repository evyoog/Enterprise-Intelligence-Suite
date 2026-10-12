import { apiRequest } from './client'
/** 05.04.02 Projects (REQ-TEN-009, C88): a group of members plus products. */
export interface ProjectMember {
  memberId: number
  customerId: number
  name: string
  email?: string
  orgRole: string
}

export interface ProjectProduct {
  productId: number
  productName: string
  productRole: string
}

export interface Project {
  id: number
  name: string
  description?: string | null
  createdAt: string
  members: ProjectMember[]
  products: ProjectProduct[]
}

// Authenticated and always scoped to the caller's own organization by the backend.
export const projectsApi = {
  list: () => apiRequest<Project[]>('/organization/me/projects'),
  create: (name: string, description?: string) =>
    apiRequest<Project>('/organization/me/projects', { method: 'POST', body: JSON.stringify({ name, description }) }),
  update: (id: number, name: string, description?: string) =>
    apiRequest<Project>(`/organization/me/projects/${id}`, { method: 'PUT', body: JSON.stringify({ name, description }) }),
  remove: (id: number) => apiRequest<void>(`/organization/me/projects/${id}`, { method: 'DELETE' }),
  addMember: (id: number, memberId: number) =>
    apiRequest<Project>(`/organization/me/projects/${id}/members/${memberId}`, { method: 'POST' }),
  removeMember: (id: number, memberId: number) =>
    apiRequest<Project>(`/organization/me/projects/${id}/members/${memberId}`, { method: 'DELETE' }),
  setProduct: (id: number, productId: number, productRole: string) =>
    apiRequest<Project>(`/organization/me/projects/${id}/products`, { method: 'PUT', body: JSON.stringify({ productId, productRole }) }),
  removeProduct: (id: number, productId: number) =>
    apiRequest<Project>(`/organization/me/projects/${id}/products/${productId}`, { method: 'DELETE' }),
}
