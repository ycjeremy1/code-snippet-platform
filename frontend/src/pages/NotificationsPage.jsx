import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getNotifications, markAllNotificationsRead } from '../api/notifications'
import Pagination from '../components/Pagination'

export default function NotificationsPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState({ content: [], totalPages: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    getNotifications({ page, size: 20 })
      .then(setResult)
      .finally(() => setLoading(false))
  }, [page])

  async function handleMarkAllRead() {
    await markAllNotificationsRead()
    setResult((prev) => ({ ...prev, content: prev.content.map((n) => ({ ...n, isRead: true })) }))
  }

  return (
    <div className="notifications-page">
      <div className="notifications-header">
        <h2>Notifications</h2>
        <button onClick={handleMarkAllRead}>Mark all as read</button>
      </div>

      {loading ? (
        <p>Loading…</p>
      ) : (
        <>
          <ul className="notification-list">
            {result.content.map((notification) => (
              <li key={notification.id} className={notification.isRead ? 'read' : 'unread'}>
                {notification.relatedSnippetId ? (
                  <Link to={`/snippets/${notification.relatedSnippetId}`}>{notification.message}</Link>
                ) : (
                  <span>{notification.message}</span>
                )}
                <time>{new Date(notification.createdAt).toLocaleString()}</time>
              </li>
            ))}
          </ul>
          {result.content.length === 0 && <p>No notifications yet.</p>}
          <Pagination page={result.page ?? page} totalPages={result.totalPages ?? 0} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
