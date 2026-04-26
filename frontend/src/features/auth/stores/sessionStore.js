import { computed, reactive, readonly } from 'vue'
import { getCurrentSession, login, logoutSession } from '@/features/auth/services/authService'
import { resetCsrfTokenCache } from '@/shared/services/apiClient'

const state = reactive({
  user: null,
  initialized: false
})

function defaultRouteForRole(role) {
  if (role === 'ADMIN') {
    return '/admin'
  }
  if (role === 'INSTRUCTOR') {
    return '/instructor'
  }
  if (role === 'STUDENT') {
    return '/student'
  }
  return '/login'
}

async function hydrateSession(force = false) {
  if (state.initialized && !force) {
    return state.user
  }

  try {
    state.user = await getCurrentSession()
  } catch (error) {
    if (error.status !== 401) {
      throw error
    }
    state.user = null
  } finally {
    state.initialized = true
  }

  return state.user
}

async function signIn(credentials) {
  const user = await login(credentials)
  resetCsrfTokenCache()
  state.user = user
  state.initialized = true
  return user
}

async function signOut() {
  try {
    await logoutSession()
  } finally {
    resetCsrfTokenCache()
    state.user = null
    state.initialized = true
  }
}

export function useSessionStore() {
  return {
    state: readonly(state),
    user: computed(() => state.user),
    isAuthenticated: computed(() => Boolean(state.user)),
    hydrateSession,
    signIn,
    signOut,
    defaultRouteForRole
  }
}
