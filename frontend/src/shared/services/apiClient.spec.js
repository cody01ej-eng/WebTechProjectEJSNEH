import { beforeEach, describe, expect, it, vi } from 'vitest'

describe('apiClient', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.unstubAllGlobals()
  })

  it('does not send a JSON content-type header for simple GET requests', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ flag: false, message: 'Authentication required', code: 401 })
      })

    vi.stubGlobal('fetch', fetchMock)

    const { apiGet } = await import('./apiClient')

    await expect(apiGet('/auth/me')).rejects.toThrow('Authentication required')

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [, options] = fetchMock.mock.calls[0]
    expect(options.method).toBe('GET')
    expect(options.headers.Accept).toBe('application/json')
    expect(options.headers['Content-Type']).toBeUndefined()
  })

  it('sends JSON content-type and csrf header for POST requests with a body', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({
          flag: true,
          data: {
            token: 'csrf-token',
            headerName: 'X-CSRF-TOKEN'
          }
        })
      })
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ flag: true, data: { id: 1 } })
      })

    vi.stubGlobal('fetch', fetchMock)

    const { apiPost } = await import('./apiClient')

    await expect(apiPost('/auth/login', { email: 'admin@projectpulse.local' })).resolves.toEqual({ id: 1 })

    expect(fetchMock).toHaveBeenCalledTimes(2)
    const [, options] = fetchMock.mock.calls[1]
    expect(options.method).toBe('POST')
    expect(options.headers['Content-Type']).toBe('application/json')
    expect(options.headers['X-CSRF-TOKEN']).toBe('csrf-token')
  })
})
