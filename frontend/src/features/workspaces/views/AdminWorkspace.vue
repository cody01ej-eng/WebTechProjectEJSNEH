<template>
  <section class="page">
    <header class="page__hero">
      <p class="eyebrow">Admin Workspace</p>
      <h2>Shape the senior design program from setup through roster control.</h2>
      <p class="page__intro">
        This screen combines the implemented admin use cases for sections, teams, invitations, and instructor access
        so the program can be managed without hopping between domain-oriented operator pages.
      </p>
    </header>

    <article class="panel">
      <div class="panel__header">
        <h3>Program Snapshot</h3>
        <button class="button button--secondary" type="button" @click="loadReferenceData">Refresh Workspace Data</button>
      </div>
      <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
      <p v-if="errorMessage" class="status status--error">{{ errorMessage }}</p>
      <div class="stat-row">
        <div class="stat-card">
          <strong>{{ rubrics.length }}</strong>
          <span>Rubrics</span>
        </div>
        <div class="stat-card">
          <strong>{{ sections.length }}</strong>
          <span>Sections</span>
        </div>
        <div class="stat-card">
          <strong>{{ teams.length }}</strong>
          <span>Teams</span>
        </div>
      </div>
      <p class="muted-note">
        Signed in as {{ adminName }}. Select a section, team, or user from the directories below and the related setup,
        invitation, and access actions will prefill around that current focus.
      </p>
    </article>

    <article class="panel panel--accent">
      <div class="panel__header">
        <h3>Admin Focus</h3>
        <span class="chip">Guided Actions</span>
      </div>
      <div class="stat-row">
        <div class="stat-card">
          <strong>{{ selectedSectionLabel }}</strong>
          <span>Current section</span>
        </div>
        <div class="stat-card">
          <strong>{{ selectedTeamLabel }}</strong>
          <span>Current team</span>
        </div>
        <div class="stat-card">
          <strong>{{ selectedUserLabel }}</strong>
          <span>Current user</span>
        </div>
      </div>
      <p class="muted-note">
        Section selection now drives team creation and student invitations. Team selection drives roster updates.
        Instructor user selection drives access-management actions.
      </p>
    </article>

    <div class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === tab.id }"
        type="button"
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="panel-grid" v-show="activeTab === 'setup'">
      <article class="panel">
        <div class="panel__header">
          <h3>Rubrics and Sections</h3>
          <span class="chip">UC-1, UC-4, UC-6</span>
        </div>

        <form class="form-grid" @submit.prevent="handleCreateRubric">
          <label class="field field--full">
            <span>Rubric Name</span>
            <input v-model="rubricForm.name" placeholder="Peer Eval Rubric v1" />
          </label>
          <div class="subform-list">
            <div v-for="(criterion, index) in rubricForm.criteria" :key="index" class="subform-card">
              <label class="field">
                <span>Criterion</span>
                <input v-model="criterion.name" />
              </label>
              <label class="field field--full">
                <span>Description</span>
                <textarea v-model="criterion.description" rows="2" />
              </label>
              <label class="field">
                <span>Max Score</span>
                <input v-model="criterion.maxScore" type="number" min="1" step="1" />
              </label>
            </div>
          </div>
          <button class="button" type="submit">Create Rubric</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleCreateSection">
          <label class="field">
            <span>Section Name</span>
            <input v-model="sectionForm.name" placeholder="2026-2027" />
          </label>
          <label class="field">
            <span>Start Date</span>
            <input v-model="sectionForm.startDate" type="date" />
          </label>
          <label class="field">
            <span>End Date</span>
            <input v-model="sectionForm.endDate" type="date" />
          </label>
          <label class="field">
            <span>Rubric</span>
            <select v-model="sectionForm.rubricId">
              <option value="">Select a rubric</option>
              <option v-for="rubric in rubrics" :key="rubric.id" :value="rubric.id">
                {{ rubric.name }}
              </option>
            </select>
          </label>
          <button class="button" type="submit">Create Section</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Section Directory</h3>
          <span class="chip">UC-2, UC-3, UC-5</span>
        </div>

        <form class="form-grid" @submit.prevent="handleFindSections">
          <label class="field">
            <span>Section Name Filter</span>
            <input v-model="sectionDirectoryForm.name" placeholder="2026-2027" />
          </label>
          <button class="button button--secondary" type="submit">Find Sections</button>
        </form>

        <ul class="stack-list" v-if="sections.length">
          <li v-for="section in sections" :key="section.id" class="stack-list__item">
            <strong>{{ section.name }}</strong>
            <span>{{ section.rubricName }}</span>
            <p>{{ section.startDate }} to {{ section.endDate }}</p>
            <div class="stack-list__actions">
              <button class="button button--ghost button--compact" type="button" @click="useSection(section)">
                Use Section
              </button>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadSectionById(section.id)">
                Load Section Detail
              </button>
            </div>
          </li>
        </ul>

        <form class="form-grid form-grid--inline" @submit.prevent="handleUpdateSection">
          <label class="field">
            <span>Section ID</span>
            <input v-model="editSectionForm.sectionId" placeholder="1" />
          </label>
          <div class="field field--full">
            <button class="button button--secondary" type="button" @click="handleLoadSection">Load Section</button>
          </div>
          <label class="field">
            <span>Section Name</span>
            <input v-model="editSectionForm.name" />
          </label>
          <label class="field">
            <span>Start Date</span>
            <input v-model="editSectionForm.startDate" type="date" />
          </label>
          <label class="field">
            <span>End Date</span>
            <input v-model="editSectionForm.endDate" type="date" />
          </label>
          <label class="field">
            <span>Rubric</span>
            <select v-model="editSectionForm.rubricId">
              <option value="">Select a rubric</option>
              <option v-for="rubric in rubrics" :key="rubric.id" :value="rubric.id">
                {{ rubric.name }}
              </option>
            </select>
          </label>
          <button class="button" type="submit">Save Section Changes</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleConfigureActiveWeeks">
          <label class="field">
            <span>Inactive Week Start Dates</span>
            <textarea
              v-model="activeWeekForm.inactiveWeekStartDates"
              rows="3"
              placeholder="2026-12-21, 2026-12-28"
            />
          </label>
          <button class="button button--secondary" type="submit">Configure Active Weeks</button>
        </form>

        <div class="data-grid" v-if="loadedSection">
          <section class="data-card">
            <h4>Loaded Section</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>{{ loadedSection.name }}</strong>
                <span>{{ loadedSection.rubricName }}</span>
                <p>{{ loadedSection.startDate }} to {{ loadedSection.endDate }}</p>
              </li>
            </ul>
          </section>
          <section class="data-card">
            <h4>Weeks and Teams</h4>
            <ul class="stack-list">
              <li v-for="week in loadedSection.weeks" :key="week.id" class="stack-list__item">
                <strong>{{ week.weekStartDate }}</strong>
                <span>{{ week.active ? 'Active week' : 'Inactive week' }}</span>
              </li>
            </ul>
            <ul class="stack-list" v-if="loadedSection.teams.length">
              <li v-for="team in loadedSection.teams" :key="team.id" class="stack-list__item">
                <strong>{{ team.name }}</strong>
                <span>Team {{ team.id }}</span>
              </li>
            </ul>
          </section>
        </div>
      </article>
    </div>

    <div class="panel-grid" v-show="activeTab === 'teams'">
      <article class="panel">
        <div class="panel__header">
          <h3>Team Operations</h3>
          <span class="chip">UC-7 through UC-13, UC-19, UC-20</span>
        </div>
        <p class="muted-note">
          New teams default to {{ selectedSectionLabel }} when a section is selected. Roster updates use the currently
          selected team.
        </p>

        <form class="form-grid" @submit.prevent="handleCreateTeam">
          <label class="field">
            <span>Section</span>
            <select v-model="teamForm.sectionId">
              <option value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>
          <label class="field">
            <span>Team Name</span>
            <input v-model="teamForm.name" placeholder="Project Pulse Team" />
          </label>
          <label class="field field--full">
            <span>Description</span>
            <textarea v-model="teamForm.description" rows="3" />
          </label>
          <label class="field">
            <span>Website URL</span>
            <input v-model="teamForm.websiteUrl" placeholder="https://example.com" />
          </label>
          <button class="button" type="submit">Create Team</button>
        </form>

        <ul class="stack-list" v-if="teams.length">
          <li v-for="team in teams" :key="team.id" class="stack-list__item">
            <strong>{{ team.name }}</strong>
            <span>{{ team.sectionName }}</span>
            <p>{{ team.description }}</p>
            <div class="stack-list__actions">
              <button class="button button--ghost button--compact" type="button" @click="useTeam(team)">
                Use Team
              </button>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadTeamById(team.id)">
                Load Team Detail
              </button>
            </div>
          </li>
        </ul>

        <form class="form-grid form-grid--inline" @submit.prevent="handleUpdateTeam">
          <label class="field">
            <span>Team ID</span>
            <input v-model="editTeamForm.teamId" placeholder="1" />
          </label>
          <div class="field field--full">
            <button class="button button--secondary" type="button" @click="handleLoadTeam">Load Team</button>
          </div>
          <label class="field">
            <span>Team Name</span>
            <input v-model="editTeamForm.name" />
          </label>
          <label class="field field--full">
            <span>Description</span>
            <textarea v-model="editTeamForm.description" rows="3" />
          </label>
          <label class="field">
            <span>Website URL</span>
            <input v-model="editTeamForm.websiteUrl" placeholder="https://example.com" />
          </label>
          <button class="button" type="submit">Save Team Changes</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleAssignStudents">
          <label class="field">
            <span>Student IDs</span>
            <textarea v-model="membershipForm.studentIds" rows="2" placeholder="12, 14, 15" />
          </label>
          <button class="button button--secondary" type="submit">Assign Students</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleRemoveStudent">
          <label class="field">
            <span>Student ID to Remove</span>
            <input v-model="membershipForm.removeStudentId" />
          </label>
          <button class="button button--ghost" type="submit">Remove Student</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleAssignInstructors">
          <label class="field">
            <span>Instructor IDs</span>
            <textarea v-model="membershipForm.instructorIds" rows="2" placeholder="3, 5" />
          </label>
          <button class="button button--secondary" type="submit">Assign Instructors</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleRemoveInstructor">
          <label class="field">
            <span>Instructor ID to Remove</span>
            <input v-model="membershipForm.removeInstructorId" />
          </label>
          <button class="button button--ghost" type="submit">Remove Instructor</button>
        </form>

        <div class="data-grid" v-if="loadedTeam">
          <section class="data-card">
            <h4>Students</h4>
            <ul class="stack-list">
              <li v-for="student in loadedTeam.students" :key="student.id" class="stack-list__item">
                <strong>{{ student.name }}</strong>
                <span>Student {{ student.id }}</span>
                <p>{{ student.email }}</p>
              </li>
            </ul>
          </section>
          <section class="data-card">
            <h4>Instructors</h4>
            <ul class="stack-list">
              <li v-for="instructor in loadedTeam.instructors" :key="instructor.id" class="stack-list__item">
                <strong>{{ instructor.name }}</strong>
                <span>Instructor {{ instructor.id }}</span>
                <p>{{ instructor.email }}</p>
              </li>
            </ul>
          </section>
        </div>
      </article>
    </div>

    <div class="panel-grid" v-show="activeTab === 'access'">
      <article class="panel">
        <div class="panel__header">
          <h3>Invitations and Access</h3>
          <span class="chip">UC-11, UC-18, UC-23, UC-24</span>
        </div>
        <p class="muted-note">
          Student invites follow the selected section, and instructor access actions follow the selected instructor
          when you pick one from the user directory.
        </p>

        <form class="form-grid" @submit.prevent="handleInviteStudents">
          <label class="field">
            <span>Section</span>
            <select v-model="studentInvitation.sectionId">
              <option value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>
          <label class="field field--full">
            <span>Student Emails</span>
            <textarea
              v-model="studentInvitation.emails"
              rows="3"
              placeholder="student.one@tcu.edu; student.two@tcu.edu"
            />
          </label>
          <label class="field field--full">
            <span>Invitation Message</span>
            <textarea v-model="studentInvitation.message" rows="3" />
          </label>
          <button class="button" type="submit">Invite Students</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleInviteInstructors">
          <label class="field field--full">
            <span>Instructor Emails</span>
            <textarea
              v-model="instructorInvitation.emails"
              rows="3"
              placeholder="instructor.one@tcu.edu; instructor.two@tcu.edu"
            />
          </label>
          <label class="field field--full">
            <span>Invitation Message</span>
            <textarea v-model="instructorInvitation.message" rows="3" />
          </label>
          <button class="button button--secondary" type="submit">Invite Instructors</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleDeactivateInstructor">
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
          <button class="button button--ghost" type="submit">Reactivate Instructor</button>
        </form>
      </article>
    </div>

    <div class="panel-grid" v-show="activeTab === 'users'">
      <article class="panel">
        <div class="panel__header">
          <h3>User Directory</h3>
          <span class="chip">UC-15, UC-16, UC-21, UC-22</span>
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
          <button class="button button--secondary" type="submit">Find Users</button>
        </form>

        <ul class="stack-list" v-if="userResults.length">
          <li v-for="user in userResults" :key="user.id" class="stack-list__item">
            <strong>{{ formatUserName(user) }}</strong>
            <span>{{ user.role }} - {{ user.status }}</span>
            <p>{{ user.email }}</p>
            <div class="stack-list__actions">
              <button class="button button--ghost button--compact" type="button" @click="useUser(user)">
                Use User
              </button>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadUserById(user.id)">
                Load User Detail
              </button>
            </div>
          </li>
        </ul>

        <form class="form-grid form-grid--inline" @submit.prevent="handleUpdateUser">
          <label class="field">
            <span>User ID</span>
            <input v-model="userForm.id" placeholder="7" />
          </label>
          <div class="field field--full">
            <button class="button button--secondary" type="button" @click="handleLoadUser">Load User</button>
          </div>
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
          <button class="button" type="submit">Save User Changes</button>
        </form>

        <div class="data-grid" v-if="loadedUser">
          <section class="data-card">
            <h4>Loaded User</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>{{ formatUserName(loadedUser) }}</strong>
                <span>{{ loadedUser.role }} - {{ loadedUser.status }}</span>
                <p>{{ loadedUser.email }}</p>
              </li>
            </ul>
          </section>
          <section class="data-card">
            <h4>Assignments</h4>
            <ul class="stack-list">
              <li class="stack-list__item" v-if="loadedUser.sectionName">
                <strong>Section</strong>
                <span>{{ loadedUser.sectionName }}</span>
              </li>
              <li class="stack-list__item" v-if="loadedUser.assignedTeamName">
                <strong>Assigned Team</strong>
                <span>{{ loadedUser.assignedTeamName }}</span>
              </li>
              <li
                v-for="teamName in loadedUser.supervisedTeams"
                :key="teamName"
                class="stack-list__item"
              >
                <strong>Supervised Team</strong>
                <span>{{ teamName }}</span>
              </li>
            </ul>
          </section>
        </div>
      </article>

      <article v-if="isDev" class="panel">
        <div class="panel__header">
          <h3>Latest Admin Response</h3>
          <span class="chip">Dev Only</span>
        </div>
        <pre class="code-block">{{ latestResponse }}</pre>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deactivateInstructor,
  findUsers,
  getUser,
  inviteInstructors,
  inviteStudents,
  reactivateInstructor,
  updateUser
} from '@/features/auth/services/authService'
import { useSessionStore } from '@/features/auth/stores/sessionStore'
import {
  assignInstructors,
  assignStudents,
  configureActiveWeeks,
  createRubric,
  createSection,
  createTeam,
  getRubrics,
  getSection,
  getSections,
  getTeam,
  getTeams,
  removeInstructorFromTeam,
  removeStudentFromTeam,
  updateSection,
  updateTeam
} from '@/features/projects/services/projectService'

