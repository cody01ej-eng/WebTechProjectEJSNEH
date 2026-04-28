import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import LoginView from './LoginView.vue'

const mocks = vi.hoisted(() => ({
  registerStudent: vi.fn(),
  registerInstructor: vi.fn(),
  signIn: vi.fn(),
  defaultRouteForRole: vi.fn((role) => `/${role.toLowerCase()}`),
  startupErrorMessage: ''
}))

vi.mock('@/features/auth/services/authService', () => ({
  registerStudent: mocks.registerStudent,
  registerInstructor: mocks.registerInstructor
}))

vi.mock('@/features/auth/stores/sessionStore', () => ({
  useSessionStore: () => ({
    signIn: mocks.signIn,
    defaultRouteForRole: mocks.defaultRouteForRole,
    startupError: {
      get value() {
        return mocks.startupErrorMessage
      }
    }
  })
}))

async function mountLoginView(query = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/login',
        component: LoginView
      }
    ]
  })

  await router.push({ path: '/login', query })
  await router.isReady()

  return mount(LoginView, {
    global: {
      plugins: [router]
    }
  })
}

describe('LoginView', () => {
  beforeEach(() => {
    mocks.registerStudent.mockReset()
    mocks.registerInstructor.mockReset()
    mocks.signIn.mockReset()
    mocks.defaultRouteForRole.mockReset()
    mocks.defaultRouteForRole.mockImplementation((role) => `/${role.toLowerCase()}`)
    mocks.startupErrorMessage = ''
  })

  it('prefills student invitation registration from the link query parameters', async () => {
    const wrapper = await mountLoginView({
      mode: 'student-register',
      token: 'student-token-123'
    })
    await nextTick()
    const studentForm = wrapper.findAll('form')[1]
    const studentTokenInput = studentForm.findAll('input')[0]

    expect(wrapper.text()).toContain('Student invitation link detected. Complete the student registration form below.')
    expect(studentTokenInput.element.value).toBe('student-token-123')
    expect(wrapper.find('.panel--accent').text()).toContain('Student Invitation Registration')
  })

  it('shows a validation error when instructor password confirmation does not match', async () => {
    const wrapper = await mountLoginView()
    const instructorForm = wrapper.findAll('form')[2]
    const instructorInputs = instructorForm.findAll('input')

    await instructorInputs[0].setValue('instructor-token-123')
    await instructorInputs[1].setValue('Casey')
    await instructorInputs[2].setValue('Q')
    await instructorInputs[3].setValue('Jones')
    await instructorInputs[4].setValue('Password123!')
    await instructorInputs[5].setValue('Mismatch123!')

    await instructorForm.trigger('submit.prevent')

    expect(mocks.registerInstructor).not.toHaveBeenCalled()
    expect(mocks.signIn).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Password confirmation must match the instructor password.')
  })

  it('shows the startup connection error when session bootstrap cannot reach the backend', async () => {
    mocks.startupErrorMessage = 'Unable to reach the Project Pulse backend right now.'
    const wrapper = await mountLoginView()

    expect(wrapper.text()).toContain('Unable to reach the Project Pulse backend right now.')
  })
})
