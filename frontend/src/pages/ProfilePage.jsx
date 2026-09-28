import { useEffect, useState } from 'react'
import { getMyFavorites, getMySnippets } from '../api/snippets'
import { useAuth } from '../context/AuthContext'
import SnippetCard from '../components/SnippetCard'
import Pagination from '../components/Pagination'

export default function ProfilePage() {
  const { user } = useAuth()
  const [tab, setTab] = useState('mine')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState({ content: [], totalPages: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const fetcher = tab === 'mine' ? getMySnippets : getMyFavorites
    fetcher({ page, size: 12 })
      .then(setResult)
      .finally(() => setLoading(false))
  }, [tab, page])

  function switchTab(next) {
    setTab(next)
    setPage(0)
  }

  return (
    <div className="profile-page">
      <h2>{user.username}</h2>
      {user.bio && <p>{user.bio}</p>}

      <div className="tabs">
        <button className={tab === 'mine' ? 'active' : ''} onClick={() => switchTab('mine')}>
          My Snippets
        </button>
        <button className={tab === 'favorites' ? 'active' : ''} onClick={() => switchTab('favorites')}>
          Favorites
        </button>
      </div>

      {loading ? (
        <p>Loading…</p>
      ) : (
        <>
          <div className="snippet-grid">
            {result.content.map((snippet) => (
              <SnippetCard key={snippet.id} snippet={snippet} />
            ))}
          </div>
          {result.content.length === 0 && <p>Nothing here yet.</p>}
          <Pagination page={result.page ?? page} totalPages={result.totalPages ?? 0} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
