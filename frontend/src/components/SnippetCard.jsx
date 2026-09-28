import { Link } from 'react-router-dom'

export default function SnippetCard({ snippet }) {
  return (
    <Link to={`/snippets/${snippet.id}`} className="snippet-card">
      <div className="snippet-card-header">
        <h3>{snippet.title}</h3>
        <span className="language-badge">{snippet.language}</span>
      </div>
      {snippet.description && <p className="snippet-description">{snippet.description}</p>}
      <div className="snippet-tags">
        {[...snippet.tags].map((tag) => (
          <span key={tag} className="tag-chip">
            #{tag}
          </span>
        ))}
      </div>
      <div className="snippet-meta">
        <span>by {snippet.author.username}</span>
        <span>👁 {snippet.viewCount}</span>
        <span>♥ {snippet.likeCount}</span>
        <span>💬 {snippet.commentCount}</span>
      </div>
    </Link>
  )
}
