import { useEffect, useRef, useState } from 'react'
import { searchSnippets } from '../api/snippets'

/**
 * Search-as-you-type combobox for picking one snippet (used twice on the compare
 * page, once per side — nothing ties the two together, so the two picks can be
 * any two users' submissions).
 */
export default function SnippetPicker({ label, selected, onSelect }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const containerRef = useRef(null)

  useEffect(() => {
    if (!query.trim()) {
      setResults([])
      return undefined
    }
    let cancelled = false
    setLoading(true)
    const timer = setTimeout(() => {
      searchSnippets({ keyword: query, size: 8 })
        .then((data) => {
          if (!cancelled) setResults(data.content)
        })
        .finally(() => {
          if (!cancelled) setLoading(false)
        })
    }, 300)

    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [query])

  useEffect(() => {
    function handleClickOutside(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handlePick(snippet) {
    onSelect(snippet)
    setOpen(false)
    setQuery('')
  }

  return (
    <div className="snippet-picker" ref={containerRef}>
      <span className="snippet-picker-label">{label}</span>

      {selected ? (
        <div className="snippet-picker-selected">
          <div>
            <strong>{selected.title}</strong>
            <span className="snippet-picker-meta">
              by {selected.author.username} · {selected.language}
            </span>
          </div>
          <button type="button" className="link-button" onClick={() => onSelect(null)}>
            Change
          </button>
        </div>
      ) : (
        <div className="snippet-picker-input">
          <input
            placeholder="Search a snippet by title…"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value)
              setOpen(true)
            }}
            onFocus={() => setOpen(true)}
          />
          {open && query.trim() && (
            <ul className="snippet-picker-dropdown">
              {loading && <li className="snippet-picker-empty">Searching…</li>}
              {!loading && results.length === 0 && <li className="snippet-picker-empty">No matches</li>}
              {!loading &&
                results.map((snippet) => (
                  <li key={snippet.id}>
                    <button type="button" onClick={() => handlePick(snippet)}>
                      <strong>{snippet.title}</strong>
                      <span className="snippet-picker-meta">
                        by {snippet.author.username} · {snippet.language}
                      </span>
                    </button>
                  </li>
                ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}
