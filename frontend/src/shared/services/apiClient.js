const DEFAULT_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

let csrfLoadPromise = null
let csrfToken = null
let csrfHeaderName = 'X-CSRF-TOKEN'

function buildUrl(path, params) {
  const url = new URL(`${DEFAULT_BASE_URL}${path}`, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, value)
      }
    })
  }
  return url.toString()
}

function isMutationMethod(method) {
  return ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)
}

async function loadCsrfToken(force = false) {
  if (!force) {
    if (csrfToken) {
      return { token: csrfToken, headerName: csrfHeaderName }
    }
    if (csrfLoadPromise) {
      return csrfLoadPromise
    }
  }

  csrfLoadPromise = fetch(buildUrl('/auth/csrf'), {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json'
    }
  })
    .then(async (response) => {
      const payload = await response.json().catch(() => null)
      if (!response.ok || payload?.flag === false) {
        const error = new Error(payload?.message ?? `Request failed with status ${response.status}`)
        error.status = response.status
        error.code = payload?.code
        throw error
      }
      csrfToken = payload?.data?.token ?? null
      csrfHeaderName = payload?.data?.headerName ?? 'X-CSRF-TOKEN'
      return { token: csrfToken, headerName: csrfHeaderName }
    })
    .finally(() => {
      csrfLoadPromise = null
    })

  return csrfLoadPromise
}

export function resetCsrfTokenCache() {
  csrfLoadPromise = null
  csrfToken = null
  csrfHeaderName = 'X-CSRF-TOKEN'
}

async function request(path, options = {}, params, retryOnCsrfFailure = true) {
  const method = (options.method ?? 'GET').toUpperCase()
  const headers = {
    Accept: 'application/json',
    ...(options.headers ?? {})
  }
  const hasBody = options.body !== undefined

  if (isMutationMethod(method)) {
    const csrf = await loadCsrfToken()
    if (csrf?.token) {
      headers[csrf.headerName] = csrf.token
    }
  }

  if (hasBody && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(buildUrl(path, params), {
    credentials: 'include',
    ...options,
    method,
    headers
  })

  const payload = await response.json().catch(() => null)

  if (
    retryOnCsrfFailure &&
    isMutationMethod(method) &&
    response.status === 403 &&
    payload?.message === 'CSRF token missing or invalid'
  ) {
    await loadCsrfToken(true)
    return request(path, options, params, false)
  }

  if (!response.ok || payload?.flag === false) {
    const error = new Error(payload?.message ?? `Request failed with status ${response.status}`)
    error.status = response.status
    error.code = payload?.code
    throw error
  }

  return payload?.data
}

export function apiGet(path, params) {
  return request(path, { method: 'GET' }, params)
}

export function apiPost(path, body) {
  return request(path, {
    method: 'POST',
    body: JSON.stringify(body)
  })
}

export function apiPut(path, body) {
  return request(path, {
    method: 'PUT',
    body: JSON.stringify(body)
  })
}

export function apiDelete(path) {
  return request(path, { method: 'DELETE' })
}
