<template>
  <section class="page">
    <header class="page__hero">
      <p class="eyebrow">Project Domain</p>
      <h2>Run the section, team, WAR, and reporting workflows from one place.</h2>
      <p class="page__intro">
        This workspace now covers the operational use cases behind section setup, team maintenance, weekly activity
        reporting, and peer-evaluation reporting.
      </p>
    </header>

    <article class="panel">
      <div class="panel__header">
        <h3>Reference Refresh</h3>
        <button class="button button--secondary" type="button" @click="loadReferenceData">Reload Project Data</button>
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
    </article>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Create Rubric</h3>
          <span class="chip">UC-1</span>
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
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Section Setup</h3>
          <span class="chip">UC-4 + UC-6</span>
        </div>
        <form class="form-grid" @submit.prevent="handleCreateSection">
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

        <form class="form-grid form-grid--inline" @submit.prevent="handleConfigureActiveWeeks">
          <label class="field">
            <span>Section ID</span>
            <input v-model="activeWeekForm.sectionId" placeholder="1" />
          </label>
          <label class="field field--full">
            <span>Inactive Week Start Dates</span>
            <textarea
              v-model="activeWeekForm.inactiveWeekStartDates"
              rows="3"
              placeholder="2026-12-21, 2026-12-28"
            />
          </label>
          <button class="button button--secondary" type="submit">Configure Active Weeks</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Section Maintenance</h3>
          <span class="chip">UC-5</span>
        </div>
        <form class="form-grid" @submit.prevent="handleUpdateSection">
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

        <div v-if="loadedSection" class="data-grid">
          <section class="data-card">
            <h4>Current Section Snapshot</h4>
            <ul class="stack-list">
              <li class="stack-list__item">
                <strong>{{ loadedSection.name }}</strong>
                <span>{{ loadedSection.rubricName }}</span>
                <p>{{ loadedSection.startDate }} to {{ loadedSection.endDate }}</p>
              </li>
            </ul>
          </section>

          <section class="data-card">
            <h4>Configured Weeks</h4>
            <ul class="stack-list">
              <li v-for="week in loadedSection.weeks" :key="week.id" class="stack-list__item">
                <strong>{{ week.weekStartDate }}</strong>
                <span>{{ week.active ? 'Active week' : 'Inactive week' }}</span>
              </li>
            </ul>
          </section>
        </div>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Team Setup</h3>
          <span class="chip">UC-9</span>
        </div>
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
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Team Maintenance</h3>
          <span class="chip">UC-10</span>
        </div>
        <form class="form-grid" @submit.prevent="handleUpdateTeam">
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

        <div v-if="loadedTeam" class="data-grid">
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

      <article class="panel">
        <div class="panel__header">
          <h3>Team Membership</h3>
          <span class="chip">UC-12 + UC-13 + UC-19 + UC-20</span>
        </div>
        <form class="form-grid" @submit.prevent="handleAssignStudents">
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamMembershipForm.teamId" />
          </label>
          <label class="field field--full">
            <span>Student IDs</span>
            <textarea v-model="teamMembershipForm.studentIds" rows="2" placeholder="12, 14, 15" />
          </label>
          <button class="button button--secondary" type="submit">Assign Students</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleRemoveStudent">
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamMembershipForm.teamId" />
          </label>
          <label class="field">
            <span>Student ID to Remove</span>
            <input v-model="teamMembershipForm.removeStudentId" />
          </label>
          <button class="button" type="submit">Remove Student</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleAssignInstructors">
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamMembershipForm.teamId" />
          </label>
          <label class="field field--full">
            <span>Instructor IDs</span>
            <textarea v-model="teamMembershipForm.instructorIds" rows="2" placeholder="3, 5" />
          </label>
          <button class="button button--secondary" type="submit">Assign Instructors</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleRemoveInstructor">
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamMembershipForm.teamId" />
          </label>
          <label class="field">
            <span>Instructor ID to Remove</span>
            <input v-model="teamMembershipForm.removeInstructorId" />
          </label>
          <button class="button" type="submit">Remove Instructor</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>WAR Activity</h3>
          <span class="chip">UC-27</span>
        </div>
        <form class="form-grid" @submit.prevent="handleSaveWarActivity">
          <label class="field">
            <span>Activity ID (leave blank to create)</span>
            <input v-model="warForm.activityId" placeholder="Optional for update" />
          </label>
          <label class="field">
            <span>Student ID</span>
            <input v-model="warForm.studentId" />
          </label>
          <label class="field">
            <span>Week Start Date</span>
            <input v-model="warForm.weekStartDate" type="date" />
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
            {{ warForm.activityId ? 'Update WAR Activity' : 'Create WAR Activity' }}
          </button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleDeleteWarActivity">
          <label class="field">
            <span>Activity ID</span>
            <input v-model="warDeleteForm.activityId" />
          </label>
          <button class="button button--secondary" type="submit">Delete WAR Activity</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Peer Evaluation Submission</h3>
          <span class="chip">UC-28</span>
        </div>
        <form class="form-grid" @submit.prevent="handleSubmitPeerEvaluation">
          <label class="field">
            <span>Author ID</span>
            <input v-model="peerEvaluationForm.authorId" />
          </label>
          <label class="field">
            <span>Week Start Date</span>
            <input v-model="peerEvaluationForm.weekStartDate" type="date" />
          </label>
          <label class="field field--full">
            <span>Submission JSON</span>
            <textarea v-model="peerEvaluationForm.payload" rows="12" />
          </label>
          <button class="button" type="submit">Submit Peer Evaluation</button>
        </form>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Team and Section Reports</h3>
          <span class="chip">UC-31 + UC-32</span>
        </div>
        <form class="form-grid" @submit.prevent="handleLoadTeamWarReport">
          <label class="field">
            <span>Team ID</span>
            <input v-model="teamWarReportForm.teamId" />
          </label>
          <label class="field">
            <span>Week Start Date</span>
            <input v-model="teamWarReportForm.weekStartDate" type="date" />
          </label>
          <button class="button button--secondary" type="submit">Load Team WAR Report</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleLoadSectionPeerReport">
          <label class="field">
            <span>Section ID</span>
            <input v-model="sectionPeerReportForm.sectionId" />
          </label>
          <label class="field">
            <span>Week Start Date</span>
            <input v-model="sectionPeerReportForm.weekStartDate" type="date" />
          </label>
          <button class="button button--secondary" type="submit">Load Section Peer Report</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Student Reports</h3>
          <span class="chip">UC-29 + UC-33 + UC-34</span>
        </div>
        <form class="form-grid" @submit.prevent="handleLoadStudentWarReport">
          <label class="field">
            <span>Student ID</span>
            <input v-model="studentWarReportForm.studentId" />
          </label>
          <label class="field">
            <span>From Week</span>
            <input v-model="studentWarReportForm.fromWeekStartDate" type="date" />
          </label>
          <label class="field">
            <span>To Week</span>
            <input v-model="studentWarReportForm.toWeekStartDate" type="date" />
          </label>
          <button class="button button--secondary" type="submit">Load Student WAR Report</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleLoadStudentSelfPeerReport">
          <label class="field">
            <span>Student ID</span>
            <input v-model="studentSelfPeerReportForm.studentId" />
          </label>
          <label class="field">
            <span>Week Start Date</span>
            <input v-model="studentSelfPeerReportForm.weekStartDate" type="date" />
          </label>
          <button class="button" type="submit">Load Self Peer Report</button>
        </form>

        <form class="form-grid form-grid--inline" @submit.prevent="handleLoadInstructorStudentPeerReport">
          <label class="field">
            <span>Student ID</span>
            <input v-model="instructorStudentPeerReportForm.studentId" />
          </label>
          <label class="field">
            <span>From Week</span>
            <input v-model="instructorStudentPeerReportForm.fromWeekStartDate" type="date" />
          </label>
          <label class="field">
            <span>To Week</span>
            <input v-model="instructorStudentPeerReportForm.toWeekStartDate" type="date" />
          </label>
          <button class="button" type="submit">Load Instructor Peer Report</button>
        </form>
      </article>
    </div>

    <article class="panel">
      <div class="panel__header">
        <h3>Latest Project Response</h3>
        <span class="chip">JSON Output</span>
      </div>
      <pre class="code-block">{{ latestResponse }}</pre>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  assignInstructors,
  assignStudents,
  configureActiveWeeks,
  createRubric,
  createSection,
  createTeam,
  createWarActivity,
  deleteWarActivity,
  getInstructorStudentPeerReport,
  getRubrics,
  getSection,
  getSectionPeerReport,
  getSections,
  getStudentSelfPeerReport,
  getStudentWarReport,
  getTeam,
  getTeams,
  getTeamWarReport,
  removeInstructorFromTeam,
  removeStudentFromTeam,
  submitPeerEvaluation,
  updateSection,
  updateTeam,
  updateWarActivity
} from '@/features/projects/services/projectService'

