import { expect, test } from '@playwright/test'
import { mockCsrf, mockLogin, mockSession, successEnvelope } from './support/apiMocks'

const adminUser = {
  id: 1,
  firstName: 'Casey',
  middleInitial: 'M',
  lastName: 'Admin',
  email: 'admin@projectpulse.local',
  role: 'ADMIN',
  status: 'ACTIVE',
  sectionId: null,
  sectionName: null,
  assignedTeamId: null,
  assignedTeamName: null,
  supervisedTeams: []
}

const studentUser = {
  id: 7,
  firstName: 'Jane',
  middleInitial: 'Q',
  lastName: 'Doe',
  email: 'jane.doe@tcu.edu',
  role: 'STUDENT',
  status: 'ACTIVE',
  sectionId: 12,
  sectionName: 'Senior Design A',
  assignedTeamId: 19,
  assignedTeamName: 'Pulse Team',
  supervisedTeams: []
}

function loginPanel(page) {
  return page.locator('article.panel').filter({ has: page.getByRole('heading', { name: 'Sign In' }) })
}

function invitePanel(page) {
  return page.locator('article.panel').filter({ has: page.getByRole('heading', { name: 'Invitations and Access' }) })
}

function peerPanel(page) {
  return page.locator('article.panel').filter({ has: page.getByRole('heading', { name: 'Peer Evaluation Submission' }) })
}

test('admin can sign in and land on the admin dashboard', async ({ page }) => {
  await mockCsrf(page)
  const sessionController = await mockSession(page)
  await mockLogin(page, {
    [adminUser.email]: adminUser
  }, sessionController)

  await page.route('**/api/project/rubrics', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([
        { id: 5, name: 'Peer Eval Rubric v1', criteria: [] }
      ], 'Rubrics loaded'))
    })
  })
  await page.route('**/api/project/sections**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([], 'Sections loaded'))
    })
  })
  await page.route('**/api/project/teams**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([], 'Teams loaded'))
    })
  })

  await page.goto('/login')

  const panel = loginPanel(page)
  await panel.getByLabel('Email').fill(adminUser.email)
  await panel.getByLabel('Password').fill('Admin123!')
  await panel.getByRole('button', { name: 'Sign In' }).click()

  await expect(page).toHaveURL(/\/admin$/)
  await expect(page.getByRole('heading', { name: 'Program Snapshot' })).toBeVisible()
  await expect(page.getByText('Casey Admin')).toBeVisible()
  await expect(page.getByRole('link', { name: /Requirements/ })).toBeVisible()
})

test('admin can select a section and send student invitations from the guided flow', async ({ page }) => {
  await mockCsrf(page)
  await mockSession(page, adminUser)

  await page.route('**/api/project/rubrics', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([
        { id: 5, name: 'Peer Eval Rubric v1', criteria: [] }
      ], 'Rubrics loaded'))
    })
  })
  await page.route('**/api/project/sections**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([
        {
          id: 12,
          name: 'Senior Design A',
          startDate: '2026-08-24',
          endDate: '2027-05-01',
          rubricId: 5,
          rubricName: 'Peer Eval Rubric v1',
          weeks: [],
          teams: []
        }
      ], 'Sections loaded'))
    })
  })
  await page.route('**/api/project/teams**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([], 'Teams loaded'))
    })
  })

  let invitationPayload = null
  await page.route('**/api/auth/invitations/students', async (route) => {
    invitationPayload = route.request().postDataJSON()
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope([
        { email: 'student.one@tcu.edu', status: 'SENT' },
        { email: 'student.two@tcu.edu', status: 'SENT' }
      ], 'Student invitations processed'))
    })
  })

  await page.goto('/admin')

  await page.getByRole('button', { name: 'Use Section' }).click()

  const panel = invitePanel(page)
  await expect(panel.getByLabel('Section')).toHaveValue('12')
  await panel.getByLabel('Student Emails').fill('student.one@tcu.edu; student.two@tcu.edu')
  await panel.getByRole('button', { name: 'Invite Students' }).click()

  await expect(page.getByText('Student invitation emails sent.')).toBeVisible()
  expect(invitationPayload).toEqual({
    sectionId: 12,
    emails: ['student.one@tcu.edu', 'student.two@tcu.edu'],
    message: 'You have been invited to join Project Pulse as a student in your senior design section.'
  })
})

test('student can sign in and submit a guided peer evaluation journey', async ({ page }) => {
  await mockCsrf(page)
  const sessionController = await mockSession(page)
  await mockLogin(page, {
    [studentUser.email]: studentUser
  }, sessionController)

  await page.route(`**/api/users/${studentUser.id}`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope(studentUser, 'User loaded'))
    })
  })
  await page.route('**/api/project/workspace/student', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope({
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
      }, 'Student workspace loaded'))
    })
  })

  let peerEvaluationPayload = null
  await page.route('**/api/project/peer-evaluations', async (route) => {
    peerEvaluationPayload = route.request().postDataJSON()
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope({ submissionId: 41, accepted: true }, 'Peer evaluation submitted'))
    })
  })

  await page.goto('/login')

  const panel = loginPanel(page)
  await panel.getByLabel('Email').fill(studentUser.email)
  await panel.getByLabel('Password').fill('Password123!')
  await panel.getByRole('button', { name: 'Sign In' }).click()

  await expect(page).toHaveURL(/\/student$/)
  await expect(page.locator('p.eyebrow').filter({ hasText: 'Student Workspace' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Peer Evaluation Submission' })).toBeVisible()

  const submissionPanel = peerPanel(page)
  await submissionPanel.getByLabel('Previous Week Start Date').selectOption('2026-04-13')

  const peerEntry = submissionPanel.locator('[data-testid="peer-entry-11"]')
  await peerEntry.getByLabel('Quality of work score').fill('10')
  await peerEntry.getByLabel('Initiative score').fill('9')
  await peerEntry.getByLabel('Public Comment').fill('Always communicates blockers early.')
  await peerEntry.getByLabel('Private Comment').fill('Strong planning habits.')
  await submissionPanel.getByRole('button', { name: 'Submit Peer Evaluation' }).click()

  await expect(page.getByText('Peer evaluation submitted.')).toBeVisible()
  expect(peerEvaluationPayload).toEqual({
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
})
