<template>
  <section class="page page--auth">
    <header class="page__hero">
      <p class="eyebrow">Project Pulse Access</p>
      <h2>Sign in with your role, or finish invitation-based account setup.</h2>
      <p class="page__intro">
        Admins sign in with the bootstrap or configured admin account. Students and instructors can complete account
        setup from their invitation tokens and land directly in their protected dashboards.
      </p>
      <p v-if="invitationLinkMessage" class="status">{{ invitationLinkMessage }}</p>
    </header>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Sign In</h3>
          <span class="chip">Authenticated Access</span>
        </div>
        <form class="form-grid" @submit.prevent="handleLogin">
          <label class="field">
            <span>Email</span>
            <input v-model="loginForm.email" placeholder="admin@projectpulse.local" />
          </label>
          <label class="field">
            <span>Password</span>
            <input v-model="loginForm.password" type="password" placeholder="Enter your password" />
          </label>
          <button class="button" type="submit">Sign In</button>
        </form>
        <p class="muted-note">
          Local development bootstraps an admin account only when the backend runs with the `local` profile. Keep
          `BOOTSTRAP_ADMIN_*` disabled outside local/dev unless you intentionally opt in.
        </p>
      </article>

      <article class="panel" :class="{ 'panel--accent': activeInviteMode === 'student-register' }">
        <div class="panel__header">
          <h3>Student Invitation Registration</h3>
          <span class="chip">UC-25</span>
        </div>
        <form class="form-grid" @submit.prevent="handleRegisterStudent">
          <label class="field">
            <span>Invitation Token</span>
            <input v-model="studentRegistration.token" placeholder="Paste token from your invitation" />
          </label>
          <label class="field">
            <span>First Name</span>
            <input v-model="studentRegistration.firstName" />
          </label>
          <label class="field">
            <span>Last Name</span>
            <input v-model="studentRegistration.lastName" />
          </label>
          <label class="field">
            <span>Email</span>
            <input v-model="studentRegistration.email" />
          </label>
          <label class="field">
            <span>Password</span>
            <input v-model="studentRegistration.password" type="password" />
          </label>
          <button class="button button--secondary" type="submit">Create Student Account</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel" :class="{ 'panel--accent': activeInviteMode === 'instructor-register' }">
        <div class="panel__header">
          <h3>Instructor Invitation Registration</h3>
          <span class="chip">UC-30</span>
        </div>
        <form class="form-grid" @submit.prevent="handleRegisterInstructor">
          <label class="field">
            <span>Invitation Token</span>
            <input v-model="instructorRegistration.token" placeholder="Paste token from your invitation" />
          </label>
          <label class="field">
            <span>First Name</span>
            <input v-model="instructorRegistration.firstName" />
          </label>
          <label class="field">
            <span>Middle Initial</span>
            <input v-model="instructorRegistration.middleInitial" />
          </label>
          <label class="field">
            <span>Last Name</span>
            <input v-model="instructorRegistration.lastName" />
          </label>
          <label class="field">
            <span>Password</span>
            <input v-model="instructorRegistration.password" type="password" />
          </label>
          <label class="field">
            <span>Confirm Password</span>
            <input v-model="instructorRegistration.confirmPassword" type="password" />
          </label>
          <button class="button button--secondary" type="submit">Create Instructor Account</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Latest Access Response</h3>
          <span class="chip">Auth Output</span>
        </div>
        <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
        <p v-if="errorMessage" class="status status--error">{{ errorMessage }}</p>
        <pre class="code-block">{{ latestResponse }}</pre>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { registerInstructor, registerStudent } from '@/features/auth/services/authService'
import { useSessionStore } from '@/features/auth/stores/sessionStore'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()

const invitationLinkMessage = ref('')
const statusMessage = ref('')
const errorMessage = ref('')
const latestResponse = ref('No access request has been sent yet.')

const activeInviteMode = computed(() => {
  const mode = typeof route.query.mode === 'string' ? route.query.mode : ''
  return ['student-register', 'instructor-register'].includes(mode) ? mode : null
})

const loginForm = reactive({
  email: 'admin@projectpulse.local',
  password: 'Admin123!'
})

const studentRegistration = reactive({
  token: '',
  firstName: '',
  lastName: '',
  email: '',
  password: ''
})

const instructorRegistration = reactive({
  token: '',
  firstName: '',
  middleInitial: '',
  lastName: '',
  password: '',
  confirmPassword: ''
})

watch(
  () => [activeInviteMode.value, typeof route.query.token === 'string' ? route.query.token : ''],
  ([mode, token]) => {
    const normalizedToken = token.trim()
    if (!mode || !normalizedToken) {
      invitationLinkMessage.value = ''
      return
    }

    if (mode === 'student-register') {
      studentRegistration.token = normalizedToken
      invitationLinkMessage.value = 'Student invitation link detected. Complete the student registration form below.'
      return
    }

    instructorRegistration.token = normalizedToken
    invitationLinkMessage.value = 'Instructor invitation link detected. Complete the instructor registration form below.'
  },
  { immediate: true }
)

