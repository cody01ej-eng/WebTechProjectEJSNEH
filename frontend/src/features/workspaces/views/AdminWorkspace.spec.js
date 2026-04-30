import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminWorkspace from './AdminWorkspace.vue'

const mocks = vi.hoisted(() => ({
  currentSessionUser: null,
  deleteStudent: vi.fn(),
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
  deleteTeam: vi.fn(),
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
  deleteStudent: mocks.deleteStudent,
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
  deleteTeam: mocks.deleteTeam,
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

const clipboardWriteText = vi.fn()
const confirmMock = vi.fn()

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
    clipboardWriteText.mockReset()
    Object.defineProperty(globalThis.navigator, 'clipboard', {
      configurable: true,
      value: {
        writeText: clipboardWriteText
      }
    })
    confirmMock.mockReset()
    confirmMock.mockReturnValue(true)
    Object.defineProperty(globalThis.window, 'confirm', {
      configurable: true,
      value: confirmMock
    })
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

  it('finds sections using a name filter from the Section Directory panel (UC-2)', async () => {
    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    await findField(sectionDirectoryPanel, 'Section Name Filter').setValue('design')
    await sectionDirectoryPanel.findAll('form')[0].trigger('submit.prevent')
    await flushPromises()

    expect(mocks.getSections).toHaveBeenCalledWith('design')
    expect(wrapper.text()).toContain('Loaded 1 section record(s).')
  })

  it('loads a section detail from the directory list (UC-3)', async () => {
    mocks.getSection.mockResolvedValue({ ...sectionRecord })

    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    await findButton(sectionDirectoryPanel, 'Load Section Detail').trigger('click')
    await flushPromises()

    expect(mocks.getSection).toHaveBeenCalledWith(12)
    expect(sectionDirectoryPanel.text()).toContain('Loaded Section')
    expect(wrapper.text()).toContain('Section loaded.')
  })

  it('creates a section from the Rubrics and Sections panel (UC-4)', async () => {
    mocks.createSection.mockResolvedValue({ ...sectionRecord })

    const wrapper = mountWorkspace()
    await flushPromises()

    const rubricAndSectionPanel = findPanel(wrapper, 'Rubrics and Sections')
    await findField(rubricAndSectionPanel, 'Section Name').setValue('2026-2027')
    await findField(rubricAndSectionPanel, 'Start Date').setValue('2026-08-24')
    await findField(rubricAndSectionPanel, 'End Date').setValue('2027-05-01')
    await findField(rubricAndSectionPanel, 'Rubric').setValue(5)
    await rubricAndSectionPanel.findAll('form')[1].trigger('submit.prevent')
    await flushPromises()

    expect(mocks.createSection).toHaveBeenCalledWith(
      expect.objectContaining({ name: '2026-2027' })
    )
    expect(wrapper.text()).toContain('Section created.')
  })

  it('updates a section name via the edit section form (UC-5)', async () => {
    mocks.updateSection.mockResolvedValue({ ...sectionRecord, name: 'Renamed Section' })

    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    await findButton(sectionDirectoryPanel, 'Use Section').trigger('click')
    await flushPromises()

    await findField(sectionDirectoryPanel, 'Section Name').setValue('Renamed Section')
    await sectionDirectoryPanel.findAll('form')[1].trigger('submit.prevent')
    await flushPromises()

    expect(mocks.updateSection).toHaveBeenCalledWith(
      12,
      expect.objectContaining({ name: 'Renamed Section' })
    )
    expect(wrapper.text()).toContain('Section updated.')
  })

  it('configures active weeks for the selected section (UC-6)', async () => {
    mocks.configureActiveWeeks.mockResolvedValue({ ...sectionRecord })

    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    await findButton(sectionDirectoryPanel, 'Use Section').trigger('click')
    await flushPromises()

    await findField(sectionDirectoryPanel, 'Inactive Week Start Dates').setValue('2026-08-31')
    await sectionDirectoryPanel.findAll('form')[2].trigger('submit.prevent')
    await flushPromises()

    expect(mocks.configureActiveWeeks).toHaveBeenCalledWith(
      12,
      expect.objectContaining({ inactiveWeekStartDates: ['2026-08-31'] })
    )
    expect(wrapper.text()).toContain('Active weeks configured.')
  })

  it('invites students to a section and shows all-sent success message (UC-11)', async () => {
    mocks.inviteStudents.mockResolvedValue([
      { id: 100, email: 'student.one@tcu.edu', status: 'PENDING' },
      { id: 101, email: 'student.two@tcu.edu', status: 'PENDING' }
    ])

    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    const invitationPanel = findPanel(wrapper, 'Invitations and Access')

    await findButton(sectionDirectoryPanel, 'Use Section').trigger('click')
    await flushPromises()

    await findField(invitationPanel, 'Student Emails').setValue('student.one@tcu.edu; student.two@tcu.edu')
    await invitationPanel.findAll('form')[0].trigger('submit.prevent')
    await flushPromises()

    expect(mocks.inviteStudents).toHaveBeenCalledWith(
      expect.objectContaining({
        sectionId: 12,
        emails: ['student.one@tcu.edu', 'student.two@tcu.edu']
      })
    )
    expect(wrapper.text()).toContain('Student invitation emails sent.')
  })

  it('shows partial failure summary when some student invitation emails fail to deliver (UC-11)', async () => {
    mocks.inviteStudents.mockResolvedValue([
      { id: 100, email: 'student.one@tcu.edu', status: 'PENDING' },
      { id: 101, email: 'student.two@tcu.edu', status: 'FAILED' }
    ])

    const wrapper = mountWorkspace()
    await flushPromises()

    const sectionDirectoryPanel = findPanel(wrapper, 'Section Directory')
    const invitationPanel = findPanel(wrapper, 'Invitations and Access')

    await findButton(sectionDirectoryPanel, 'Use Section').trigger('click')
    await flushPromises()

    await findField(invitationPanel, 'Student Emails').setValue('student.one@tcu.edu; student.two@tcu.edu')
    await invitationPanel.findAll('form')[0].trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('1 email delivery failure')
  })

  it('surfaces manual registration links when invitation email delivery fails', async () => {
    mocks.inviteInstructors.mockResolvedValue([
      {
        id: 31,
        token: 'invite-token-31',
        email: 'b.wei@uni.edu',
        type: 'INSTRUCTOR',
        status: 'FAILED',
        sectionName: null,
        expiresAt: '2026-05-29T21:04:51.511485173',
        message: 'You have been invited to join Project Pulse as an instructor and complete your account setup.',
        registrationUrl: 'https://pulse.example/login?mode=instructor-register&token=invite-token-31'
      }
    ])

    const wrapper = mountWorkspace()
    await flushPromises()

    const invitationPanel = findPanel(wrapper, 'Invitations and Access')
    const forms = invitationPanel.findAll('form')
    await findField(invitationPanel, 'Instructor Emails').setValue('b.wei@uni.edu')
    await forms[1].trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('Copy the manual registration link or token below')
    expect(wrapper.text()).toContain('Email delivery failed, but this invite can still be completed manually.')
    expect(wrapper.text()).toContain('invite-token-31')
    expect(wrapper.text()).toContain('https://pulse.example/login?mode=instructor-register&token=invite-token-31')

    await findButton(invitationPanel, 'Copy Registration Link').trigger('click')

    expect(clipboardWriteText).toHaveBeenCalledWith(
      'https://pulse.example/login?mode=instructor-register&token=invite-token-31'
    )
    expect(wrapper.text()).toContain('Registration link copied.')
  })

  it('deletes the selected team after admin confirmation', async () => {
    mocks.deleteTeam.mockResolvedValue({
      teamId: 19,
      teamName: 'Pulse Team',
      removedStudentAssignments: 1,
      removedInstructorAssignments: 1,
      deletedWarActivities: 0,
      deletedPeerEvaluationSubmissions: 0
    })

    const wrapper = mountWorkspace()
    await flushPromises()

    const teamPanel = findPanel(wrapper, 'Team Operations')
    const forms = teamPanel.findAll('form')
    await findButton(teamPanel, 'Use Team').trigger('click')
    await flushPromises()
    await forms[6].trigger('submit.prevent')
    await flushPromises()

    expect(confirmMock).toHaveBeenCalled()
    expect(mocks.deleteTeam).toHaveBeenCalledWith(19)
    expect(findField(teamPanel, 'Team ID').element.value).toBe('')
    expect(wrapper.text()).toContain('Team deleted permanently.')
  })

  it('deletes the selected student after admin confirmation', async () => {
    mocks.findUsers.mockResolvedValue([
      {
        id: 7,
        firstName: 'Jane',
        middleInitial: null,
        lastName: 'Doe',
        email: 'jane@tcu.edu',
        role: 'STUDENT',
        status: 'ACTIVE',
        sectionId: 12,
        sectionName: 'Senior Design A',
        assignedTeamId: 19,
        assignedTeamName: 'Pulse Team',
        supervisedTeams: []
      }
    ])
    mocks.deleteStudent.mockResolvedValue({
      studentId: 7,
      studentName: 'Jane Doe',
      deletedWarActivities: 2,
      deletedPeerEvaluationSubmissions: 1,
      deletedPeerEvaluationItems: 3
    })

    const wrapper = mountWorkspace()
    await flushPromises()

    const userPanel = findPanel(wrapper, 'User Directory')
    const userForms = userPanel.findAll('form')
    await findField(userPanel, 'Role').setValue('STUDENT')
    await userForms[0].trigger('submit.prevent')
    await flushPromises()
    await findButton(userPanel, 'Use User').trigger('click')
    await flushPromises()
    await userForms[2].trigger('submit.prevent')
    await flushPromises()

    expect(confirmMock).toHaveBeenCalled()
    expect(mocks.deleteStudent).toHaveBeenCalledWith(7)
    expect(findField(userPanel, 'User ID').element.value).toBe('')
    expect(wrapper.text()).toContain('Student deleted permanently.')
  })
})
