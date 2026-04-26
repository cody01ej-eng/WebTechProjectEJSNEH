<template>
  <section class="page">
    <header class="page__hero">
      <p class="eyebrow">Auth & User</p>
      <h2>Handle invitations, account changes, and instructor access.</h2>
      <p class="page__intro">
        This workspace now covers invitation/setup flows plus the account and roster actions that support student and
        instructor lifecycle management.
      </p>
    </header>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Invite Students</h3>
          <span class="chip">UC-11</span>
        </div>
        <form class="form-grid" @submit.prevent="handleInviteStudents">
          <label class="field">
            <span>Section ID</span>
            <input v-model="studentInvitation.sectionId" placeholder="1" />
          </label>
          <label class="field field--full">
            <span>Emails</span>
            <textarea
              v-model="studentInvitation.emails"
              rows="4"
              placeholder="student.one@tcu.edu; student.two@tcu.edu"
            />
          </label>
          <label class="field field--full">
            <span>Message</span>
            <textarea v-model="studentInvitation.message" rows="4" />
          </label>
          <button class="button" type="submit">Send Student Invitations</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Invite Instructors</h3>
          <span class="chip">UC-18</span>
        </div>
        <form class="form-grid" @submit.prevent="handleInviteInstructors">
          <label class="field field--full">
            <span>Emails</span>
            <textarea
              v-model="instructorInvitation.emails"
              rows="4"
              placeholder="instructor.one@tcu.edu; instructor.two@tcu.edu"
            />
          </label>
          <label class="field field--full">
            <span>Message</span>
            <textarea v-model="instructorInvitation.message" rows="4" />
          </label>
          <button class="button" type="submit">Send Instructor Invitations</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Register Student</h3>
          <span class="chip">UC-25</span>
        </div>
        <form class="form-grid" @submit.prevent="handleRegisterStudent">
          <label class="field">
            <span>Invitation Token</span>
            <input v-model="studentRegistration.token" placeholder="Paste token from invitation response" />
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
          <button class="button" type="submit">Register Student</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Register Instructor</h3>
          <span class="chip">UC-30</span>
        </div>
        <form class="form-grid" @submit.prevent="handleRegisterInstructor">
          <label class="field">
            <span>Invitation Token</span>
            <input v-model="instructorRegistration.token" placeholder="Paste token from invitation response" />
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
          <button class="button" type="submit">Register Instructor</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Find Users</h3>
          <span class="chip">UC-15 + UC-21</span>
        </div>
        <form class="form-grid" @submit.prevent="handleSearchUsers">
          <label class="field">
            <span>Role</span>
            <select v-model="userSearch.role">
              <option value="">All Roles</option>
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
            </select>
          </label>
          <label class="field">
            <span>Name Filter</span>
            <input v-model="userSearch.name" placeholder="Doe" />
          </label>
          <button class="button button--secondary" type="submit">Search Users</button>
        </form>

        <ul class="stack-list" v-if="userResults.length">
          <li v-for="user in userResults" :key="user.id" class="stack-list__item">
            <strong>{{ formatUserName(user) }}</strong>
            <span>{{ user.role }} - {{ user.status }}</span>
            <p>{{ user.email }}</p>
          </li>
        </ul>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>View or Edit Account</h3>
          <span class="chip">UC-16 + UC-22 + UC-26</span>
        </div>
        <form class="form-grid" @submit.prevent="handleUpdateUser">
          <label class="field">
            <span>User ID</span>
            <input v-model="userForm.id" placeholder="7" />
          </label>
          <button class="button button--secondary" type="button" @click="handleLoadUser">Load User</button>
          <label class="field">
            <span>First Name</span>
            <input v-model="userForm.firstName" />
          </label>
          <label class="field">
            <span>Middle Initial</span>
            <input v-model="userForm.middleInitial" />
          </label>
          <label class="field">
            <span>Last Name</span>
            <input v-model="userForm.lastName" />
          </label>
          <label class="field">
            <span>Email</span>
            <input v-model="userForm.email" />
          </label>
          <button class="button" type="submit">Save Account Changes</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Instructor Access</h3>
          <span class="chip">UC-23 + UC-24</span>
        </div>
        <form class="form-grid" @submit.prevent="handleDeactivateInstructor">
          <label class="field">
            <span>Instructor ID</span>
            <input v-model="instructorStatusForm.userId" placeholder="4" />
          </label>
          <label class="field field--full">
            <span>Deactivation Reason</span>
            <textarea v-model="instructorStatusForm.reason" rows="3" />
          </label>
          <button class="button" type="submit">Deactivate Instructor</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleReactivateInstructor">
          <label class="field">
            <span>Instructor ID</span>
            <input v-model="instructorStatusForm.userId" placeholder="4" />
          </label>
          <button class="button button--secondary" type="submit">Reactivate Instructor</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Latest Auth Response</h3>
          <span class="chip">Phase 3 Output</span>
        </div>
        <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
        <p v-if="errorMessage" class="status status--error">{{ errorMessage }}</p>
        <pre class="code-block">{{ latestResponse }}</pre>
      </article>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import {
  deactivateInstructor,
  findUsers,
  getUser,
  inviteInstructors,
  inviteStudents,
  reactivateInstructor,
  registerInstructor,
  registerStudent,
  updateUser
} from '@/features/auth/services/authService'

