import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

let accessToken = null
let unauthorizedHandler = null
let refreshing = null

export function setAccessToken(token) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const status = error.response?.status
    const isAuthRequest = original?.url?.includes('/auth/')

    if (status === 401 && original && !original._retry && !isAuthRequest) {
      original._retry = true
      try {
        if (!refreshing) {
          refreshing = axios
            .post('/api/v1/auth/refresh', null, { withCredentials: true })
            .then((response) => {
              setAccessToken(response.data.accessToken)
              unauthorizedHandler?.onRefresh?.(response.data.user)
              return response.data
            })
            .finally(() => {
              refreshing = null
            })
        }
        const data = await refreshing
        original.headers.Authorization = `Bearer ${data.accessToken}`
        return api(original)
      } catch {
        setAccessToken(null)
        unauthorizedHandler?.onLogout?.()
      }
    }
    return Promise.reject(error)
  },
)

export function apiErrorMessage(error, fallback = 'Ocurrió un error inesperado.') {
  const data = error?.response?.data
  if (data?.errors?.length) {
    return data.errors.map((item) => item.message).join('. ')
  }
  return data?.detail || data?.message || fallback
}

export default api