const session = useSessionStore()
const rubrics = ref([])
const sections = ref([])
const teams = ref([])
const userResults = ref([])
const loadedSection = ref(null)
const loadedTeam = ref(null)
const loadedUser = ref(null)
const statusMessage = ref('')
const errorMessage = ref('')
const latestResponse = ref('No admin request has been sent yet.')

const activeTab = ref('setup')
const tabs = [
  { id: 'setup', label: 'Setup' },
  { id: 'teams', label: 'Teams' },
  { id: 'access', label: 'Access' },
  { id: 'users', label: 'Users' },
]
const isDev = import.meta.env.DEV

const rubricForm = reactive({
  name: 'Peer Eval Rubric v1',
  criteria: [
    { name: 'Quality of work', description: "How do you rate the quality of this teammate's work?", maxScore: 10 },
    { name: 'Productivity', description: 'How productive is this teammate?', maxScore: 10 },
    { name: 'Initiative', description: 'How proactive is this teammate?', maxScore: 10 },
    { name: 'Courtesy', description: 'Does this teammate treat others with respect?', maxScore: 10 },
    { name: 'Open-mindedness', description: 'How well does this teammate handle criticism?', maxScore: 10 },
    { name: 'Engagement in meetings', description: "How is this teammate's performance during meetings?", maxScore: 10 }
  ]
})

