<template>
  <AppShell :links="links" :current-user="currentUser" @logout="handleLogout">
    <RouterView />
  </AppShell>
</template>

<script setup>
import { computed } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { useSessionStore } from '@/features/auth/stores/sessionStore'
import AppShell from '@/shared/components/AppShell.vue'

const router = useRouter()
const session = useSessionStore()

const currentUser = computed(() => session.user.value)
const links = computed(() => {
  if (currentUser.value?.role === 'ADMIN') {
    return [
      {
        label: 'Requirements',
        to: '/requirements',
        caption: 'Source of truth'
      },
      {
        label: 'Admin',
        to: '/admin',
        caption: 'Sections, rosters, access'
      }
    ]
  }

  if (currentUser.value?.role === 'STUDENT') {
    return [
      {
        label: 'Student',
        to: '/student',
        caption: 'WARs and peer feedback'
      }
    ]
  }

  if (currentUser.value?.role === 'INSTRUCTOR') {
    return [
      {
        label: 'Instructor',
        to: '/instructor',
        caption: 'Lookup and reporting'
      }
    ]
  }

  return []
})

async function handleLogout() {
  await session.signOut()
  await router.push('/login')
}
</script>
