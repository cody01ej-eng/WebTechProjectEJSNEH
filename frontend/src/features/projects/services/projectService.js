import { apiDelete, apiGet, apiPost, apiPut } from '@/shared/services/apiClient'

export function getRubrics() {
  return apiGet('/project/rubrics')
}

export function createRubric(payload) {
  return apiPost('/project/rubrics', payload)
}

export function getSections(name) {
  return apiGet('/project/sections', { name })
}

export function getSection(sectionId) {
  return apiGet(`/project/sections/${sectionId}`)
}

export function createSection(payload) {
  return apiPost('/project/sections', payload)
}

export function updateSection(sectionId, payload) {
  return apiPut(`/project/sections/${sectionId}`, payload)
}

export function configureActiveWeeks(sectionId, payload) {
  return apiPost(`/project/sections/${sectionId}/active-weeks`, payload)
}

export function getTeams(sectionId) {
  return apiGet('/project/teams', { sectionId })
}

export function getTeam(teamId) {
  return apiGet(`/project/teams/${teamId}`)
}

export function createTeam(payload) {
  return apiPost('/project/teams', payload)
}

export function updateTeam(teamId, payload) {
  return apiPut(`/project/teams/${teamId}`, payload)
}

export function assignStudents(teamId, payload) {
  return apiPost(`/project/teams/${teamId}/students`, payload)
}

export function removeStudentFromTeam(teamId, studentId) {
  return apiDelete(`/project/teams/${teamId}/students/${studentId}`)
}

export function assignInstructors(teamId, payload) {
  return apiPost(`/project/teams/${teamId}/instructors`, payload)
}

export function removeInstructorFromTeam(teamId, instructorId) {
  return apiDelete(`/project/teams/${teamId}/instructors/${instructorId}`)
}

export function createWarActivity(payload) {
  return apiPost('/project/war/activities', payload)
}

export function updateWarActivity(activityId, payload) {
  return apiPut(`/project/war/activities/${activityId}`, payload)
}

export function deleteWarActivity(activityId) {
  return apiDelete(`/project/war/activities/${activityId}`)
}

export function submitPeerEvaluation(payload) {
  return apiPost('/project/peer-evaluations', payload)
}

export function getStudentWorkspaceContext() {
  return apiGet('/project/workspace/student')
}

export function getTeamWarReport(teamId, weekStartDate) {
  return apiGet(`/project/reports/teams/${teamId}/war`, { weekStartDate })
}

export function getSectionPeerReport(sectionId, weekStartDate) {
  return apiGet(`/project/reports/sections/${sectionId}/peer-evaluations`, { weekStartDate })
}

export function getStudentWarReport(studentId, fromWeekStartDate, toWeekStartDate) {
  return apiGet(`/project/reports/students/${studentId}/war`, { fromWeekStartDate, toWeekStartDate })
}

export function getStudentSelfPeerReport(studentId, weekStartDate) {
  return apiGet(`/project/reports/students/${studentId}/peer-evaluations/self`, { weekStartDate })
}

export function getInstructorStudentPeerReport(studentId, fromWeekStartDate, toWeekStartDate) {
  return apiGet(`/project/reports/students/${studentId}/peer-evaluations/instructor`, {
    fromWeekStartDate,
    toWeekStartDate
  })
}