const sectionForm = reactive({
  name: '',
  startDate: '',
  endDate: '',
  rubricId: ''
})

const sectionDirectoryForm = reactive({
  name: ''
})

const editSectionForm = reactive({
  sectionId: '',
  name: '',
  startDate: '',
  endDate: '',
  rubricId: ''
})

const activeWeekForm = reactive({
  inactiveWeekStartDates: ''
})

const teamForm = reactive({
  sectionId: '',
  name: '',
  description: '',
  websiteUrl: ''
})

const editTeamForm = reactive({
  teamId: '',
  name: '',
  description: '',
  websiteUrl: ''
})

const membershipForm = reactive({
  studentIds: '',
  instructorIds: '',
  removeStudentId: '',
  removeInstructorId: ''
})

const studentInvitation = reactive({
  sectionId: '',
  emails: '',
  message: 'You have been invited to join Project Pulse as a student in your senior design section.'
})

const instructorInvitation = reactive({
  emails: '',
  message: 'You have been invited to join Project Pulse as an instructor and complete your account setup.'
})

const instructorStatusForm = reactive({
  userId: '',
  reason: 'No longer teaching this course.'
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

const adminName = computed(() => formatUserName(session.user.value) || 'your admin account')
const selectedSectionLabel = computed(() => loadedSection.value?.name || 'None')
const selectedTeamLabel = computed(() => loadedTeam.value?.name || 'None')
const selectedUserLabel = computed(() => loadedUser.value ? formatUserName(loadedUser.value) : 'None')

function splitEmails(rawEmails) {
  return rawEmails
    .split(';')
    .map((email) => email.trim())
    .filter(Boolean)
}

function parseNumberList(value) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => Number(item))
}

