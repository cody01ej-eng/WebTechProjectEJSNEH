<template>
  <section class="page">
    <header class="page__hero">
      <p class="eyebrow">Student Workspace</p>
      <h2>Capture weekly progress, submit peer feedback, and keep your profile current.</h2>
      <p class="page__intro">
        This dashboard reflects the implemented student-facing use cases after sign-in: profile updates, weekly
        activity management, guided peer-evaluation submission, team WAR visibility, and self-only peer feedback.
      </p>
    </header>

    <article class="panel">
      <div class="panel__header">
        <h3>Student Status</h3>
        <span class="chip">UC-25 through UC-29, UC-32</span>
      </div>
      <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
      <p v-if="errorMessage" class="status status--error">{{ errorMessage }}</p>
      <p class="muted-note">
        Signed-in students can only update their own profile, submit their own WAR entries, and view their own peer
        feedback. Instructor-only drilldown reports remain outside this workspace.
      </p>
    </article>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Weekly Checklist</h3>
          <span class="chip">Protected Flow</span>
        </div>
        <ul class="detail-list">
          <li>
            <strong>Profile</strong>
            <span>UC-26</span>
            <p>Review your account details and keep contact information aligned with the roster.</p>
          </li>
          <li>
            <strong>WAR Activity</strong>
            <span>UC-27</span>
            <p>Log planned and completed work for the guided active week using your signed-in student account.</p>
          </li>
          <li>
            <strong>Peer Feedback</strong>
            <span>UC-28 and UC-29</span>
            <p>Score each teammate against the section rubric and load only your own published feedback history.</p>
          </li>
        </ul>

        <div v-if="workspaceContext" class="data-grid">
          <section class="data-card">
            <h4>Guided Defaults</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>Section</strong>
                <span>{{ workspaceContext.sectionName || 'No section assigned' }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Team</strong>
                <span>{{ workspaceContext.teamName || 'No team assigned' }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Peer Rubric</strong>
                <span>{{ workspaceContext.rubricName || 'No rubric assigned' }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Suggested WAR Week</strong>
                <span>{{ workspaceContext.suggestedWarWeekStartDate || 'Not available' }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Suggested Peer Evaluation Week</strong>
                <span>{{ workspaceContext.suggestedPeerEvaluationWeekStartDate || 'Not available' }}</span>
              </li>
            </ul>
          </section>

          <section class="data-card">
            <div class="panel__header">
              <h4>Active Weeks</h4>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadWorkspaceContext">
                Refresh Defaults
              </button>
            </div>
            <div v-if="activeWeekOptions.length" class="chip-row">
              <span v-for="week in activeWeekOptions" :key="week" class="chip">{{ week }}</span>
            </div>
            <p v-else class="muted-note">No active weeks are configured for your section yet.</p>
          </section>
        </div>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>My Profile</h3>
          <span class="chip">UC-26</span>
        </div>
        <p class="muted-note">
          Signed in as {{ workspaceContext?.studentName || formatUserName(loadedUser) || formatUserName(session.user.value) }}.
          Use refresh if your roster or assignment changed.
        </p>
        <form class="form-grid" @submit.prevent="handleUpdateUser">
          <div class="field field--full">
            <button class="button button--secondary" type="button" @click="handleLoadUser">Refresh Profile</button>
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
          <button class="button" type="submit">Save Profile Changes</button>
        </form>

        <div class="data-grid" v-if="loadedUser">
          <section class="data-card">
            <h4>Current Enrollment</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>{{ formatUserName(loadedUser) }}</strong>
                <span>{{ loadedUser.status }}</span>
                <p>{{ loadedUser.email }}</p>
              </li>
              <li class="stack-list__item" v-if="loadedUser.sectionName">
                <strong>Section</strong>
                <span>{{ loadedUser.sectionName }}</span>
              </li>
              <li class="stack-list__item" v-if="loadedUser.assignedTeamName">
                <strong>Assigned Team</strong>
                <span>{{ loadedUser.assignedTeamName }}</span>
              </li>
            </ul>
          </section>
        </div>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Weekly Activity Report</h3>
          <span class="chip">UC-27</span>
        </div>
        <p class="muted-note">
          WAR entries are submitted under your signed-in account for {{ workspaceContext?.teamName || 'your team' }}.
        </p>
        <form class="form-grid" @submit.prevent="handleSaveWarActivity">
          <label class="field">
            <span>Activity ID (leave blank to create)</span>
            <input v-model="warForm.activityId" placeholder="Optional for update" />
          </label>
          <label class="field">
            <span>Week Start Date</span>
            <select v-if="activeWeekOptions.length" v-model="warForm.weekStartDate">
              <option value="">Select an active week</option>
              <option v-for="week in activeWeekOptions" :key="week" :value="week">{{ week }}</option>
            </select>
            <input v-else v-model="warForm.weekStartDate" type="date" />
          </label>
          <label class="field">
            <span>Category</span>
            <select v-model="warForm.category">
              <option value="DEVELOPMENT">Development</option>
              <option value="TESTING">Testing</option>
              <option value="BUGFIX">Bugfix</option>
              <option value="COMMUNICATION">Communication</option>
              <option value="DOCUMENTATION">Documentation</option>
              <option value="DESIGN">Design</option>
              <option value="PLANNING">Planning</option>
              <option value="LEARNING">Learning</option>
              <option value="DEPLOYMENT">Deployment</option>
              <option value="SUPPORT">Support</option>
              <option value="MISCELLANEOUS">Miscellaneous</option>
            </select>
          </label>
          <label class="field">
            <span>Status</span>
            <select v-model="warForm.status">
              <option value="IN_PROGRESS">In Progress</option>
              <option value="UNDER_TESTING">Under Testing</option>
              <option value="DONE">Done</option>
            </select>
          </label>
          <label class="field field--full">
            <span>Planned Activity</span>
            <input v-model="warForm.plannedActivity" />
          </label>
          <label class="field field--full">
            <span>Description</span>
            <textarea v-model="warForm.description" rows="3" />
          </label>
          <label class="field">
            <span>Planned Hours</span>
            <input v-model="warForm.plannedHours" type="number" min="0" step="0.5" />
          </label>
          <label class="field">
            <span>Actual Hours</span>
            <input v-model="warForm.actualHours" type="number" min="0" step="0.5" />
          </label>
          <button class="button" type="submit">
            {{ warForm.activityId ? 'Update Activity' : 'Add Activity' }}
          </button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleDeleteWarActivity">
          <label class="field">
            <span>Activity ID</span>
            <input v-model="warDeleteForm.activityId" />
          </label>
          <button class="button button--ghost" type="submit">Delete Activity</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Team WAR View</h3>
          <span class="chip">UC-32</span>
        </div>
        <p class="muted-note">
          Team WAR visibility is tied to {{ workspaceContext?.teamName || 'your assigned team' }}.
        </p>
        <form class="form-grid" @submit.prevent="handleLoadTeamWarReport">
          <label class="field">
            <span>Week Start Date</span>
            <select v-if="activeWeekOptions.length" v-model="teamWarReportForm.weekStartDate">
              <option value="">Select an active week</option>
              <option v-for="week in activeWeekOptions" :key="week" :value="week">{{ week }}</option>
            </select>
            <input v-else v-model="teamWarReportForm.weekStartDate" type="date" />
          </label>
          <button class="button button--secondary" type="submit">Load Team WAR Report</button>
        </form>

        <div class="data-grid" v-if="teamWarReport">
          <section class="data-card">
            <h4>{{ teamWarReport.teamName }}</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>Week</strong>
                <span>{{ teamWarReport.weekStartDate }}</span>
              </li>
              <li class="stack-list__item" v-if="teamWarReport.missingStudents.length">
                <strong>Missing Students</strong>
                <span>{{ teamWarReport.missingStudents.join(', ') }}</span>
              </li>
            </ul>
          </section>
          <section class="data-card">
            <h4>Submitted Activity Groups</h4>
            <ul class="stack-list">
              <li v-for="student in teamWarReport.students" :key="student.studentId" class="stack-list__item">
                <strong>{{ student.studentName }}</strong>
                <span>{{ student.activities.length }} activities</span>
              </li>
            </ul>
          </section>
        </div>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Peer Evaluation Submission</h3>
          <span class="chip">UC-28</span>
        </div>
        <form class="form-grid" @submit.prevent="handleSubmitPeerEvaluation">
          <label class="field">
            <span>Previous Week Start Date</span>
            <select v-if="peerEvaluationWeekOptions.length" v-model="peerEvaluationForm.weekStartDate">
              <option value="">Select a prior active week</option>
              <option v-for="week in peerEvaluationWeekOptions" :key="week" :value="week">{{ week }}</option>
            </select>
            <input v-else v-model="peerEvaluationForm.weekStartDate" type="date" />
          </label>

          <div v-if="!workspaceContext" class="status">
            Guided peer evaluation fields will appear once the student workspace context loads.
          </div>
          <div v-else-if="!peerEvaluationEntries.length" class="status">
            No teammates are currently assigned for peer evaluation in your team.
          </div>
          <div v-else class="subform-list">
            <article
              v-for="entry in peerEvaluationEntries"
              :key="entry.evaluateeId"
              :data-testid="`peer-entry-${entry.evaluateeId}`"
              class="subform-card"
            >
              <div class="panel__header">
                <h4>{{ entry.teammateName }}</h4>
                <span class="chip">{{ entry.scores.length }} rubric score(s)</span>
              </div>

              <div class="criterion-grid">
                <section
                  v-for="score in entry.scores"
                  :key="`${entry.evaluateeId}-${score.criterionId}`"
                  class="criterion-card"
                >
                  <strong>{{ score.criterionName }}</strong>
                  <p>{{ score.description }}</p>
                  <label class="field">
                    <span>{{ score.criterionName }} score</span>
                    <input v-model="score.value" type="number" min="0" :max="score.maxScore" step="1" />
                  </label>
                </section>
              </div>

              <label class="field field--full">
                <span>Public Comment</span>
                <textarea v-model="entry.publicComment" rows="3" />
              </label>
              <label class="field field--full">
                <span>Private Comment</span>
                <textarea v-model="entry.privateComment" rows="3" />
              </label>
            </article>
          </div>

          <button class="button" type="submit">Submit Peer Evaluation</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>My Peer Feedback</h3>
          <span class="chip">UC-29</span>
        </div>
        <p class="muted-note">
          Load your published feedback by week without re-entering your student ID.
        </p>
        <form class="form-grid" @submit.prevent="handleLoadSelfPeerReport">
          <label class="field">
            <span>Week Start Date</span>
            <select v-if="peerEvaluationWeekOptions.length" v-model="selfPeerReportForm.weekStartDate">
              <option value="">Select a prior active week</option>
              <option v-for="week in peerEvaluationWeekOptions" :key="week" :value="week">{{ week }}</option>
            </select>
            <input v-else v-model="selfPeerReportForm.weekStartDate" type="date" />
          </label>
          <button class="button button--secondary" type="submit">Load My Report</button>
        </form>

        <div class="data-grid" v-if="selfPeerReport">
          <section class="data-card">
            <h4>{{ selfPeerReport.studentName }}</h4>
            <ul class="stack-list">
              <li
                v-for="week in selfPeerReport.weeks"
                :key="week.weekStartDate"
                class="stack-list__item"
              >
                <strong>{{ week.weekStartDate }}</strong>
                <span>Average grade: {{ week.averageGrade ?? 'N/A' }}</span>
                <p>{{ week.publicComments.join(' | ') || 'No public comments yet.' }}</p>
              </li>
            </ul>
          </section>
        </div>
      </article>
    </div>

    <article class="panel">
      <div class="panel__header">
        <h3>Latest Student Response</h3>
        <span class="chip">JSON Output</span>
      </div>
      <pre class="code-block">{{ latestResponse }}</pre>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getUser, updateUser } from '@/features/auth/services/authService'
import { useSessionStore } from '@/features/auth/stores/sessionStore'
import {
  createWarActivity,
  deleteWarActivity,
  getStudentSelfPeerReport,
  getStudentWorkspaceContext,
  getTeamWarReport,
  submitPeerEvaluation,
  updateWarActivity
} from '@/features/projects/services/projectService'

const session = useSessionStore()
const statusMessage = ref('')
const errorMessage = ref('')
const latestResponse = ref('No student request has been sent yet.')
const loadedUser = ref(null)
const workspaceContext = ref(null)
const teamWarReport = ref(null)
const selfPeerReport = ref(null)
const peerEvaluationEntries = ref([])

const userForm = reactive({
  id: '',
  firstName: '',
  middleInitial: '',
  lastName: '',
  email: ''
})

const warForm = reactive({
  activityId: '',
  weekStartDate: '',
  category: 'DEVELOPMENT',
  plannedActivity: '',
  description: '',
  plannedHours: '1',
  actualHours: '1',
  status: 'IN_PROGRESS'
})

const warDeleteForm = reactive({
  activityId: ''
})

const teamWarReportForm = reactive({
  teamId: '',
  weekStartDate: ''
})

const peerEvaluationForm = reactive({
  weekStartDate: ''
})

const selfPeerReportForm = reactive({
  weekStartDate: ''
})

const activeWeekOptions = computed(() => workspaceContext.value?.activeWeeks ?? [])

const peerEvaluationWeekOptions = computed(() => {
  const context = workspaceContext.value
  if (!context) {
    return []
  }

  const limit = context.suggestedWarWeekStartDate ?? null
  return (context.activeWeeks ?? []).filter((week) => !limit || week <= limit)
})

function buildValidationError(message) {
  const error = new Error(message)
  error.status = 400
  return error
}

function normalizeOptionalValue(value) {
  const trimmed = value?.trim?.() ?? value
  return trimmed === '' ? null : trimmed
}

function formatUserName(user) {
  if (!user) {
    return ''
  }
  return [user.firstName, user.middleInitial, user.lastName].filter(Boolean).join(' ')
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

function currentStudentId() {
  return workspaceContext.value?.studentId ?? Number(userForm.id)
}

function currentTeamId() {
  return workspaceContext.value?.teamId ?? Number(teamWarReportForm.teamId)
}

function createPeerEvaluationEntries(context) {
  if (!context) {
    return []
  }

  return (context.teammates ?? [])
    .filter((teammate) => !teammate.self)
    .map((teammate) => ({
      evaluateeId: teammate.id,
      teammateName: teammate.name,
      publicComment: '',
      privateComment: '',
      scores: (context.rubricCriteria ?? []).map((criterion) => ({
        criterionId: criterion.id,
        criterionName: criterion.name,
        description: criterion.description,
        maxScore: Number(criterion.maxScore),
        value: ''
      }))
    }))
}

function applyUser(user) {
  loadedUser.value = user
  userForm.id = String(user.id)
  userForm.firstName = user.firstName
  userForm.middleInitial = user.middleInitial ?? ''
  userForm.lastName = user.lastName
  userForm.email = user.email
}

function applyWorkspaceContext(context) {
  workspaceContext.value = context
  warForm.weekStartDate = context.suggestedWarWeekStartDate ?? ''
  teamWarReportForm.teamId = context.teamId ? String(context.teamId) : ''
  teamWarReportForm.weekStartDate = context.suggestedWarWeekStartDate ?? ''
  peerEvaluationForm.weekStartDate = context.suggestedPeerEvaluationWeekStartDate ?? ''
  selfPeerReportForm.weekStartDate = context.suggestedPeerEvaluationWeekStartDate ?? ''
  peerEvaluationEntries.value = createPeerEvaluationEntries(context)
}

function toWarPayload() {
  const studentId = currentStudentId()
  if (!studentId) {
    throw buildValidationError('Student profile must be loaded before saving a WAR activity.')
  }
  if (!warForm.weekStartDate) {
    throw buildValidationError('Select a week before saving a WAR activity.')
  }

  return {
    studentId,
    weekStartDate: warForm.weekStartDate,
    category: warForm.category,
    plannedActivity: warForm.plannedActivity,
    description: warForm.description,
    plannedHours: Number(warForm.plannedHours),
    actualHours: Number(warForm.actualHours),
    status: warForm.status
  }
}

function toPeerEvaluationPayload() {
  const authorId = currentStudentId()
  if (!authorId) {
    throw buildValidationError('Student workspace context must be loaded before submitting peer feedback.')
  }
  if (!peerEvaluationForm.weekStartDate) {
    throw buildValidationError('Select a prior week before submitting peer feedback.')
  }
  if (!peerEvaluationEntries.value.length) {
    throw buildValidationError('No teammates are currently available for peer evaluation.')
  }

  return {
    authorId,
    weekStartDate: peerEvaluationForm.weekStartDate,
    evaluations: peerEvaluationEntries.value.map((entry) => ({
      evaluateeId: entry.evaluateeId,
      publicComment: normalizeOptionalValue(entry.publicComment),
      privateComment: normalizeOptionalValue(entry.privateComment),
      scores: entry.scores.map((score) => {
        if (score.value === '' || score.value === null) {
          throw buildValidationError(`Enter a score for ${score.criterionName} for ${entry.teammateName}.`)
        }

        const numericScore = Number(score.value)
        if (!Number.isFinite(numericScore)) {
          throw buildValidationError(`Score for ${score.criterionName} for ${entry.teammateName} must be a number.`)
        }
        if (numericScore < 0 || numericScore > score.maxScore) {
          throw buildValidationError(
            `Score for ${score.criterionName} for ${entry.teammateName} must be between 0 and ${score.maxScore}.`
          )
        }

        return {
          criterionId: score.criterionId,
          score: numericScore
        }
      })
    }))
  }
}

async function handleLoadUser() {
  try {
    const studentId = currentStudentId()
    if (!studentId) {
      throw buildValidationError('Student profile cannot be refreshed until the session is loaded.')
    }

    const response = await getUser(studentId)
    applyUser(response)
    setSuccess('Student profile loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadWorkspaceContext() {
  try {
    const response = await getStudentWorkspaceContext()
    applyWorkspaceContext(response)
    setSuccess('Student workspace defaults loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleUpdateUser() {
  try {
    const studentId = currentStudentId()
    if (!studentId) {
      throw buildValidationError('Student profile must be loaded before saving changes.')
    }

    const response = await updateUser(studentId, {
      firstName: userForm.firstName,
      middleInitial: normalizeOptionalValue(userForm.middleInitial),
      lastName: userForm.lastName,
      email: userForm.email
    })
    applyUser(response)
    setSuccess('Student profile updated.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleSaveWarActivity() {
  try {
    const payload = toWarPayload()
    const response = warForm.activityId
      ? await updateWarActivity(Number(warForm.activityId), payload)
      : await createWarActivity(payload)
    setSuccess(warForm.activityId ? 'WAR activity updated.' : 'WAR activity created.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleDeleteWarActivity() {
  try {
    if (!warDeleteForm.activityId) {
      throw buildValidationError('Enter an activity ID before deleting a WAR entry.')
    }

    await deleteWarActivity(Number(warDeleteForm.activityId))
    setSuccess('WAR activity deleted.', { activityId: Number(warDeleteForm.activityId), deleted: true })
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeamWarReport() {
  try {
    const teamId = currentTeamId()
    if (!teamId) {
      throw buildValidationError('A team assignment is required before loading the team WAR report.')
    }
    if (!teamWarReportForm.weekStartDate) {
      throw buildValidationError('Select a week before loading the team WAR report.')
    }

    const response = await getTeamWarReport(teamId, teamWarReportForm.weekStartDate)
    teamWarReport.value = response
    setSuccess('Team WAR report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleSubmitPeerEvaluation() {
  try {
    const response = await submitPeerEvaluation(toPeerEvaluationPayload())
    peerEvaluationEntries.value = createPeerEvaluationEntries(workspaceContext.value)
    setSuccess('Peer evaluation submitted.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadSelfPeerReport() {
  try {
    const studentId = currentStudentId()
    if (!studentId) {
      throw buildValidationError('Student workspace context must be loaded before requesting your peer feedback.')
    }
    if (!selfPeerReportForm.weekStartDate) {
      throw buildValidationError('Select a week before loading your peer feedback.')
    }

    const response = await getStudentSelfPeerReport(studentId, selfPeerReportForm.weekStartDate)
    selfPeerReport.value = response
    setSuccess('Student self report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function initializeWorkspace() {
  const currentUser = session.user.value
  if (!currentUser) {
    return
  }

  applyUser(currentUser)

  try {
    const [userResponse, contextResponse] = await Promise.all([
      getUser(currentUser.id),
      getStudentWorkspaceContext()
    ])
    applyUser(userResponse)
    applyWorkspaceContext(contextResponse)
    setSuccess('Student workspace loaded.', {
      user: userResponse,
      context: contextResponse
    })
  } catch (error) {
    setError(error)
  }
}

onMounted(initializeWorkspace)
</script>
