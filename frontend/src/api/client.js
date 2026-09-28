import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const client = axios.create({ baseURL: BASE_URL })

const ACCESS_TOKEN_KEY = 'csp_access_token'
const REFRESH_TOKEN_KEY = 'csp_refresh_token'

export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}

client.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Queues concurrent 401s behind a single in-flight refresh call instead of firing one
// refresh request per failed request.
let refreshPromise = null

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error
    const isAuthEndpoint = config?.url?.startsWith('/auth/')

    if (response?.status === 401 && !config._retry && !isAuthEndpoint && tokenStorage.getRefreshToken()) {
      config._retry = true
      try {
        if (!refreshPromise) {
          refreshPromise = axios
            .post(`${BASE_URL}/auth/refresh`, { refreshToken: tokenStorage.getRefreshToken() })
            .finally(() => {
              refreshPromise = null
            })
        }
        const { data } = await refreshPromise
        tokenStorage.setTokens(data.accessToken, data.refreshToken)
        config.headers.Authorization = `Bearer ${data.accessToken}`
        return client(config)
      } catch (refreshError) {
        tokenStorage.clear()
        window.location.assign('/login')
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default client
