import { apiRequest } from './client'

export type ProviderStatus = 'REGISTERED' | 'VERIFIED' | 'APPROVED' | 'ACTIVE' | 'REJECTED'
export type ContractStatus = 'ACTIVE' | 'EXPIRED'

export interface Provider {
  id: number
  name: string
  contactName: string
  contactEmail: string
  description?: string
  status: ProviderStatus
  createdAt: string
}

export interface PartnerContract {
  id: number
  providerId: number
  terms: string
  startDate: string
  endDate: string
  status: ContractStatus
}

export interface ApplyAsProviderRequest {
  name: string
  contactName: string
  contactEmail: string
  description?: string
}

export interface CreateOrUpdateContractRequest {
  terms: string
  startDate: string
  endDate: string
}

// 14.01.01.01 Register provider — public, no Vyoog account required.
export const partnersApi = {
  apply: (request: ApplyAsProviderRequest) =>
    apiRequest<Provider>('/partners/apply', { method: 'POST', body: JSON.stringify(request) }),
}

// ADMIN-only on the backend (MANAGE_PARTNERS) — see SecurityConfig.
export const adminPartnersApi = {
  listAll: () => apiRequest<Provider[]>('/admin/partners'),
  get: (id: number) => apiRequest<Provider>(`/admin/partners/${id}`),
  verify: (id: number) => apiRequest<Provider>(`/admin/partners/${id}/verify`, { method: 'POST' }),
  approve: (id: number) => apiRequest<Provider>(`/admin/partners/${id}/approve`, { method: 'POST' }),
  activate: (id: number) => apiRequest<Provider>(`/admin/partners/${id}/activate`, { method: 'POST' }),
  reject: (id: number) => apiRequest<Provider>(`/admin/partners/${id}/reject`, { method: 'POST' }),
  getContract: (id: number) => apiRequest<PartnerContract>(`/admin/partners/${id}/contract`),
  saveContract: (id: number, request: CreateOrUpdateContractRequest) =>
    apiRequest<PartnerContract>(`/admin/partners/${id}/contract`, { method: 'PUT', body: JSON.stringify(request) }),
}
