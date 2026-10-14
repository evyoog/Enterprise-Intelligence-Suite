import { apiRequest } from './client'
import type { IncidentPhase, IncidentSeverity } from './serviceStatusApi'

// C101 (IC-2 A): a problem links several incidents to one root cause, with corrective actions. MANAGE_SERVICE_STATUS.
export type ProblemStatus = 'OPEN' | 'ROOT_CAUSE_IDENTIFIED' | 'RESOLVED' | 'CLOSED'

/** In lifecycle order. */
export const PROBLEM_STATUSES: ProblemStatus[] = ['OPEN', 'ROOT_CAUSE_IDENTIFIED', 'RESOLVED', 'CLOSED']

export interface Problem {
  id: number
  title: string
  description?: string | null
  status: ProblemStatus
  rootCause?: string | null
  incidentCount: number
  actionCount: number
  openActionCount: number
  createdAt: string
  resolvedAt?: string | null
  closedAt?: string | null
}

export interface ProblemIncident {
  id: number
  title: string
  productName: string
  phase: IncidentPhase
  severity: IncidentSeverity
  startedAt: string
}

export interface ProblemAction {
  id: number
  description: string
  owner?: string | null
  /** YYYY-MM-DD */
  dueOn?: string | null
  done: boolean
  doneAt?: string | null
}

export interface ProblemDetail {
  problem: Problem
  incidents: ProblemIncident[]
  actions: ProblemAction[]
}

export interface ProblemCreateInput {
  title: string
  description?: string
  incidentIds?: number[]
}

/** Fields left out (or null) keep their value. */
export interface ProblemUpdateInput {
  title?: string
  description?: string
  rootCause?: string
  status?: ProblemStatus
}

export interface ProblemActionInput {
  description?: string
  owner?: string
  dueOn?: string
  done?: boolean
}

const base = '/admin/service-status/problems'
const json = (method: string, body?: unknown) => ({ method, body: body === undefined ? undefined : JSON.stringify(body) })

export const problemsApi = {
  list: () => apiRequest<Problem[]>(base),
  get: (id: number) => apiRequest<ProblemDetail>(`${base}/${id}`),
  create: (input: ProblemCreateInput) => apiRequest<ProblemDetail>(base, json('POST', input)),
  update: (id: number, input: ProblemUpdateInput) => apiRequest<ProblemDetail>(`${base}/${id}`, json('PUT', input)),
  linkIncident: (id: number, incidentId: number) => apiRequest<ProblemDetail>(`${base}/${id}/incidents/${incidentId}`, json('POST')),
  unlinkIncident: (id: number, incidentId: number) => apiRequest<ProblemDetail>(`${base}/${id}/incidents/${incidentId}`, json('DELETE')),
  addAction: (id: number, input: ProblemActionInput & { description: string }) => apiRequest<ProblemDetail>(`${base}/${id}/actions`, json('POST', input)),
  updateAction: (id: number, actionId: number, input: ProblemActionInput) => apiRequest<ProblemDetail>(`${base}/${id}/actions/${actionId}`, json('PUT', input)),
  deleteAction: (id: number, actionId: number) => apiRequest<ProblemDetail>(`${base}/${id}/actions/${actionId}`, json('DELETE')),
}
