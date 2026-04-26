import { apiGet, apiPost, apiPut } from '@/shared/services/apiClient'

export function login(payload) {
  return apiPost('/auth/login', payload)
}

export function getCurrentSession() {
  return apiGet('/auth/me')
}

export function logoutSession() {
  return apiPost('/auth/logout', {})
}

export function inviteStudents(payload) {
  return apiPost('/auth/invitations/students', payload)
}

export function inviteInstructors(payload) {
  return apiPost('/auth/invitations/instructors', payload)
}

export function registerStudent(payload) {
  return apiPost('/auth/registrations/students', payload)
}

export function registerInstructor(payload) {
  return apiPost('/auth/registrations/instructors', payload)
}

export function findUsers(role, name) {
  return apiGet('/users', { role, name })
}

export function getUser(userId) {
  return apiGet(`/users/${userId}`)
}

export function updateUser(userId, payload) {
  return apiPut(`/users/${userId}`, payload)
}

export function deactivateInstructor(userId, payload) {
  return apiPost(`/users/${userId}/deactivate`, payload)
}

export function reactivateInstructor(userId) {
  return apiPost(`/users/${userId}/reactivate`)
}
