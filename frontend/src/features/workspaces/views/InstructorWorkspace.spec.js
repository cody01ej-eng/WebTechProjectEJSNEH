import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import InstructorWorkspace from './InstructorWorkspace.vue'

const mocks = vi.hoisted(() => ({
  currentSessionUser: null,
  findUsers: vi.fn(),
  getUser: vi.fn(),
  getInstructorStudentPeerReport: vi.fn(),
  getSectionPeerReport: vi.fn(),
  getStudentWarReport: vi.fn(),
  getTeam: vi.fn(),
  getTeams: vi.fn(),
  getTeamWarReport: vi.fn()
}))

vi.mock('@/features/auth/stores/sessionStore', () => ({
  useSessionStore: () => ({
    user: {
      get value() {
        return mocks.currentSessionUser
      }
    }
  })
}))

vi.mock('@/features/auth/services/authService', () => ({
  findUsers: mocks.findUsers,
  getUser: mocks.getUser
}))

vi.mock('@/features/projects/services/projectService', () => ({
  getInstructorStudentPeerReport: mocks.getInstructorStudentPeerReport,
  getSectionPeerReport: mocks.getSectionPeerReport,
  getStudentWarReport: mocks.getStudentWarReport,
  getTeam: mocks.getTeam,
  getTeams: mocks.getTeams,
  getTeamWarReport: mocks.getTeamWarReport
}))

const sessionInstructor = {
  id: 3,
  firstName: 'Morgan',
  middleInitial: 'J',
  lastName: 'Lee',
  role: 'INSTRUCTOR',
  sectionId: 12,
  sectionName: 'Senior Design A',
  supervisedTeams: ['Pulse Team', 'Signal Team']
}

function findPanel(wrapper, title) {
  const panel = wrapper.findAll('article.panel').find((item) => item.find('h3').exists() && item.find('h3').text() === title)
  expect(panel, `Expected panel "${title}" to exist.`).toBeTruthy()
  return panel
}

function findField(container, labelText, index = 0) {
  const labels = container.findAll('label.field').filter((label) => label.find('span').exists() && label.find('span').text() === labelText)
  expect(labels[index], `Expected field "${labelText}" at index ${index} to exist.`).toBeTruthy()
  return labels[index].find('input, textarea, select')
}

function findButton(container, text) {
  const button = container.findAll('button').find((item) => item.text() === text)
  expect(button, `Expected button "${text}" to exist.`).toBeTruthy()
  return button
}

function mountWorkspace() {
  return mount(InstructorWorkspace)
}

