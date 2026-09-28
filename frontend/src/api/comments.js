import client from './client'

export async function getComments(snippetId, { page = 0, size = 20 } = {}) {
  const { data } = await client.get(`/comments/snippet/${snippetId}`, { params: { page, size } })
  return data
}

export async function addComment(snippetId, content) {
  const { data } = await client.post(`/snippets/${snippetId}/comments`, { content })
  return data
}

export async function deleteComment(commentId) {
  await client.delete(`/comments/${commentId}`)
}