const rubrics = ref([])
const sections = ref([])
const teams = ref([])
const loadedSection = ref(null)
const loadedTeam = ref(null)
const statusMessage = ref('')
const errorMessage = ref('')
const latestResponse = ref('No project request has been sent yet.')

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

const editSectionForm = reactive({
  sectionId: '',
  name: '',
  startDate: '',
  endDate: '',
  rubricId: ''
})

const activeWeekForm = reactive({
  sectionId: '',
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

const teamMembershipForm = reactive({
  teamId: '',
  studentIds: '',
  instructorIds: '',
  removeStudentId: '',
  removeInstructorId: ''
})

const warForm = reactive({
  activityId: '',
  studentId: '',
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

const peerEvaluationForm = reactive({
  authorId: '',
  weekStartDate: '',
  payload: `[
  {
    "evaluateeId": 1,
    "publicComment": "Great work this week.",
    "privateComment": "Strong contributor.",
    "scores": [
      { "criterionId": 1, "score": 10 },
      { "criterionId": 2, "score": 9 }
    ]
  }
]`
})

const teamWarReportForm = reactive({
  teamId: '',
  weekStartDate: ''
})

const sectionPeerReportForm = reactive({
  sectionId: '',
  weekStartDate: ''
})

const studentWarReportForm = reactive({
  studentId: '',
  fromWeekStartDate: '',
  toWeekStartDate: ''
})

const studentSelfPeerReportForm = reactive({
  studentId: '',
  weekStartDate: ''
})

const instructorStudentPeerReportForm = reactive({
  studentId: '',
  fromWeekStartDate: '',
  toWeekStartDate: ''
})

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

function toWarPayload() {
  return {
    studentId: Number(warForm.studentId),
    weekStartDate: warForm.weekStartDate,
    category: warForm.category,
    plannedActivity: warForm.plannedActivity,
    description: warForm.description,
    plannedHours: Number(warForm.plannedHours),
    actualHours: Number(warForm.actualHours),
    status: warForm.status
  }
}

function setResponse(message, payload) {
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
  activeWeekForm.sectionId = String(section.id)
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
  teamMembershipForm.teamId = String(team.id)
}

async function loadReferenceData() {
  try {
    const [rubricData, sectionData, teamData] = await Promise.all([getRubrics(), getSections(), getTeams()])
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
    setResponse('Rubric created.', response)
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
    setResponse('Section created.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleLoadSection() {
  try {
    const response = await getSection(Number(editSectionForm.sectionId))
    applySection(response)
    setResponse('Section loaded.', response)
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
    setResponse('Section updated.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleConfigureActiveWeeks() {
  try {
    const response = await configureActiveWeeks(Number(activeWeekForm.sectionId), {
      inactiveWeekStartDates: parseDateList(activeWeekForm.inactiveWeekStartDates)
    })
    applySection(response)
    setResponse('Active weeks configured.', response)
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
    setResponse('Team created.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeam() {
  try {
    const response = await getTeam(Number(editTeamForm.teamId))
    applyTeam(response)
    setResponse('Team loaded.', response)
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
    setResponse('Team updated.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleAssignStudents() {
  try {
    const response = await assignStudents(Number(teamMembershipForm.teamId), {
      studentIds: parseNumberList(teamMembershipForm.studentIds)
    })
    applyTeam(response)
    setResponse('Students assigned to team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleRemoveStudent() {
  try {
    const response = await removeStudentFromTeam(
      Number(teamMembershipForm.teamId),
      Number(teamMembershipForm.removeStudentId)
    )
    applyTeam(response)
    setResponse('Student removed from team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleAssignInstructors() {
  try {
    const response = await assignInstructors(Number(teamMembershipForm.teamId), {
      instructorIds: parseNumberList(teamMembershipForm.instructorIds)
    })
    applyTeam(response)
    setResponse('Instructors assigned to team.', response)
    await loadReferenceData()
  } catch (error) {
    setError(error)
  }
}

async function handleRemoveInstructor() {
  try {
    const response = await removeInstructorFromTeam(
      Number(teamMembershipForm.teamId),
      Number(teamMembershipForm.removeInstructorId)
    )
    applyTeam(response)
    setResponse('Instructor removed from team.', response)
    await loadReferenceData()
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
    setResponse(warForm.activityId ? 'WAR activity updated.' : 'WAR activity created.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleDeleteWarActivity() {
  try {
    await deleteWarActivity(Number(warDeleteForm.activityId))
    setResponse('WAR activity deleted.', { activityId: Number(warDeleteForm.activityId), deleted: true })
  } catch (error) {
    setError(error)
  }
}

async function handleSubmitPeerEvaluation() {
  try {
    const evaluations = JSON.parse(peerEvaluationForm.payload)
    const response = await submitPeerEvaluation({
      authorId: Number(peerEvaluationForm.authorId),
      weekStartDate: peerEvaluationForm.weekStartDate,
      evaluations
    })
    setResponse('Peer evaluation submitted.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadTeamWarReport() {
  try {
    const response = await getTeamWarReport(Number(teamWarReportForm.teamId), teamWarReportForm.weekStartDate)
    setResponse('Team WAR report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadSectionPeerReport() {
  try {
    const response = await getSectionPeerReport(
      Number(sectionPeerReportForm.sectionId),
      sectionPeerReportForm.weekStartDate
    )
    setResponse('Section peer report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadStudentWarReport() {
  try {
    const response = await getStudentWarReport(
      Number(studentWarReportForm.studentId),
      studentWarReportForm.fromWeekStartDate,
      studentWarReportForm.toWeekStartDate
    )
    setResponse('Student WAR report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadStudentSelfPeerReport() {
  try {
    const response = await getStudentSelfPeerReport(
      Number(studentSelfPeerReportForm.studentId),
      studentSelfPeerReportForm.weekStartDate
    )
    setResponse('Student self peer report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

async function handleLoadInstructorStudentPeerReport() {
  try {
    const response = await getInstructorStudentPeerReport(
      Number(instructorStudentPeerReportForm.studentId),
      instructorStudentPeerReportForm.fromWeekStartDate,
      instructorStudentPeerReportForm.toWeekStartDate
    )
    setResponse('Instructor student peer report loaded.', response)
  } catch (error) {
    setError(error)
  }
}

onMounted(loadReferenceData)
</script>
