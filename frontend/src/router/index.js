import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import LoginView from '@/features/auth/views/LoginView.vue'
import RequirementsWorkspace from '@/features/requirements/views/RequirementsWorkspace.vue'
import AdminWorkspace from '@/features/workspaces/views/AdminWorkspace.vue'
import StudentWorkspace from '@/features/workspaces/views/StudentWorkspace.vue'
import InstructorWorkspace from '@/features/workspaces/views/InstructorWorkspace.vue'
import { useSessionStore } from '@/features/auth/stores/sessionStore'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      guestOnly: true
    }
  },
  {
    path: '/',
    component: DefaultLayout,
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'requirements',
        name: 'requirements',
        component: RequirementsWorkspace,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'admin',
        name: 'admin',
        component: AdminWorkspace,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'student',
        name: 'student',
        component: StudentWorkspace,
        meta: {
          requiresAuth: true,
          roles: ['STUDENT']
        }
      },
      {
        path: 'instructor',
        name: 'instructor',
        component: InstructorWorkspace,
        meta: {
          requiresAuth: true,
          roles: ['INSTRUCTOR']
        }
      },
      {
        path: 'projects',
        redirect: '/admin'
      },
      {
        path: 'auth',
        redirect: '/login'
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to) => {
  const session = useSessionStore()
  await session.hydrateSession()

  const currentUser = session.user.value
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const isGuestOnly = to.matched.some((record) => record.meta.guestOnly)
  const allowedRoles = to.matched.reduce((roles, record) => {
    if (Array.isArray(record.meta.roles)) {
      return roles.concat(record.meta.roles)
    }
    return roles
  }, [])

  if (to.path === '/') {
    return currentUser ? session.defaultRouteForRole(currentUser.role) : '/login'
  }

  if (isGuestOnly && currentUser) {
    return session.defaultRouteForRole(currentUser.role)
  }

  if (requiresAuth && !currentUser) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(currentUser?.role)) {
    return currentUser ? session.defaultRouteForRole(currentUser.role) : '/login'
  }

  return true
})

export default router
