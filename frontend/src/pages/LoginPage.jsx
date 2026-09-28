import { useState } from 'react'
import { useLocation, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AuthCard from '../components/AuthCard'
import { MailIcon, LockIcon, SpinnerIcon, GoogleIcon } from '../components/icons'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [usernameOrEmail, setUsernameOrEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [googleNotice, setGoogleNotice] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(usernameOrEmail, password)
      const redirectTo = location.state?.from?.pathname || '/'
      navigate(redirectTo, { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthCard title="Welcome back" subtitle="Enter your credentials to access your account">
      <form onSubmit={handleSubmit} className="auth-form" noValidate>
        <div className="field-group">
          <label htmlFor="usernameOrEmail">Email or username</label>
          <div className="input-icon-wrap">
            <MailIcon className="input-icon" />
            <input
              id="usernameOrEmail"
              type="text"
              placeholder="name@example.com"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              disabled={submitting}
              autoComplete="username"
              required
            />
          </div>
        </div>

        <div className="field-group">
          <label htmlFor="password">Password</label>
          <div className="input-icon-wrap">
            <LockIcon className="input-icon" />
            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={submitting}
              autoComplete="current-password"
              required
            />
          </div>
        </div>

        {error && <p className="form-error">{error}</p>}

        <button type="submit" className="btn-auth-primary" disabled={submitting}>
          {submitting && <SpinnerIcon />}
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <div className="auth-divider">
        <span>Or continue with</span>
      </div>

      <button
        type="button"
        className="btn-auth-google"
        onClick={() => setGoogleNotice(true)}
      >
        <GoogleIcon />
        Sign in with Google
      </button>
      {googleNotice && <p className="auth-hint">Google sign-in is coming soon — use email/username for now.</p>}

      <p className="auth-footer">
        No account yet? <Link to="/register">Register</Link>
      </p>
    </AuthCard>
  )
}
