<template>
  <div class="shell">
    <aside class="shell__rail">
      <div class="brand-card">
        <p class="eyebrow">Project Pulse</p>
        <h1>Build from requirements, then work by role.</h1>
        <p class="brand-copy">
          The frontend keeps the product contract nearby while surfacing dedicated workspaces for admins, students, and
          instructors.
        </p>
      </div>

      <nav class="nav-card" aria-label="Primary">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="nav-link"
          active-class="nav-link--active"
        >
          <span>{{ link.label }}</span>
          <small>{{ link.caption }}</small>
        </RouterLink>
      </nav>

      <div v-if="currentUser" class="session-card">
        <p class="eyebrow">{{ currentUser.role }}</p>
        <strong>{{ [currentUser.firstName, currentUser.lastName].filter(Boolean).join(' ') }}</strong>
        <p>{{ currentUser.email }}</p>
        <button class="button button--ghost button--compact" type="button" @click="$emit('logout')">Sign Out</button>
      </div>
    </aside>

    <main class="shell__content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  links: {
    type: Array,
    required: true
  },
  currentUser: {
    type: Object,
    default: null
  }
})

defineEmits(['logout'])
</script>
