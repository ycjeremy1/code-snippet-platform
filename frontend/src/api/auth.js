import client, { tokenStorage } from './client'

export async function register(username, email, password) {
  const { data } = await client.post('/auth/register', { username, email, password })
  tokenStorage.setTokens(data.accessToken, data.refreshToken)
  return data.user
}

export async function login(usernameOrEmail, password) {
  const { data } = await client.post('/auth/login', { usernameOrEmail, password })
  tokenStorage.setTokens(data.accessToken, data.refreshToken)
  return data.user
}

export async function logout() {
  const refreshToken = tokenStorage.getRefreshToken()
  try {
    await client.post('/auth/logout', { refreshToken })
  } finally {
    tokenStorage.clear()
  }
}

export async function fetchCurrentUser() {
  const { data } = await client.get('/users/me')
  return data
}
