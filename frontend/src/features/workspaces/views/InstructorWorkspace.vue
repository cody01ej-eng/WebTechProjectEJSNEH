<template>
  <section class="page">
    <header class="page__hero">
      <p class="eyebrow">Instructor Workspace</p>
      <h2>Review teams, inspect students, and generate the reports that drive feedback.</h2>
      <p class="page__intro">
        This dashboard focuses on the implemented instructor use cases after sign-in: team and student lookup, section
        peer-evaluation reporting, team WAR review, and student-level performance reporting over time.
      </p>
    </header>

    <article class="panel">
      <div class="panel__header">
        <h3>Instructor Status</h3>
        <span class="chip">UC-7, UC-8, UC-15, UC-16, UC-30 through UC-34</span>
      </div>
      <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
      <p v-if="errorMessage" class="status status--error">{{ errorMessage }}</p>
      <p class="muted-note">
        Instructor reporting spans section-wide peer trends, team WAR coverage, and student-by-student drilldowns over
        an active-week range.
      </p>
    </article>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Instructor Toolkit</h3>
          <span class="chip">UC-30 through UC-34</span>
        </div>
        <p class="muted-note">
          Signed in as {{ instructorName }}. Use directory results below to prefill report actions instead of retyping
          IDs into every form.
        </p>

        <div class="stat-row">
          <section class="stat-card">
            <strong>{{ supervisedTeamCount }}</strong>
            <span>Supervised team{{ supervisedTeamCount === 1 ? '' : 's' }}</span>
          </section>
          <section class="stat-card">
            <strong>{{ selectedTeamLabel }}</strong>
            <span>Current team focus</span>
          </section>
          <section class="stat-card">
            <strong>{{ selectedStudentLabel }}</strong>
            <span>Current student focus</span>
          </section>
        </div>

        <div v-if="instructorProfile?.supervisedTeams?.length" class="chip-row">
          <span v-for="teamName in instructorProfile.supervisedTeams" :key="teamName" class="chip">{{ teamName }}</span>
        </div>

        <ul class="detail-list">
          <li>
            <strong>Directory Review</strong>
            <span>UC-7, UC-8, UC-15, UC-16</span>
            <p>Search teams and students, then promote the selected record into the guided report actions below.</p>
          </li>
          <li>
            <strong>Section Monitoring</strong>
            <span>UC-31 and UC-32</span>
            <p>Use one shared snapshot week for both section peer health and team WAR coverage.</p>
          </li>
          <li>
            <strong>Student Deep Dive</strong>
            <span>UC-33 and UC-34</span>
            <p>Keep one shared date window for peer and WAR trend reports on the selected student.</p>
          </li>
        </ul>
      </article>

      <article class="panel panel--accent">
        <div class="panel__header">
          <h3>Reporting Window</h3>
          <span class="chip">Shared Filters</span>
        </div>
        <form class="form-grid" @submit.prevent>
          <label class="field">
            <span>Snapshot Week</span>
            <input v-model="reportWindow.snapshotWeekStartDate" type="date" />
          </label>
          <label class="field">
            <span>From Week</span>
            <input v-model="reportWindow.fromWeekStartDate" type="date" />
          </label>
          <label class="field">
            <span>To Week</span>
            <input v-model="reportWindow.toWeekStartDate" type="date" />
          </label>
          <div class="field field--full">
            <button class="button button--secondary" type="button" @click="resetReportWindow">
              Reset To Current Window
            </button>
          </div>
        </form>
        <p class="muted-note">
          The snapshot week powers section and team report actions. The date window powers student peer and WAR trend
          reports.
        </p>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Team Directory</h3>
          <span class="chip">UC-7, UC-8, UC-32</span>
        </div>
        <form class="form-grid" @submit.prevent="handleSearchTeams">
          <label class="field">
            <span>Section ID Filter</span>
            <input v-model="teamSearchForm.sectionId" placeholder="Optional" />
          </label>
          <button class="button button--secondary" type="submit">Find Teams</button>
        </form>

        <ul class="stack-list" v-if="teamResults.length">
          <li v-for="team in teamResults" :key="team.id" class="stack-list__item">
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
              <button class="button button--ghost button--compact" type="button" @click="handleLoadTeamWarReportFrom(team.id)">
                Load Team WAR Snapshot
              </button>
            </div>
          </li>
        </ul>

        <form class="form-grid form-grid--inline" @submit.prevent="handleLoadTeam">
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamLookupForm.teamId" placeholder="1" />
          </label>
          <button class="button" type="submit">Load Team Detail</button>
        </form>

        <div class="data-grid" v-if="loadedTeam">
          <section class="data-card">
            <div class="panel__header">
              <h4>{{ loadedTeam.name }}</h4>
              <span class="chip">Team {{ loadedTeam.id }}</span>
            </div>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>Section</strong>
                <span>{{ loadedTeam.sectionName }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Website</strong>
                <span>{{ loadedTeam.websiteUrl || 'No website listed' }}</span>
              </li>
            </ul>
            <div class="stack-list__actions">
              <button class="button button--secondary button--compact" type="button" @click="handleLoadTeamWarReport">
                Load Team WAR Snapshot
              </button>
            </div>
          </section>

          <section class="data-card">
            <h4>Roster</h4>
            <ul class="stack-list">
              <li v-for="student in loadedTeam.students" :key="student.id" class="stack-list__item">
                <strong>{{ student.name }}</strong>
                <span>Student {{ student.id }}</span>
                <p>{{ student.email }}</p>
                <div class="stack-list__actions">
                  <button class="button button--ghost button--compact" type="button" @click="handleLoadStudentById(student.id)">
                    Focus Student
                  </button>
                </div>
              </li>
            </ul>
          </section>
        </div>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Student Directory</h3>
          <span class="chip">UC-15, UC-16, UC-33, UC-34</span>
        </div>
        <form class="form-grid" @submit.prevent="handleSearchStudents">
          <label class="field">
            <span>Name Filter</span>
            <input v-model="studentSearchForm.name" placeholder="Doe" />
          </label>
          <button class="button button--secondary" type="submit">Find Students</button>
        </form>

        <ul class="stack-list" v-if="studentResults.length">
          <li v-for="student in studentResults" :key="student.id" class="stack-list__item">
            <strong>{{ formatUserName(student) }}</strong>
            <span>{{ student.sectionName || 'No section assigned' }}</span>
            <p>{{ student.assignedTeamName || 'No team assigned' }}</p>
            <div class="stack-list__actions">
              <button class="button button--ghost button--compact" type="button" @click="useStudent(student)">
                Use Student
              </button>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadStudentById(student.id)">
                Load Student Detail
              </button>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadStudentPeerReportFrom(student.id)">
                Load Peer Trend
              </button>
            </div>
          </li>
        </ul>

        <form class="form-grid form-grid--inline" @submit.prevent="handleLoadStudent">
          <label class="field">
            <span>Student ID</span>
            <input v-model="studentLookupForm.studentId" placeholder="7" />
          </label>
          <button class="button" type="submit">Load Student Detail</button>
        </form>

        <div class="data-grid" v-if="loadedStudent">
          <section class="data-card">
            <div class="panel__header">
              <h4>{{ formatUserName(loadedStudent) }}</h4>
              <span class="chip">Student {{ loadedStudent.id }}</span>
            </div>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>Status</strong>
                <span>{{ loadedStudent.status }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Section</strong>
                <span>{{ loadedStudent.sectionName || 'No section assigned' }}</span>
              </li>
              <li class="stack-list__item">
                <strong>Assigned Team</strong>
                <span>{{ loadedStudent.assignedTeamName || 'No team assigned' }}</span>
              </li>
            </ul>
            <div class="stack-list__actions">
              <button class="button button--secondary button--compact" type="button" @click="handleLoadStudentPeerReport">
                Load Peer Trend
              </button>
              <button class="button button--ghost button--compact" type="button" @click="handleLoadStudentWarReport">
                Load WAR Trend
              </button>
            </div>
          </section>
        </div>
      </article>
    </div>

    <article class="panel">
      <div class="panel__header">
        <h3>Guided Report Actions</h3>
        <span class="chip">Selected Focus</span>
      </div>

      <div class="data-grid">
        <section class="data-card">
          <div class="panel__header">
            <h4>Section Snapshot</h4>
            <span class="chip">{{ reportWindow.snapshotWeekStartDate || 'No week selected' }}</span>
          </div>
          <label class="field">
            <span>Section ID</span>
            <input v-model="sectionPeerReportForm.sectionId" placeholder="Select a student or enter a section ID" />
          </label>
          <div class="stack-list__actions">
            <button class="button button--secondary button--compact" type="button" @click="handleLoadSectionPeerReport">
              Load Section Peer Snapshot
            </button>
          </div>

          <div v-if="sectionPeerSummary" class="stat-row">
            <section class="stat-card">
              <strong>{{ sectionPeerSummary.studentCount }}</strong>
              <span>Students reviewed</span>
            </section>
            <section class="stat-card">
              <strong>{{ sectionPeerSummary.missingEvaluators }}</strong>
              <span>Missing evaluators</span>
            </section>
            <section class="stat-card">
              <strong>{{ sectionPeerSummary.averageGradeLabel }}</strong>
              <span>Average grade</span>
            </section>
          </div>
        </section>

        <section class="data-card">
          <div class="panel__header">
            <h4>Team Snapshot</h4>
            <span class="chip">{{ reportWindow.snapshotWeekStartDate || 'No week selected' }}</span>
          </div>
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamWarReportForm.teamId" placeholder="Select a team or student first" />
          </label>
          <div class="stack-list__actions">
            <button class="button button--secondary button--compact" type="button" @click="handleLoadTeamWarReport">
              Load Team WAR Snapshot
            </button>
          </div>

          <div v-if="teamWarSummary" class="stat-row">
            <section class="stat-card">
              <strong>{{ teamWarSummary.studentCount }}</strong>
              <span>Students submitted</span>
            </section>
            <section class="stat-card">
              <strong>{{ teamWarSummary.totalActivities }}</strong>
              <span>Total activities</span>
            </section>
            <section class="stat-card">
              <strong>{{ teamWarSummary.missingStudents }}</strong>
              <span>Missing students</span>
            </section>
          </div>
        </section>

        <section class="data-card">
          <div class="panel__header">
            <h4>Student Deep Dive</h4>
            <span class="chip">{{ selectedStudentLabel }}</span>
          </div>
          <label class="field">
            <span>Student ID</span>
            <input v-model="studentPeerReportForm.studentId" placeholder="Select a student first" />
          </label>
          <div class="stack-list__actions">
            <button class="button button--secondary button--compact" type="button" @click="handleLoadStudentPeerReport">
              Load Peer Trend
            </button>
            <button class="button button--ghost button--compact" type="button" @click="handleLoadStudentWarReport">
              Load WAR Trend
            </button>
          </div>

          <div v-if="studentPeerSummary || studentWarSummary" class="stat-row">
            <section class="stat-card">
              <strong>{{ studentPeerSummary?.weekCount ?? 0 }}</strong>
              <span>Peer report weeks</span>
            </section>
            <section class="stat-card">
              <strong>{{ studentWarSummary?.totalActivities ?? 0 }}</strong>
              <span>Total WAR activities</span>
            </section>
            <section class="stat-card">
              <strong>{{ studentPeerSummary?.latestAverageLabel ?? 'N/A' }}</strong>
              <span>Latest peer average</span>
            </section>
          </div>
        </section>
      </div>
    </article>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Section and Team Snapshots</h3>
          <span class="chip">UC-31, UC-32</span>
        </div>

        <div class="data-grid" v-if="sectionPeerReport || teamWarReport">
          <section class="data-card" v-if="sectionPeerReport">
            <h4>{{ sectionPeerReport.sectionName }}</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>Week</strong>
                <span>{{ sectionPeerReport.weekStartDate }}</span>
              </li>
              <li class="stack-list__item" v-if="sectionPeerReport.missingEvaluators.length">
                <strong>Missing Evaluators</strong>
                <span>{{ sectionPeerReport.missingEvaluators.join(', ') }}</span>
              </li>
              <li
                v-for="student in sectionPeerReport.students"
                :key="student.studentId"
                class="stack-list__item"
              >
                <strong>{{ student.studentName }}</strong>
                <span>Average grade: {{ student.averageGrade ?? 'N/A' }}</span>
                <p>{{ student.comments?.length ?? 0 }} comment thread(s)</p>
              </li>
            </ul>
          </section>

          <section class="data-card" v-if="teamWarReport">
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
              <li
                v-for="student in teamWarReport.students"
                :key="student.studentId"
                class="stack-list__item"
              >
                <strong>{{ student.studentName }}</strong>
                <span>{{ student.activities.length }} activities</span>
              </li>
            </ul>
          </section>
        </div>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Student Trend Reports</h3>
          <span class="chip">UC-33, UC-34</span>
        </div>

        <div class="data-grid" v-if="studentPeerReport || studentWarReport">
          <section class="data-card" v-if="studentPeerReport">
            <h4>{{ studentPeerReport.studentName }}</h4>
            <ul class="stack-list">
              <li
                v-for="week in studentPeerReport.weeks"
                :key="week.weekStartDate"
                class="stack-list__item"
              >
                <strong>{{ week.weekStartDate }}</strong>
                <span>Average grade: {{ week.averageGrade ?? 'N/A' }}</span>
                <p>{{ week.publicComments.join(' | ') || 'No public comments yet.' }}</p>
              </li>
            </ul>
          </section>

          <section class="data-card" v-if="studentWarReport">
            <h4>{{ studentWarReport.studentName }}</h4>
            <ul class="stack-list">
              <li
                v-for="week in studentWarReport.weeks"
                :key="week.weekStartDate"
                class="stack-list__item"
              >
                <strong>{{ week.weekStartDate }}</strong>
                <span>{{ week.activities.length }} activities</span>
              </li>
            </ul>
          </section>
        </div>
      </article>
    </div>

    <article class="panel">
      <div class="panel__header">
        <h3>Latest Instructor Response</h3>
        <span class="chip">JSON Output</span>
      </div>
      <pre class="code-block">{{ latestResponse }}</pre>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { findUsers, getUser } from '@/features/auth/services/authService'
import { useSessionStore } from '@/features/auth/stores/sessionStore'
import {
  getInstructorStudentPeerReport,
  getSectionPeerReport,
  getStudentWarReport,
  getTeam,
  getTeams,
  getTeamWarReport
} from '@/features/projects/services/projectService'

const session = useSessionStore()
const statusMessage = ref('')
const errorMessage = ref('')
const latestResponse = ref('No instructor request has been sent yet.')
const instructorProfile = ref(null)
const teamResults = ref([])
const studentResults = ref([])
const loadedTeam = ref(null)
const loadedStudent = ref(null)
const sectionPeerReport = ref(null)
const teamWarReport = ref(null)
const studentPeerReport = ref(null)
const studentWarReport = ref(null)

const teamSearchForm = reactive({
  sectionId: ''
})

const teamLookupForm = reactive({
  teamId: ''
})

const studentSearchForm = reactive({
  name: ''
})

const studentLookupForm = reactive({
  studentId: ''
})

const sectionPeerReportForm = reactive({
  sectionId: ''
})

const teamWarReportForm = reactive({
  teamId: ''
})

const studentPeerReportForm = reactive({
  studentId: ''
})

const studentWarReportForm = reactive({
  studentId: ''
})

const reportWindow = reactive({
  snapshotWeekStartDate: '',
  fromWeekStartDate: '',
  toWeekStartDate: ''
})

const instructorName = computed(() => formatUserName(instructorProfile.value || session.user.value) || 'your instructor account')
const supervisedTeamCount = computed(() => instructorProfile.value?.supervisedTeams?.length ?? session.user.value?.supervisedTeams?.length ?? 0)
const selectedTeamLabel = computed(() => loadedTeam.value?.name || loadedStudent.value?.assignedTeamName || 'None')
const selectedStudentLabel = computed(() => formatUserName(loadedStudent.value) || 'None')

const sectionPeerSummary = computed(() => {
  const report = sectionPeerReport.value
  if (!report) {
    return null
  }

  const gradedStudents = report.students.filter((student) => student.averageGrade !== null && student.averageGrade !== undefined)
  const averageGrade = gradedStudents.length
    ? gradedStudents.reduce((sum, student) => sum + Number(student.averageGrade), 0) / gradedStudents.length
    : null

  return {
    studentCount: report.students.length,
    missingEvaluators: report.missingEvaluators.length,
    averageGradeLabel: averageGrade === null ? 'N/A' : averageGrade.toFixed(2)
  }
})

const teamWarSummary = computed(() => {
  const report = teamWarReport.value
  if (!report) {
    return null
  }

  return {
    studentCount: report.students.length,
    totalActivities: report.students.reduce((sum, student) => sum + student.activities.length, 0),
    missingStudents: report.missingStudents.length
  }
})

const studentPeerSummary = computed(() => {
  const report = studentPeerReport.value
  if (!report) {
    return null
  }

  const latestWeek = [...report.weeks].sort((left, right) => right.weekStartDate.localeCompare(left.weekStartDate))[0]
  return {
    weekCount: report.weeks.length,
    latestAverageLabel: latestWeek?.averageGrade === null || latestWeek?.averageGrade === undefined
      ? 'N/A'
      : Number(latestWeek.averageGrade).toFixed(2)
  }
})

const studentWarSummary = computed(() => {
  const report = studentWarReport.value
  if (!report) {
    return null
  }

  return {
    weekCount: report.weeks.length,
    totalActivities: report.weeks.reduce((sum, week) => sum + week.activities.length, 0)
  }
})

function startOfWeekIso(offsetWeeks = 0) {
  const current = new Date()
  const dayIndex = current.getDay()
  const mondayOffset = (dayIndex + 6) % 7
  current.setHours(12, 0, 0, 0)
  current.setDate(current.getDate() - mondayOffset + offsetWeeks * 7)
  return current.toISOString().slice(0, 10)
}

function resetReportWindow() {
  reportWindow.snapshotWeekStartDate = startOfWeekIso(0)
  reportWindow.fromWeekStartDate = startOfWeekIso(-1)
  reportWindow.toWeekStartDate = startOfWeekIso(0)
}

function buildValidationError(message) {
  const error = new Error(message)
  error.status = 400
  return error
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

function syncStudentForms(studentId) {
  const value = String(studentId)
  studentLookupForm.studentId = value
  studentPeerReportForm.studentId = value
  studentWarReportForm.studentId = value
}

function useTeam(team) {
  loadedTeam.value = team
  teamLookupForm.teamId = String(team.id)
  teamWarReportForm.teamId = String(team.id)
  if (teamSearchForm.sectionId) {
    sectionPeerReportForm.sectionId = teamSearchForm.sectionId
  }
  setSuccess('Team selected from directory.', team)
}

function useStudent(student) {
  loadedStudent.value = student
  syncStudentForms(student.id)

  if (student.sectionId) {
    sectionPeerReportForm.sectionId = String(student.sectionId)
    teamSearchForm.sectionId = String(student.sectionId)
  }

  if (student.assignedTeamId) {
    teamWarReportForm.teamId = String(student.assignedTeamId)
    teamLookupForm.teamId = String(student.assignedTeamId)
  }

  setSuccess('Student selected from directory.', student)
}

async function handleSearchTeams() {
  try {
    const response = await getTeams(teamSearchForm.sectionId ? Number(teamSearchForm.sectionId) : undefined)
    teamResults.value = response
    setSuccess(`Loaded ${response.length} team record(s).`, response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeamById(teamId) {
  const response = await getTeam(Number(teamId))
  loadedTeam.value = response
  teamLookupForm.teamId = String(response.id)
  teamWarReportForm.teamId = String(response.id)
  if (teamSearchForm.sectionId) {
    sectionPeerReportForm.sectionId = teamSearchForm.sectionId
  }
  return response
}

async function handleLoadTeam() {
  try {
    const response = await handleLoadTeamById(teamLookupForm.teamId)
    setSuccess('Team loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleSearchStudents() {
  try {
    const response = await findUsers('STUDENT', studentSearchForm.name || undefined)
    studentResults.value = response
    setSuccess(`Loaded ${response.length} student record(s).`, response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadStudentById(studentId) {
  const response = await getUser(Number(studentId))
  useStudent(response)
  return response
}

async function handleLoadStudent() {
  try {
    const response = await handleLoadStudentById(studentLookupForm.studentId)
    setSuccess('Student loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadSectionPeerReport() {
  try {
    if (!sectionPeerReportForm.sectionId) {
      throw buildValidationError('Select or enter a section ID before loading the section snapshot.')
    }
    if (!reportWindow.snapshotWeekStartDate) {
      throw buildValidationError('Select a snapshot week before loading the section snapshot.')
    }

    const response = await getSectionPeerReport(
      Number(sectionPeerReportForm.sectionId),
      reportWindow.snapshotWeekStartDate
    )
    sectionPeerReport.value = response
    setSuccess('Section peer report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeamWarReportFrom(teamId) {
  try {
    teamWarReportForm.teamId = String(teamId)
    await handleLoadTeamWarReport()
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeamWarReport() {
  try {
    if (!teamWarReportForm.teamId) {
      throw buildValidationError('Select or enter a team ID before loading the team WAR snapshot.')
    }
    if (!reportWindow.snapshotWeekStartDate) {
      throw buildValidationError('Select a snapshot week before loading the team WAR snapshot.')
    }

    const response = await getTeamWarReport(Number(teamWarReportForm.teamId), reportWindow.snapshotWeekStartDate)
    teamWarReport.value = response
    setSuccess('Team WAR report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadStudentPeerReportFrom(studentId) {
  try {
    studentPeerReportForm.studentId = String(studentId)
    studentWarReportForm.studentId = String(studentId)
    await handleLoadStudentPeerReport()
  } catch (error) {
    setError(error)
  }
}

async function handleLoadStudentPeerReport() {
  try {
    if (!studentPeerReportForm.studentId) {
      throw buildValidationError('Select or enter a student ID before loading the peer trend report.')
    }
    if (!reportWindow.fromWeekStartDate || !reportWindow.toWeekStartDate) {
      throw buildValidationError('Choose a from week and to week before loading the peer trend report.')
    }

    const response = await getInstructorStudentPeerReport(
      Number(studentPeerReportForm.studentId),
      reportWindow.fromWeekStartDate,
      reportWindow.toWeekStartDate
    )
    studentPeerReport.value = response
    setSuccess('Student peer report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadStudentWarReport() {
  try {
    const studentId = studentWarReportForm.studentId || studentPeerReportForm.studentId
    if (!studentId) {
      throw buildValidationError('Select or enter a student ID before loading the WAR trend report.')
    }
    if (!reportWindow.fromWeekStartDate || !reportWindow.toWeekStartDate) {
      throw buildValidationError('Choose a from week and to week before loading the WAR trend report.')
    }

    studentWarReportForm.studentId = String(studentId)
    const response = await getStudentWarReport(
      Number(studentWarReportForm.studentId),
      reportWindow.fromWeekStartDate,
      reportWindow.toWeekStartDate
    )
    studentWarReport.value = response
    setSuccess('Student WAR report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

function initializeWorkspace() {
  resetReportWindow()

  const currentUser = session.user.value
  if (!currentUser) {
    return
  }

  instructorProfile.value = currentUser
  if (currentUser.sectionId) {
    teamSearchForm.sectionId = String(currentUser.sectionId)
    sectionPeerReportForm.sectionId = String(currentUser.sectionId)
  }
}

onMounted(initializeWorkspace)
</script>
