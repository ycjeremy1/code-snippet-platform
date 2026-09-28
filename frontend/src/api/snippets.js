import client from './client'

export async function searchSnippets({ keyword, language, tag, page = 0, size = 12 } = {}) {
  const { data } = await client.get('/snippets', { params: { keyword, language, tag, page, size } })
  return data
}

export async function getSnippet(id) {
  const { data } = await client.get(`/snippets/${id}`)
  return data
}

export async function createSnippet(payload) {
  const { data } = await client.post('/snippets', payload)
  return data
}

export async function updateSnippet(id, payload) {
  const { data } = await client.put(`/snippets/${id}`, payload)
  return data
}

export async function deleteSnippet(id) {
  await client.delete(`/snippets/${id}`)
}

export async function toggleLike(id) {
  const { data } = await client.post(`/snippets/${id}/like`)
  return data.liked
}

export async function toggleFavorite(id) {
  const { data } = await client.post(`/snippets/${id}/favorite`)
  return data.favorited
}

export async function getMySnippets({ page = 0, size = 12 } = {}) {
  const { data } = await client.get('/snippets/mine', { params: { page, size } })
  return data
}

export async function getMyFavorites({ page = 0, size = 12 } = {}) {
  const { data } = await client.get('/snippets/favorites', { params: { page, size } })
  return data
}
