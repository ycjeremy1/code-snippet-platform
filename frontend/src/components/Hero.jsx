import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Hero() {
  const { user } = useAuth()

  return (
    <section className="hero">
      <div className="hero-copy">
        <h1>
          How developers
          <br />
          <span className="hero-highlight">share</span> &amp; <span className="hero-highlight">discover</span>
          <br />
          code together
        </h1>
        <p className="hero-subtext">
          Publish code snippets, search by language or tag, and collaborate through likes, comments,
          and favorites — with real-time notifications powered by Redis and RabbitMQ.
        </p>
        <div className="hero-actions">
          {user ? (
            <Link to="/snippets/new" className="hero-cta">
              Publish a snippet
            </Link>
          ) : (
            <Link to="/register" className="hero-cta">
              Get started free
            </Link>
          )}
          <a href="#browse" className="hero-secondary">
            Browse snippets ↓
          </a>
        </div>
      </div>

      <div className="hero-visual" aria-hidden="true">
        <div className="mock-window">
          <div className="mock-window-bar">
            <span />
            <span />
            <span />
          </div>
          <div className="mock-card">
            <div className="mock-dot lang-js" />
            <div className="mock-line long" />
            <div className="mock-line short" />
          </div>
          <div className="mock-card">
            <div className="mock-dot lang-py" />
            <div className="mock-line medium" />
            <div className="mock-line long" />
          </div>
          <div className="mock-card">
            <div className="mock-dot lang-java" />
            <div className="mock-line short" />
            <div className="mock-line medium" />
          </div>
        </div>
      </div>
    </section>
  )
}