describe('InstructorWorkspace', () => {
  beforeEach(() => {
    mocks.currentSessionUser = { ...sessionInstructor }
    mocks.findUsers.mockReset()
    mocks.getUser.mockReset()
    mocks.getInstructorStudentPeerReport.mockReset()
    mocks.getSectionPeerReport.mockReset()
    mocks.getStudentWarReport.mockReset()
    mocks.getTeam.mockReset()
    mocks.getTeams.mockReset()
    mocks.getTeamWarReport.mockReset()
  })

  it('syncs the guided report actions after selecting a student from the directory', async () => {
    mocks.findUsers.mockResolvedValue([
      {
        id: 7,
        firstName: 'Jane',
        middleInitial: 'Q',
        lastName: 'Doe',
        sectionId: 12,
        sectionName: 'Senior Design A',
        assignedTeamId: 19,
        assignedTeamName: 'Pulse Team'
      }
    ])

    const wrapper = mountWorkspace()
    const studentDirectoryPanel = findPanel(wrapper, 'Student Directory')
    const guidedActionsPanel = findPanel(wrapper, 'Guided Report Actions')

    await findField(studentDirectoryPanel, 'Name Filter').setValue('Doe')
    await studentDirectoryPanel.find('form').trigger('submit.prevent')
    await flushPromises()

    await findButton(studentDirectoryPanel, 'Use Student').trigger('click')
    await flushPromises()

    expect(mocks.findUsers).toHaveBeenCalledWith('STUDENT', 'Doe')
    expect(findField(guidedActionsPanel, 'Section ID').element.value).toBe('12')
    expect(findField(guidedActionsPanel, 'Team ID').element.value).toBe('19')
    expect(findField(guidedActionsPanel, 'Student ID').element.value).toBe('7')
    expect(wrapper.text()).toContain('Student selected from directory.')
  })

  it('renders section and team report summaries after guided snapshot loads', async () => {
    mocks.getSectionPeerReport.mockResolvedValue({
      sectionId: 12,
      sectionName: 'Senior Design A',
      weekStartDate: '2026-04-20',
      missingEvaluators: ['Jamie Smith'],
      students: [
        { studentId: 7, studentName: 'Jane Doe', averageGrade: 9.5, comments: [{ evaluatorName: 'A' }] },
        { studentId: 8, studentName: 'Alex Roe', averageGrade: 8.75, comments: [] }
      ]
    })
    mocks.getTeamWarReport.mockResolvedValue({
      teamId: 19,
      teamName: 'Pulse Team',
      weekStartDate: '2026-04-20',
      missingStudents: ['Alex Roe'],
      students: [
        {
          studentId: 7,
          studentName: 'Jane Doe',
          activities: [{ id: 1 }, { id: 2 }]
        }
      ]
    })

    const wrapper = mountWorkspace()
    const reportingWindowPanel = findPanel(wrapper, 'Reporting Window')
    const guidedActionsPanel = findPanel(wrapper, 'Guided Report Actions')
    const snapshotPanel = findPanel(wrapper, 'Section and Team Snapshots')

    await findField(reportingWindowPanel, 'Snapshot Week').setValue('2026-04-20')
    await findField(guidedActionsPanel, 'Section ID').setValue('12')
    await findField(guidedActionsPanel, 'Team ID').setValue('19')
    await findButton(guidedActionsPanel, 'Load Section Peer Snapshot').trigger('click')
    await flushPromises()
    await findButton(guidedActionsPanel, 'Load Team WAR Snapshot').trigger('click')
    await flushPromises()

    expect(mocks.getSectionPeerReport).toHaveBeenCalledWith(12, '2026-04-20')
    expect(mocks.getTeamWarReport).toHaveBeenCalledWith(19, '2026-04-20')
    expect(guidedActionsPanel.text()).toContain('2')
    expect(snapshotPanel.text()).toContain('Senior Design A')
    expect(snapshotPanel.text()).toContain('Missing Evaluators')
    expect(snapshotPanel.text()).toContain('Jamie Smith')
    expect(snapshotPanel.text()).toContain('Pulse Team')
    expect(snapshotPanel.text()).toContain('Missing Students')
    expect(snapshotPanel.text()).toContain('Alex Roe')
    expect(snapshotPanel.text()).toContain('2 activities')
  })

  it('renders student deep-dive peer and WAR reports over the shared date window', async () => {
    mocks.getInstructorStudentPeerReport.mockResolvedValue({
      studentId: 7,
      studentName: 'Jane Doe',
      weeks: [
        {
          weekStartDate: '2026-04-13',
          averageGrade: 9.25,
          publicComments: ['Strong sprint contributor']
        }
      ]
    })
    mocks.getStudentWarReport.mockResolvedValue({
      studentId: 7,
      studentName: 'Jane Doe',
      weeks: [
        {
          weekStartDate: '2026-04-13',
          activities: [{ id: 1 }, { id: 2 }, { id: 3 }]
        }
      ]
    })

    const wrapper = mountWorkspace()
    const reportingWindowPanel = findPanel(wrapper, 'Reporting Window')
    const guidedActionsPanel = findPanel(wrapper, 'Guided Report Actions')
    const trendPanel = findPanel(wrapper, 'Student Trend Reports')

    await findField(reportingWindowPanel, 'From Week').setValue('2026-04-13')
    await findField(reportingWindowPanel, 'To Week').setValue('2026-04-20')
    await findField(guidedActionsPanel, 'Student ID').setValue('7')
    await findButton(guidedActionsPanel, 'Load Peer Trend').trigger('click')
    await flushPromises()
    await findButton(guidedActionsPanel, 'Load WAR Trend').trigger('click')
    await flushPromises()

    expect(mocks.getInstructorStudentPeerReport).toHaveBeenCalledWith(7, '2026-04-13', '2026-04-20')
    expect(mocks.getStudentWarReport).toHaveBeenCalledWith(7, '2026-04-13', '2026-04-20')
    expect(trendPanel.text()).toContain('Jane Doe')
    expect(trendPanel.text()).toContain('Average grade: 9.25')
    expect(trendPanel.text()).toContain('Strong sprint contributor')
    expect(trendPanel.text()).toContain('3 activities')
  })
})
