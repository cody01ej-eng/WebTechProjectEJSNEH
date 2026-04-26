import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import StudentWorkspace from './StudentWorkspace.vue'

const mocks = vi.hoisted(() => ({
  currentSessionUser: null,
  getUser: vi.fn(),
  getStudentWorkspaceContext: vi.fn(),
  updateUser: vi.fn(),
  createWarActivity: vi.fn(),
  deleteWarActivity: vi.fn(),
  getStudentSelfPeerReport: vi.fn(),
  getTeamWarReport: vi.fn(),
  submitPeerEvaluation: vi.fn(),
  updateWarActivity: vi.fn()
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
  getUser: mocks.getUser,
  updateUser: mocks.updateUser
}))

vi.mock('@/features/projects/services/projectService', () => ({
  createWarActivity: mocks.createWarActivity,
  deleteWarActivity: mocks.deleteWarActivity,
  getStudentSelfPeerReport: mocks.getStudentSelfPeerReport,
  getStudentWorkspaceContext: mocks.getStudentWorkspaceContext,
  getTeamWarReport: mocks.getTeamWarReport,
  submitPeerEvaluation: mocks.submitPeerEvaluation,
  updateWarActivity: mocks.updateWarActivity
}))

const signedInStudent = {
  id: 7,
  firstName: 'Jane',
  middleInitial: 'Q',
  lastName: 'Doe',
  email: 'jane.doe@tcu.edu',
  status: 'ACTIVE',
  sectionName: 'Senior Design A',
  assignedTeamId: 19,
  assignedTeamName: 'Pulse Team'
}

const studentWorkspaceContext = {
  studentId: 7,
  studentName: 'Jane Q Doe',
  email: 'jane.doe@tcu.edu',
  sectionId: 12,
  sectionName: 'Senior Design A',
  teamId: 19,
  teamName: 'Pulse Team',
  rubricId: 5,
  rubricName: 'Peer Eval Rubric v1',
  suggestedWarWeekStartDate: '2026-04-20',
  suggestedPeerEvaluationWeekStartDate: '2026-04-13',
  activeWeeks: ['2026-04-13', '2026-04-20', '2026-04-27'],
  teammates: [
    { id: 7, name: 'Jane Q Doe', self: true },
    { id: 11, name: 'Alex Roe', self: false }
  ],
  rubricCriteria: [
    {
      id: 1,
      name: 'Quality of work',
      description: 'Rate the quality of this teammate work.',
      maxScore: 10,
      displayOrder: 1
    },
    {
      id: 2,
      name: 'Initiative',
      description: 'Rate the teammate initiative.',
      maxScore: 10,
      displayOrder: 2
    }
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

function mountWorkspace() {
  return mount(StudentWorkspace)
}

describe('StudentWorkspace', () => {
  beforeEach(() => {
    mocks.currentSessionUser = { ...signedInStudent }
    mocks.getUser.mockReset()
    mocks.getStudentWorkspaceContext.mockReset()
    mocks.updateUser.mockReset()
    mocks.createWarActivity.mockReset()
    mocks.deleteWarActivity.mockReset()
    mocks.getStudentSelfPeerReport.mockReset()
    mocks.getTeamWarReport.mockReset()
    mocks.submitPeerEvaluation.mockReset()
    mocks.updateWarActivity.mockReset()
    mocks.getUser.mockResolvedValue({ ...signedInStudent })
    mocks.getStudentWorkspaceContext.mockResolvedValue({ ...studentWorkspaceContext })
  })

  it('hydrates the signed-in student into guided defaults and reporting forms on mount', async () => {
    const wrapper = mountWorkspace()
    await flushPromises()

    const profilePanel = findPanel(wrapper, 'My Profile')
    const teamWarPanel = findPanel(wrapper, 'Team WAR View')
    const peerPanel = findPanel(wrapper, 'Peer Evaluation Submission')
    const selfPeerPanel = findPanel(wrapper, 'My Peer Feedback')

    expect(mocks.getUser).toHaveBeenCalledWith(7)
    expect(mocks.getStudentWorkspaceContext).toHaveBeenCalledTimes(1)
    expect(findField(profilePanel, 'Email').element.value).toBe('jane.doe@tcu.edu')
    expect(findField(teamWarPanel, 'Week Start Date').element.value).toBe('2026-04-20')
    expect(findField(selfPeerPanel, 'Week Start Date').element.value).toBe('2026-04-13')
    expect(wrapper.text()).toContain('Senior Design A')
    expect(wrapper.text()).toContain('Pulse Team')
    expect(wrapper.text()).toContain('Peer Eval Rubric v1')
    expect(peerPanel.text()).toContain('Alex Roe')
    expect(peerPanel.text()).toContain('Quality of work')
    expect(wrapper.text()).toContain('Student workspace loaded.')
  })

  it('submits a guided peer evaluation using teammate comments and rubric scores', async () => {
    mocks.submitPeerEvaluation.mockResolvedValue({ submissionId: 41, accepted: true })

    const wrapper = mountWorkspace()
    await flushPromises()

    const peerPanel = findPanel(wrapper, 'Peer Evaluation Submission')
    const peerEntry = peerPanel.get('[data-testid="peer-entry-11"]')

    await findField(peerPanel, 'Previous Week Start Date').setValue('2026-04-13')
    await findField(peerEntry, 'Quality of work score').setValue('10')
    await findField(peerEntry, 'Initiative score').setValue('9')
    await findField(peerEntry, 'Public Comment').setValue('Always communicates blockers early.')
    await findField(peerEntry, 'Private Comment').setValue('Strong planning habits.')
    await peerPanel.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mocks.submitPeerEvaluation).toHaveBeenCalledWith({
      authorId: 7,
      weekStartDate: '2026-04-13',
      evaluations: [
        {
          evaluateeId: 11,
          publicComment: 'Always communicates blockers early.',
          privateComment: 'Strong planning habits.',
          scores: [
            { criterionId: 1, score: 10 },
            { criterionId: 2, score: 9 }
          ]
        }
      ]
    })
    expect(wrapper.text()).toContain('Peer evaluation submitted.')
  })

  it('renders the signed-in student self peer report after loading it', async () => {
    mocks.getStudentSelfPeerReport.mockResolvedValue({
      studentId: 7,
      studentName: 'Jane Doe',
      weeks: [
        {
          weekStartDate: '2026-04-20',
          averageGrade: 9.5,
          publicComments: ['Helpful teammate', 'Strong communicator']
        }
      ]
    })

    const wrapper = mountWorkspace()
    await flushPromises()

    const reportPanel = findPanel(wrapper, 'My Peer Feedback')
    await findField(reportPanel, 'Week Start Date').setValue('2026-04-20')
    await reportPanel.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mocks.getStudentSelfPeerReport).toHaveBeenCalledWith(7, '2026-04-20')
    expect(reportPanel.text()).toContain('Jane Doe')
    expect(reportPanel.text()).toContain('Average grade: 9.5')
    expect(reportPanel.text()).toContain('Helpful teammate | Strong communicator')
  })
})
