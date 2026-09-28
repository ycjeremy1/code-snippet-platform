import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createSnippet, getSnippet, updateSnippet } from '../api/snippets'

const emptyForm = { title: '', description: '', language: '', code: '', tags: '', isPublic: true }

export default function PublishSnippetPage() {
  const { id } = useParams()
  const isEditing = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(isEditing)

  useEffect(() => {
    if (!isEditing) return
    getSnippet(id).then((snippet) => {
      setForm({
        title: snippet.title,
        description: snippet.description || '',
        language: snippet.language,
        code: snippet.code,
        tags: [...snippet.tags].join(', '),
        isPublic: snippet.isPublic,
      })
      setLoading(false)
    })
  }, [id, isEditing])

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)

    const payload = {
      title: form.title,
      description: form.description,
      language: form.language,
      code: form.code,
      isPublic: form.isPublic,
      tags: form.tags
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean),
    }

    try {
      const saved = isEditing ? await updateSnippet(id, payload) : await createSnippet(payload)
      navigate(`/snippets/${saved.id}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save snippet')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p>Loading…</p>

  return (
    <div className="publish-page">
      <h2>{isEditing ? 'Edit snippet' : 'Publish a snippet'}</h2>
      <form onSubmit={handleSubmit} className="publish-form">
        <label>
          Title
          <input value={form.title} onChange={(e) => update('title', e.target.value)} required maxLength={200} />
        </label>
        <label>
          Description
          <textarea
            value={form.description}
            onChange={(e) => update('description', e.target.value)}
            maxLength={1000}
          />
        </label>
        <label>
          Language
          <input
            value={form.language}
            onChange={(e) => update('language', e.target.value)}
            placeholder="e.g. java, python, javascript"
            required
          />
        </label>
        <label>
          Tags (comma-separated)
          <input value={form.tags} onChange={(e) => update('tags', e.target.value)} placeholder="algorithms, react" />
        </label>
        <label>
          Code
          <textarea
            className="code-input"
            value={form.code}
            onChange={(e) => update('code', e.target.value)}
            required
            spellCheck={false}
          />
        </label>
        <label className="checkbox-label">
          <input type="checkbox" checked={form.isPublic} onChange={(e) => update('isPublic', e.target.checked)} />
          Public
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : isEditing ? 'Save changes' : 'Publish'}
        </button>
      </form>
    </div>
  )
}
