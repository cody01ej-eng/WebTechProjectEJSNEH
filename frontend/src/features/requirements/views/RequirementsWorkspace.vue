<template>
  <section class="page">
    <header class="page__hero">
      <p class="eyebrow">Requirements Workspace</p>
      <h2>Track what the code is supposed to prove.</h2>
      <p class="page__intro">
        Phase 2 adds requirement references and traceability links so implementation can stay connected to the source
        documents while the app grows.
      </p>
    </header>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Source-of-Truth Docs</h3>
          <span class="chip">Read First</span>
        </div>
        <ul class="detail-list">
          <li v-for="doc in referenceDocs" :key="doc.path">
            <strong>{{ doc.title }}</strong>
            <span>{{ doc.path }}</span>
            <p>{{ doc.focus }}</p>
          </li>
        </ul>
      </article>

      <article class="panel panel--accent">
        <div class="panel__header">
          <h3>Implementation Guardrails</h3>
          <span class="chip">Phase 2</span>
        </div>
        <ul class="check-list">
          <li v-for="rule in implementationRules" :key="rule">{{ rule }}</li>
        </ul>
      </article>
    </div>

    <div class="panel-grid">
      <article class="panel">
        <div class="panel__header">
          <h3>Create Requirement Reference</h3>
          <span class="chip">Requirement Module</span>
        </div>

        <form class="form-grid" @submit.prevent="handleCreateReference">
          <label class="field">
            <span>Reference Key</span>
            <input v-model="referenceForm.referenceKey" placeholder="UC-27" />
          </label>
          <label class="field">
            <span>Title</span>
            <input v-model="referenceForm.title" placeholder="Manage activities in a weekly activity report" />
          </label>
          <label class="field">
            <span>Source</span>
            <select v-model="referenceForm.source">
              <option value="USE_CASE">Use Case</option>
              <option value="VISION_SCOPE">Vision and Scope</option>
              <option value="GLOSSARY">Glossary</option>
              <option value="BUSINESS_RULE">Business Rule</option>
              <option value="ENHANCEMENT">Enhancement</option>
            </select>
          </label>
          <label class="field field--full">
            <span>Summary</span>
            <textarea
              v-model="referenceForm.summary"
              rows="4"
              placeholder="Brief requirement summary for traceability."
            />
          </label>
          <button class="button" type="submit">Create Requirement</button>
        </form>
      </article>

      <article class="panel">
        <div class="panel__header">
          <h3>Create Traceability Link</h3>
          <span class="chip">Implementation Map</span>
        </div>

        <form class="form-grid" @submit.prevent="handleCreateLink">
          <label class="field">
            <span>Requirement</span>
            <select v-model="linkForm.requirementId">
              <option value="">Select a requirement</option>
              <option v-for="item in requirementReferences" :key="item.id" :value="item.id">
                {{ item.referenceKey }} - {{ item.title }}
              </option>
            </select>
          </label>
          <label class="field">
            <span>Component Type</span>
            <select v-model="linkForm.componentType">
              <option value="BACKEND_SERVICE">Backend Service</option>
              <option value="BACKEND_CONTROLLER">Backend Controller</option>
              <option value="BACKEND_REPOSITORY">Backend Repository</option>
              <option value="FRONTEND_VIEW">Frontend View</option>
              <option value="FRONTEND_SERVICE">Frontend Service</option>
              <option value="DATABASE">Database</option>
              <option value="TEST">Test</option>
            </select>
          </label>
          <label class="field">
            <span>Component Name</span>
            <input v-model="linkForm.componentName" placeholder="project.service.WarService" />
          </label>
          <label class="field">
            <span>Status</span>
            <select v-model="linkForm.status">
              <option value="PLANNED">Planned</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="IMPLEMENTED">Implemented</option>
            </select>
          </label>
          <label class="field field--full">
            <span>Notes</span>
            <textarea
              v-model="linkForm.notes"
              rows="4"
              placeholder="Explain how this code or test satisfies the requirement."
            />
          </label>
          <button class="button" type="submit">Create Link</button>
        </form>
      </article>
    </div>

    <article class="panel">
      <div class="panel__header">
        <h3>Saved Traceability Data</h3>
        <button class="button button--secondary" type="button" @click="loadData">Refresh</button>
      </div>

      <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
      <p v-if="errorMessage" class="status status--error">{{ errorMessage }}</p>

      <div class="data-grid">
        <section class="data-card">
          <h4>Requirement References</h4>
          <ul class="stack-list">
            <li v-for="item in requirementReferences" :key="item.id" class="stack-list__item">
              <strong>{{ item.referenceKey }}</strong>
              <span>{{ item.title }}</span>
              <p>{{ item.summary }}</p>
            </li>
          </ul>
        </section>

        <section class="data-card">
          <h4>Traceability Links</h4>
          <ul class="stack-list">
            <li v-for="item in traceabilityLinks" :key="item.id" class="stack-list__item">
              <strong>{{ item.requirementKey }}</strong>
              <span>{{ item.componentType }} - {{ item.componentName }}</span>
              <p>{{ item.notes }}</p>
            </li>
          </ul>
        </section>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { implementationRules, referenceDocs } from '@/features/requirements/data/referenceDocs'
import {
  createRequirementReference,
  createTraceabilityLink,
  getRequirementReferences,
  getTraceabilityLinks
} from '@/features/requirements/services/traceabilityService'

const requirementReferences = ref([])
const traceabilityLinks = ref([])
const statusMessage = ref('')
const errorMessage = ref('')

const referenceForm = reactive({
  referenceKey: '',
  title: '',
  source: 'USE_CASE',
  summary: ''
})

const linkForm = reactive({
  requirementId: '',
  componentType: 'BACKEND_SERVICE',
  componentName: '',
  status: 'PLANNED',
  notes: ''
})

async function loadData() {
  statusMessage.value = ''
  errorMessage.value = ''
  try {
    const [references, links] = await Promise.all([
      getRequirementReferences(),
      getTraceabilityLinks()
    ])
    requirementReferences.value = references
    traceabilityLinks.value = links
  } catch (error) {
    errorMessage.value = error.message
  }
}

async function handleCreateReference() {
  statusMessage.value = ''
  errorMessage.value = ''
  try {
    await createRequirementReference({
      referenceKey: referenceForm.referenceKey,
      title: referenceForm.title,
      source: referenceForm.source,
      summary: referenceForm.summary
    })
    statusMessage.value = 'Requirement reference created.'
    referenceForm.referenceKey = ''
    referenceForm.title = ''
    referenceForm.summary = ''
    await loadData()
    statusMessage.value = 'Requirement reference created.'
  } catch (error) {
    errorMessage.value = error.message
  }
}

async function handleCreateLink() {
  statusMessage.value = ''
  errorMessage.value = ''
  try {
    await createTraceabilityLink({
      requirementId: Number(linkForm.requirementId),
      componentType: linkForm.componentType,
      componentName: linkForm.componentName,
      status: linkForm.status,
      notes: linkForm.notes
    })
    statusMessage.value = 'Traceability link created.'
    linkForm.requirementId = ''
    linkForm.componentName = ''
    linkForm.notes = ''
    await loadData()
    statusMessage.value = 'Traceability link created.'
  } catch (error) {
    errorMessage.value = error.message
  }
}

onMounted(loadData)
</script>
