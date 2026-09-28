import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { getSnippet, toggleLike, toggleFavorite, deleteSnippet } from '../api/snippets'
import { getComments, addComment, deleteComment } from '../api/comments'
import { useAuth } from '../context/AuthContext'
import CodeBlock from '../components/CodeBlock'

export default function SnippetDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [snippet, setSnippet] = useState(null)
  const [comments, setComments] = useState([])
  const [commentText, setCommentText] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    Promise.all([getSnippet(id), getComments(id)])
      .then(([snippetData, commentPage]) => {
        if (cancelled) return
        setSnippet(snippetData)
        setComments(commentPage.content)
      })
      .catch(() => {
        if (!cancelled) setError('Snippet not found')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  async function handleLike() {
    const liked = await toggleLike(id)
    setSnippet((s) => ({ ...s, likedByCurrentUser: liked, likeCount: s.likeCount + (liked ? 1 : -1) }))
  }

  async function handleFavorite() {
    const favorited = await toggleFavorite(id)
    setSnippet((s) => ({ ...s, favoritedByCurrentUser: favorited }))
  }

  async function handleDelete() {
    if (!window.confirm('Delete this snippet? This cannot be undone.')) return
    await deleteSnippet(id)
    navigate('/')
  }

  async function handleAddComment(e) {
    e.preventDefault()
    if (!commentText.trim()) return
    const created = await addComment(id, commentText.trim())
    setComments((prev) => [created, ...prev])
    setCommentText('')
    setSnippet((s) => ({ ...s, commentCount: s.commentCount + 1 }))
  }

  async function handleDeleteComment(commentId) {
    await deleteComment(commentId)
    setComments((prev) => prev.filter((c) => c.id !== commentId))
    setSnippet((s) => ({ ...s, commentCount: s.commentCount - 1 }))
  }

  if (loading) return <p>Loading…</p>
  if (error) return <p className="form-error">{error}</p>
  if (!snippet) return null

  const isOwner = user && user.username === snippet.author.username

  return (
    <div className="snippet-detail-page">
      <div className="snippet-detail-header">
        <div>
          <h2>{snippet.title}</h2>
          <p className="snippet-description">{snippet.description}</p>
          <div className="snippet-tags">
            {[...snippet.tags].map((tag) => (
              <span key={tag} className="tag-chip">
                #{tag}
              </span>
            ))}
          </div>
        </div>
        {isOwner && (
          <div className="owner-actions">
            <Link to={`/snippets/${snippet.id}/edit`}>Edit</Link>
            <button onClick={handleDelete}>Delete</button>
          </div>
        )}
      </div>

      <CodeBlock code={snippet.code} language={snippet.language} />

      <div className="snippet-actions">
        <button onClick={handleLike} disabled={!user} className={snippet.likedByCurrentUser ? 'active' : ''}>
          ♥ {snippet.likeCount}
        </button>
        <button
          onClick={handleFavorite}
          disabled={!user}
          className={snippet.favoritedByCurrentUser ? 'active' : ''}
        >
          ★ {snippet.favoritedByCurrentUser ? 'Favorited' : 'Favorite'}
        </button>
        <span>👁 {snippet.viewCount} views</span>
        <span>by {snippet.author.username}</span>
      </div>

      <section className="comments-section">
        <h3>Comments ({snippet.commentCount})</h3>
        {user && (
          <form onSubmit={handleAddComment} className="comment-form">
            <textarea
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="Add a comment…"
              maxLength={1000}
            />
            <button type="submit">Post</button>
          </form>
        )}
        <ul className="comment-list">
          {comments.map((comment) => (
            <li key={comment.id} className="comment-item">
              <div className="comment-meta">
                <strong>{comment.user.username}</strong>
                <span>{new Date(comment.createdAt).toLocaleString()}</span>
              </div>
              <p>{comment.content}</p>
              {user && (user.username === comment.user.username || isOwner) && (
                <button className="link-button" onClick={() => handleDeleteComment(comment.id)}>
                  Delete
                </button>
              )}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
