import { useEffect, useState } from 'react'
import { searchSnippets } from '../api/snippets'
import SnippetCard from '../components/SnippetCard'
import Pagination from '../components/Pagination'
import Hero from '../components/Hero'
import { useAuth } from '../context/AuthContext'

export default function SnippetListPage() {
  const { user } = useAuth()
  const [keyword, setKeyword] = useState('')
  const [language, setLanguage] = useState('')
  const [tag, setTag] = useState('')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState({ content: [], totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    searchSnippets({ keyword, language, tag, page, size: 12 })
      .then((data) => {
        if (!cancelled) setResult(data)
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load snippets')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [keyword, language, tag, page])

  function handleSearchSubmit(e) {
    e.preventDefault()
    setPage(0)
  }

  return (
    <div className="snippet-list-page">
      {!user && <Hero />}
      <form id="browse" className="search-bar" onSubmit={handleSearchSubmit}>
        <input
          placeholder="Search by title or description…"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <input placeholder="Language (e.g. java)" value={language} onChange={(e) => setLanguage(e.target.value)} />
        <input placeholder="Tag" value={tag} onChange={(e) => setTag(e.target.value)} />
        <button type="submit">Search</button>
      </form>

      {loading && <p>Loading…</p>}
      {error && <p className="form-error">{error}</p>}

      {!loading && !error && (
        <>
          <div className="snippet-grid">
            {result.content.map((snippet) => (
              <SnippetCard key={snippet.id} snippet={snippet} />
            ))}
          </div>
          {result.content.length === 0 && <p>No snippets found.</p>}
          <Pagination page={result.page ?? page} totalPages={result.totalPages ?? 0} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
