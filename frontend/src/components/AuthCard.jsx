export default function AuthCard({ title, subtitle, children }) {
  return (
    <div className="auth-shell">
      <div className="auth-shell-inner">
        <img src="/stabl-logo.png" alt="STaBL Foundation" className="auth-logo" />
        <div className="auth-card">
          <div className="auth-card-header">
            <h2 className="auth-card-title">{title}</h2>
            {subtitle && <p className="auth-card-subtitle">{subtitle}</p>}
          </div>
          <div className="auth-card-body">{children}</div>
        </div>
      </div>
    </div>
  )
}
