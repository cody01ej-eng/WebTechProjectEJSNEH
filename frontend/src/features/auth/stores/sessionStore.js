import { computed, reactive, readonly } from 'vue'
import { getCurrentSession, login, logoutSession } from '@/features/auth/services/authService'
import { resetCsrfTokenCache } from '@/shared/services/apiClient'

const state = reactive({
  user: null,
  initialized: false,
  startupError: ''
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
    state.startupError = ''
  } catch (error) {
    if (error.status === 401) {
      state.user = null
      state.startupError = ''
    } else {
      state.user = null
      state.startupError = `Unable to reach the Project Pulse backend right now. Verify the deployed frontend/backend URLs and try again. ${error.message ?? ''}`.trim()
    }
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
  state.startupError = ''
  return user
}

async function signOut() {
  try {
    await logoutSession()
  } finally {
    resetCsrfTokenCache()
    state.user = null
    state.initialized = true
    state.startupError = ''
  }
}

export function useSessionStore() {
  return {
    state: readonly(state),
    user: computed(() => state.user),
    startupError: computed(() => state.startupError),
    isAuthenticated: computed(() => Boolean(state.user)),
    hydrateSession,
    signIn,
    signOut,
    defaultRouteForRole
  }
}
