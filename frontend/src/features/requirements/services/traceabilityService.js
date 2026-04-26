import { apiGet, apiPost } from '@/shared/services/apiClient'

export function getRequirementReferences() {
  return apiGet('/requirements/references')
}

export function createRequirementReference(payload) {
  return apiPost('/requirements/references', payload)
}

export function getTraceabilityLinks() {
  return apiGet('/requirements/trace-links')
}

export function createTraceabilityLink(payload) {
  return apiPost('/requirements/trace-links', payload)
}
