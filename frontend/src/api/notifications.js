import client from './client'

export async function getNotifications({ page = 0, size = 20 } = {}) {
  const { data } = await client.get('/notifications', { params: { page, size } })
  return data
}

export async function getUnreadCount() {
  const { data } = await client.get('/notifications/unread-count')
  return data.count
}

export async function markAllNotificationsRead() {
  await client.post('/notifications/read-all')
}