const statusMessage = ref('')
const errorMessage = ref('')
const latestResponse = ref('No auth or user request has been sent yet.')
const userResults = ref([])

const studentInvitation = reactive({
  sectionId: '',
  emails: '',
  message: 'Welcome to Project Pulse. Use your registration link to join the section and complete your account setup.'
})

const instructorInvitation = reactive({
  emails: '',
  message: 'Welcome to Project Pulse. Use your registration link to complete your instructor account setup.'
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

const userSearch = reactive({
  role: '',
  name: ''
})

const userForm = reactive({
  id: '',
  firstName: '',
  middleInitial: '',
  lastName: '',
  email: ''
})

const instructorStatusForm = reactive({
  userId: '',
  reason: 'No longer teaching this course.'
})

function splitEmails(rawEmails) {
  return rawEmails
    .split(';')
    .map((email) => email.trim())
    .filter(Boolean)
}

function setSuccess(message, payload) {
  statusMessage.value = message
  errorMessage.value = ''
  latestResponse.value = JSON.stringify(payload, null, 2)
}

function setError(error) {
  statusMessage.value = ''
  errorMessage.value = error.message
}

function formatUserName(user) {
  return [user.firstName, user.middleInitial, user.lastName].filter(Boolean).join(' ')
}

function applyUserToForm(user) {
  userForm.id = String(user.id)
  userForm.firstName = user.firstName
  userForm.middleInitial = user.middleInitial ?? ''
  userForm.lastName = user.lastName
  userForm.email = user.email
}

async function handleInviteStudents() {
  try {
    const response = await inviteStudents({
      sectionId: Number(studentInvitation.sectionId),
      emails: splitEmails(studentInvitation.emails),
      message: studentInvitation.message
    })
    setSuccess('Student invitations created.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleInviteInstructors() {
  try {
    const response = await inviteInstructors({
      sectionId: null,
      emails: splitEmails(instructorInvitation.emails),
      message: instructorInvitation.message
    })
    setSuccess('Instructor invitations created.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleRegisterStudent() {
  try {
    const response = await registerStudent({ ...studentRegistration })
    setSuccess('Student registration completed.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleRegisterInstructor() {
  try {
    const response = await registerInstructor({ ...instructorRegistration })
    setSuccess('Instructor registration completed.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleSearchUsers() {
  try {
    const response = await findUsers(userSearch.role || undefined, userSearch.name || undefined)
    userResults.value = response
    setSuccess(`Loaded ${response.length} user record(s).`, response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadUser() {
  try {
    const response = await getUser(Number(userForm.id))
    applyUserToForm(response)
    setSuccess('User loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleUpdateUser() {
  try {
    const response = await updateUser(Number(userForm.id), {
      firstName: userForm.firstName,
      middleInitial: userForm.middleInitial || null,
      lastName: userForm.lastName,
      email: userForm.email
    })
    applyUserToForm(response)
    setSuccess('User account updated.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleDeactivateInstructor() {
  try {
    const response = await deactivateInstructor(Number(instructorStatusForm.userId), {
      reason: instructorStatusForm.reason
    })
    setSuccess('Instructor deactivated.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleReactivateInstructor() {
  try {
    const response = await reactivateInstructor(Number(instructorStatusForm.userId))
    setSuccess('Instructor reactivated.', response)
  } catch (error) {
    setError(error)
  }
}
</script>