function parseDateList(value) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function normalizeOptionalValue(value) {
  const trimmed = value?.trim?.() ?? value
  return trimmed === '' ? null : trimmed
}

function formatUserName(user) {
  return [user.firstName, user.middleInitial, user.lastName].filter(Boolean).join(' ')
}

function summarizeInvitationResult(label, invitations) {
  const failedCount = invitations.filter((invitation) => invitation.status === 'FAILED').length
  if (failedCount === 0) {
    return `${label} invitation email${invitations.length === 1 ? '' : 's'} sent.`
  }
  return `${label} invitations processed, but ${failedCount} email delivery failure${failedCount === 1 ? '' : 's'} occurred. Review the response details and SMTP configuration.`
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

function applySection(section) {
  loadedSection.value = section
  editSectionForm.sectionId = String(section.id)
  editSectionForm.name = section.name
  editSectionForm.startDate = section.startDate
  editSectionForm.endDate = section.endDate
  editSectionForm.rubricId = String(section.rubricId)
  teamForm.sectionId = String(section.id)
  studentInvitation.sectionId = String(section.id)
  activeWeekForm.inactiveWeekStartDates = section.weeks
    .filter((week) => !week.active)
    .map((week) => week.weekStartDate)
    .join(', ')
}

function applyTeam(team) {
  loadedTeam.value = team
  editTeamForm.teamId = String(team.id)
  editTeamForm.name = team.name
  editTeamForm.description = team.description
  editTeamForm.websiteUrl = team.websiteUrl ?? ''
}

function applyUser(user) {
  loadedUser.value = user
  userForm.id = String(user.id)
  userForm.firstName = user.firstName
  userForm.middleInitial = user.middleInitial ?? ''
  userForm.lastName = user.lastName
  userForm.email = user.email
  if (user.sectionId) {
    teamForm.sectionId = String(user.sectionId)
    studentInvitation.sectionId = String(user.sectionId)
  }
  if (user.assignedTeamId) {
    editTeamForm.teamId = String(user.assignedTeamId)
  }
  if (user.role === 'INSTRUCTOR') {
    instructorStatusForm.userId = String(user.id)
  }
}

function useSection(section) {
  applySection(section)
  setSuccess('Section selected from directory.', section)
}

function useTeam(team) {
  applyTeam(team)
  setSuccess('Team selected from directory.', team)
}

function useUser(user) {
  applyUser(user)
  setSuccess('User selected from directory.', user)
}

async function loadReferenceData() {
  try {
    const [rubricData, sectionData, teamData] = await Promise.all([
      getRubrics(),
      getSections(sectionDirectoryForm.name || undefined),
      getTeams()
    ])
    rubrics.value = rubricData
    sections.value = sectionData
    teams.value = teamData
  } catch (error) {
    setError(error)
  }
}

async function handleCreateRubric() {
  try {
    const response = await createRubric({
      name: rubricForm.name,
      criteria: rubricForm.criteria.map((criterion) => ({
        ...criterion,
        maxScore: Number(criterion.maxScore)
      }))
    })
    setSuccess('Rubric created.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleCreateSection() {
  try {
    const response = await createSection({
      name: sectionForm.name,
      startDate: sectionForm.startDate,
      endDate: sectionForm.endDate,
      rubricId: Number(sectionForm.rubricId)
    })
    applySection(response)
    setSuccess('Section created.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleFindSections() {
  await loadReferenceData()
  setSuccess(`Loaded ${sections.value.length} section record(s).`, sections.value)
}

async function handleLoadSection() {
  try {
    const response = await getSection(Number(editSectionForm.sectionId))
    applySection(response)
    setSuccess('Section loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadSectionById(sectionId) {
  try {
    editSectionForm.sectionId = String(sectionId)
    const response = await getSection(Number(sectionId))
    applySection(response)
    setSuccess('Section loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleUpdateSection() {
  try {
    const response = await updateSection(Number(editSectionForm.sectionId), {
      name: editSectionForm.name,
      startDate: editSectionForm.startDate,
      endDate: editSectionForm.endDate,
      rubricId: Number(editSectionForm.rubricId)
    })
    applySection(response)
    setSuccess('Section updated.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleConfigureActiveWeeks() {
  try {
    const response = await configureActiveWeeks(Number(editSectionForm.sectionId), {
      inactiveWeekStartDates: parseDateList(activeWeekForm.inactiveWeekStartDates)
    })
    applySection(response)
    setSuccess('Active weeks configured.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleCreateTeam() {
  try {
    const response = await createTeam({
      sectionId: Number(teamForm.sectionId),
      name: teamForm.name,
      description: teamForm.description,
      websiteUrl: normalizeOptionalValue(teamForm.websiteUrl)
    })
    applyTeam(response)
    setSuccess('Team created.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeam() {
  try {
    const response = await getTeam(Number(editTeamForm.teamId))
    applyTeam(response)
    setSuccess('Team loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeamById(teamId) {
  try {
    editTeamForm.teamId = String(teamId)
    const response = await getTeam(Number(teamId))
    applyTeam(response)
    setSuccess('Team loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleUpdateTeam() {
  try {
    const response = await updateTeam(Number(editTeamForm.teamId), {
      name: editTeamForm.name,
      description: editTeamForm.description,
      websiteUrl: normalizeOptionalValue(editTeamForm.websiteUrl)
    })
    applyTeam(response)
    setSuccess('Team updated.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleAssignStudents() {
  try {
    const response = await assignStudents(Number(editTeamForm.teamId), {
      studentIds: parseNumberList(membershipForm.studentIds)
    })
    applyTeam(response)
    setSuccess('Students assigned to team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleRemoveStudent() {
  try {
    const response = await removeStudentFromTeam(Number(editTeamForm.teamId), Number(membershipForm.removeStudentId))
    applyTeam(response)
    setSuccess('Student removed from team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleAssignInstructors() {
  try {
    const response = await assignInstructors(Number(editTeamForm.teamId), {
      instructorIds: parseNumberList(membershipForm.instructorIds)
    })
    applyTeam(response)
    setSuccess('Instructors assigned to team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleRemoveInstructor() {
  try {
    const response = await removeInstructorFromTeam(
      Number(editTeamForm.teamId),
      Number(membershipForm.removeInstructorId)
    )
    applyTeam(response)
    setSuccess('Instructor removed from team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleInviteStudents() {
  try {
    const response = await inviteStudents({
      sectionId: Number(studentInvitation.sectionId),
      emails: splitEmails(studentInvitation.emails),
      message: studentInvitation.message
    })
    setSuccess(summarizeInvitationResult('Student', response), response)
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
    setSuccess(summarizeInvitationResult('Instructor', response), response)
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
    applyUser(response)
    setSuccess('User loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadUserById(userId) {
  try {
    userForm.id = String(userId)
    const response = await getUser(Number(userId))
    applyUser(response)
    setSuccess('User loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleUpdateUser() {
  try {
    const response = await updateUser(Number(userForm.id), {
      firstName: userForm.firstName,
      middleInitial: normalizeOptionalValue(userForm.middleInitial),
      lastName: userForm.lastName,
      email: userForm.email
    })
    applyUser(response)
    setSuccess('User account updated.', response)
  } catch (error) {
    setError(error)
  }
}

onMounted(loadReferenceData)
</script>
