import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import NotificationBell from './NotificationBell'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="brand">
        <span className="brand-badge">
          <img src="/stabl-logo.png" alt="STaBL Foundation" />
        </span>
        <span className="brand-name">CodeShare</span>
      </Link>
      <div className="nav-links">
        <Link to="/compare">Compare</Link>
        {user ? (
          <>
            <Link to="/snippets/new">Publish</Link>
            <Link to="/profile">My Snippets</Link>
            <NotificationBell />
            <span className="nav-username">{user.username}</span>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  )
}