function setSuccess(message, payload) {
  statusMessage.value = message
  errorMessage.value = ''
  latestResponse.value = JSON.stringify(payload, null, 2)
}

function normalizeErrorMessage(error) {
  if (error.status === 401) {
    return 'Sign-in failed. Check your email and password and try again.'
  }
  if (error.status === 423) {
    return 'This account is inactive. Ask an admin to reactivate it before signing in.'
  }
  return error.message
}

function setError(error) {
  statusMessage.value = ''
  errorMessage.value = normalizeErrorMessage(error)
}

function buildValidationError(message) {
  const error = new Error(message)
  error.status = 400
  return error
}

function trimOptional(value) {
  const trimmed = value.trim()
  return trimmed === '' ? '' : trimmed
}

function validateLoginForm() {
  if (!loginForm.email.trim()) {
    throw buildValidationError('Email is required.')
  }
  if (!loginForm.password) {
    throw buildValidationError('Password is required.')
  }
  if (loginForm.password.length < 8) {
    throw buildValidationError('Password must be at least 8 characters long.')
  }
}

function validateStudentRegistrationForm() {
  if (!studentRegistration.token.trim()) {
    throw buildValidationError('Invitation token is required for student registration.')
  }
  if (!studentRegistration.firstName.trim()) {
    throw buildValidationError('First name is required for student registration.')
  }
  if (!studentRegistration.lastName.trim()) {
    throw buildValidationError('Last name is required for student registration.')
  }
  if (!studentRegistration.email.trim()) {
    throw buildValidationError('Email is required for student registration.')
  }
  if (!studentRegistration.password) {
    throw buildValidationError('Password is required for student registration.')
  }
  if (studentRegistration.password.length < 8) {
    throw buildValidationError('Student passwords must be at least 8 characters long.')
  }
}

function validateInstructorRegistrationForm() {
  if (!instructorRegistration.token.trim()) {
    throw buildValidationError('Invitation token is required for instructor registration.')
  }
  if (!instructorRegistration.firstName.trim()) {
    throw buildValidationError('First name is required for instructor registration.')
  }
  if (!instructorRegistration.lastName.trim()) {
    throw buildValidationError('Last name is required for instructor registration.')
  }
  if (!instructorRegistration.password) {
    throw buildValidationError('Password is required for instructor registration.')
  }
  if (instructorRegistration.password.length < 8) {
    throw buildValidationError('Instructor passwords must be at least 8 characters long.')
  }
  if (!instructorRegistration.confirmPassword) {
    throw buildValidationError('Password confirmation is required for instructor registration.')
  }
  if (instructorRegistration.password !== instructorRegistration.confirmPassword) {
    throw buildValidationError('Password confirmation must match the instructor password.')
  }
}

function toStudentRegistrationPayload() {
  return {
    token: studentRegistration.token.trim(),
    firstName: studentRegistration.firstName.trim(),
    lastName: studentRegistration.lastName.trim(),
    email: studentRegistration.email.trim(),
    password: studentRegistration.password
  }
}

function toInstructorRegistrationPayload() {
  return {
    token: instructorRegistration.token.trim(),
    firstName: instructorRegistration.firstName.trim(),
    middleInitial: trimOptional(instructorRegistration.middleInitial),
    lastName: instructorRegistration.lastName.trim(),
    password: instructorRegistration.password,
    confirmPassword: instructorRegistration.confirmPassword
  }
}

async function goToDefaultRoute(user) {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
  await router.push(redirect || session.defaultRouteForRole(user.role))
}

async function handleLogin() {
  try {
    validateLoginForm()
    const user = await session.signIn({
      email: loginForm.email.trim(),
      password: loginForm.password
    })
    setSuccess('Login completed.', user)
    await goToDefaultRoute(user)
  } catch (error) {
    setError(error)
  }
}

async function handleRegisterStudent() {
  try {
    validateStudentRegistrationForm()
    const payload = toStudentRegistrationPayload()
    const response = await registerStudent(payload)
    const user = await session.signIn({
      email: payload.email,
      password: payload.password
    })
    setSuccess('Student registration completed.', response)
    await goToDefaultRoute(user)
  } catch (error) {
    setError(error)
  }
}

async function handleRegisterInstructor() {
  try {
    validateInstructorRegistrationForm()
    const payload = toInstructorRegistrationPayload()
    const response = await registerInstructor(payload)
    const user = await session.signIn({
      email: response.email,
      password: payload.password
    })
    setSuccess('Instructor registration completed.', response)
    await goToDefaultRoute(user)
  } catch (error) {
    setError(error)
  }
}
</script>
