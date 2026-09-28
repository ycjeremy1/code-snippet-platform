import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getUnreadCount } from '../api/notifications'
import { useAuth } from '../context/AuthContext'

const POLL_INTERVAL_MS = 30000

export default function NotificationBell() {
  const { user } = useAuth()
  const [count, setCount] = useState(0)

  useEffect(() => {
    if (!user) return undefined

    let cancelled = false
    async function poll() {
      try {
        const unread = await getUnreadCount()
        if (!cancelled) setCount(unread)
      } catch {
        // ignore transient polling failures
      }
    }

    poll()
    const interval = setInterval(poll, POLL_INTERVAL_MS)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [user])

  if (!user) return null

  return (
    <Link to="/notifications" className="notification-bell">
      🔔{count > 0 && <span className="notification-badge">{count > 99 ? '99+' : count}</span>}
    </Link>
  )
}
