import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/features/auth/services/authService', () => ({
  getCurrentSession: vi.fn(),
  login: vi.fn(),
  logoutSession: vi.fn()
}))

vi.mock('@/shared/services/apiClient', () => ({
  resetCsrfTokenCache: vi.fn()
}))

async function loadStoreModule() {
  vi.resetModules()

  const authService = await import('@/features/auth/services/authService')
  const apiClient = await import('@/shared/services/apiClient')
  const sessionStoreModule = await import('./sessionStore')

  return {
    authService,
    apiClient,
    sessionStore: sessionStoreModule.useSessionStore()
  }
}

describe('sessionStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('stores the signed-in user and resets the csrf cache', async () => {
    const { authService, apiClient, sessionStore } = await loadStoreModule()
    const user = { id: 7, role: 'STUDENT', email: 'jane.doe@tcu.edu' }

    authService.login.mockResolvedValue(user)

    await expect(sessionStore.signIn({
      email: 'jane.doe@tcu.edu',
      password: 'Password123!'
    })).resolves.toEqual(user)

    expect(authService.login).toHaveBeenCalledWith({
      email: 'jane.doe@tcu.edu',
      password: 'Password123!'
    })
    expect(apiClient.resetCsrfTokenCache).toHaveBeenCalledTimes(1)
    expect(sessionStore.user.value).toEqual(user)
    expect(sessionStore.isAuthenticated.value).toBe(true)
  })

  it('treats a 401 session hydrate as signed out instead of throwing', async () => {
    const { authService, sessionStore } = await loadStoreModule()
    const error = new Error('Authentication required')
    error.status = 401

    authService.getCurrentSession.mockRejectedValue(error)

    await expect(sessionStore.hydrateSession()).resolves.toBeNull()

    expect(authService.getCurrentSession).toHaveBeenCalledTimes(1)
    expect(sessionStore.user.value).toBeNull()
    expect(sessionStore.state.initialized).toBe(true)
    expect(sessionStore.isAuthenticated.value).toBe(false)
    expect(sessionStore.startupError.value).toBe('')
  })

  it('captures non-401 session hydrate failures and keeps the app on the signed-out path', async () => {
    const { authService, sessionStore } = await loadStoreModule()
    const error = new Error('Failed to fetch')

    authService.getCurrentSession.mockRejectedValue(error)

    await expect(sessionStore.hydrateSession()).resolves.toBeNull()

    expect(sessionStore.user.value).toBeNull()
    expect(sessionStore.state.initialized).toBe(true)
    expect(sessionStore.startupError.value).toContain('Unable to reach the Project Pulse backend right now.')
  })

})
