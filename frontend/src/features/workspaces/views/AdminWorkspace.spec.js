import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminWorkspace from './AdminWorkspace.vue'

const mocks = vi.hoisted(() => ({
  currentSessionUser: null,
  deactivateInstructor: vi.fn(),
  findUsers: vi.fn(),
  getUser: vi.fn(),
  inviteInstructors: vi.fn(),
  inviteStudents: vi.fn(),
  reactivateInstructor: vi.fn(),
  updateUser: vi.fn(),
  assignInstructors: vi.fn(),
  assignStudents: vi.fn(),
  configureActiveWeeks: vi.fn(),
  createRubric: vi.fn(),
  createSection: vi.fn(),
  createTeam: vi.fn(),
  getRubrics: vi.fn(),
  getSection: vi.fn(),
  getSections: vi.fn(),
  getTeam: vi.fn(),
  getTeams: vi.fn(),
  removeInstructorFromTeam: vi.fn(),
  removeStudentFromTeam: vi.fn(),
  updateSection: vi.fn(),
  updateTeam: vi.fn()
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
  deactivateInstructor: mocks.deactivateInstructor,
  findUsers: mocks.findUsers,
  getUser: mocks.getUser,
  inviteInstructors: mocks.inviteInstructors,
  inviteStudents: mocks.inviteStudents,
  reactivateInstructor: mocks.reactivateInstructor,
  updateUser: mocks.updateUser
}))

vi.mock('@/features/projects/services/projectService', () => ({
  assignInstructors: mocks.assignInstructors,
  assignStudents: mocks.assignStudents,
  configureActiveWeeks: mocks.configureActiveWeeks,
  createRubric: mocks.createRubric,
  createSection: mocks.createSection,
  createTeam: mocks.createTeam,
  getRubrics: mocks.getRubrics,
  getSection: mocks.getSection,
  getSections: mocks.getSections,
  getTeam: mocks.getTeam,
  getTeams: mocks.getTeams,
  removeInstructorFromTeam: mocks.removeInstructorFromTeam,
  removeStudentFromTeam: mocks.removeStudentFromTeam,
  updateSection: mocks.updateSection,
  updateTeam: mocks.updateTeam
}))

const adminUser = {
  id: 1,
  firstName: 'Casey',
  middleInitial: 'M',
  lastName: 'Admin'
}

const sectionRecord = {
  id: 12,
  name: 'Senior Design A',
  startDate: '2026-08-24',
  endDate: '2027-05-01',
  rubricId: 5,
  rubricName: 'Peer Eval Rubric v1',
  weeks: [],
  teams: []
}

const teamRecord = {
  id: 19,
  sectionName: 'Senior Design A',
  name: 'Pulse Team',
  description: 'Core product team',
  websiteUrl: 'https://pulse.example',
  students: [
    { id: 7, name: 'Jane Doe', email: 'jane@tcu.edu' }
  ],
  instructors: [
    { id: 4, name: 'Morgan Lee', email: 'morgan@tcu.edu' }
  ]
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
  return mount(AdminWorkspace)
}

describe('AdminWorkspace', () => {
  beforeEach(() => {
    mocks.currentSessionUser = { ...adminUser }
    Object.values(mocks).forEach((value) => {
      if (value?.mockReset) {
        value.mockReset()
      }
    })

    mocks.getRubrics.mockResolvedValue([
      { id: 5, name: 'Peer Eval Rubric v1', criteria: [] }
    ])
    mocks.getSections.mockResolvedValue([{ ...sectionRecord }])
    mocks.getTeams.mockResolvedValue([{ ...teamRecord }])
  })

  it('syncs section selection into team creation and student invitation workflows', async () => {
    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    const teamPanel = findPanel(wrapper, 'Team Operations')
    const invitationPanel = findPanel(wrapper, 'Invitations and Access')
    const focusPanel = findPanel(wrapper, 'Admin Focus')

    await findButton(sectionDirectoryPanel, 'Use Section').trigger('click')
    await flushPromises()

    expect(findField(teamPanel, 'Section').element.value).toBe('12')
    expect(findField(invitationPanel, 'Section').element.value).toBe('12')
    expect(focusPanel.text()).toContain('Senior Design A')
    expect(wrapper.text()).toContain('Section selected from directory.')
  })

  it('syncs team selection into the active roster-management context', async () => {
    const wrapper = mountWorkspace()
    await flushPromises()

    const teamPanel = findPanel(wrapper, 'Team Operations')
    const focusPanel = findPanel(wrapper, 'Admin Focus')

    await findButton(teamPanel, 'Use Team').trigger('click')
    await flushPromises()

    expect(findField(teamPanel, 'Team ID').element.value).toBe('19')
    expect(teamPanel.text()).toContain('jane@tcu.edu')
    expect(teamPanel.text()).toContain('morgan@tcu.edu')
    expect(focusPanel.text()).toContain('Pulse Team')
    expect(wrapper.text()).toContain('Team selected from directory.')
  })

  it('creates a rubric from the Rubrics and Sections panel (UC-1)', async () => {
    mocks.createRubric.mockResolvedValue({
      id: 6,
      name: 'Custom Rubric',
      criteria: []
    })

    const wrapper = mountWorkspace()
    await flushPromises()

    const rubricPanel = findPanel(wrapper, 'Rubrics and Sections')
    await findField(rubricPanel, 'Rubric Name').setValue('Custom Rubric')
    await rubricPanel.findAll('form')[0].trigger('submit.prevent')
    await flushPromises()

    expect(mocks.createRubric).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'Custom Rubric' })
    )
    expect(wrapper.text()).toContain('Rubric created.')
  })

  it('syncs instructor user selection into account editing and access actions', async () => {
    mocks.findUsers.mockResolvedValue([
      {
        id: 4,
        firstName: 'Morgan',
        middleInitial: 'J',
        lastName: 'Lee',
        email: 'morgan@tcu.edu',
        role: 'INSTRUCTOR',
        status: 'ACTIVE',
        sectionId: 12,
        sectionName: 'Senior Design A',
        assignedTeamId: null,
        assignedTeamName: null,
        supervisedTeams: ['Pulse Team']
      }
    ])

    const wrapper = mountWorkspace()
    await flushPromises()

    const userPanel = findPanel(wrapper, 'User Directory')
    const invitationPanel = findPanel(wrapper, 'Invitations and Access')
    const focusPanel = findPanel(wrapper, 'Admin Focus')
    const userForms = userPanel.findAll('form')

    await findField(userPanel, 'Role').setValue('INSTRUCTOR')
    await userForms[0].trigger('submit.prevent')
    await flushPromises()
    await findButton(userPanel, 'Use User').trigger('click')
    await flushPromises()

    expect(mocks.findUsers).toHaveBeenCalledWith('INSTRUCTOR', undefined)
    expect(findField(userPanel, 'User ID').element.value).toBe('4')
    expect(findField(invitationPanel, 'Instructor ID', 0).element.value).toBe('4')
    expect(focusPanel.text()).toContain('Morgan J Lee')
    expect(wrapper.text()).toContain('User selected from directory.')
  })
})
