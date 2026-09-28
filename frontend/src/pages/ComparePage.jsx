import { useEffect, useState } from 'react'
import { getSnippet } from '../api/snippets'
import SnippetPicker from '../components/SnippetPicker'
import DiffView from '../components/DiffView'

export default function ComparePage() {
  const [left, setLeft] = useState(null)
  const [right, setRight] = useState(null)
  const [leftDetail, setLeftDetail] = useState(null)
  const [rightDetail, setRightDetail] = useState(null)

  // The picker's search results don't include full code, so once a snippet is chosen
  // fetch its full detail (the search endpoint is cached list data; this hits the
  // cached-by-id detail endpoint instead, same as opening the snippet directly).
  useEffect(() => {
    if (!left) return setLeftDetail(null)
    getSnippet(left.id).then(setLeftDetail)
  }, [left])

  useEffect(() => {
    if (!right) return setRightDetail(null)
    getSnippet(right.id).then(setRightDetail)
  }, [right])

  function handleSwap() {
    setLeft(right)
    setRight(left)
  }

  return (
    <div className="compare-page">
      <div className="compare-header">
        <h2>Compare snippets</h2>
        <button type="button" onClick={handleSwap} disabled={!left && !right}>
          ⇄ Swap
        </button>
      </div>

      <div className="compare-pickers">
        <SnippetPicker label="Snippet A" selected={left} onSelect={setLeft} />
        <SnippetPicker label="Snippet B" selected={right} onSelect={setRight} />
      </div>

      {leftDetail && rightDetail ? (
        <>
          <div className="compare-pane-headers">
            <div>
              <strong>{leftDetail.title}</strong>
              <span className="snippet-picker-meta">
                by {leftDetail.author.username} · {leftDetail.language} · ♥ {leftDetail.likeCount}
              </span>
            </div>
            <div>
              <strong>{rightDetail.title}</strong>
              <span className="snippet-picker-meta">
                by {rightDetail.author.username} · {rightDetail.language} · ♥ {rightDetail.likeCount}
              </span>
            </div>
          </div>
          <DiffView leftCode={leftDetail.code} rightCode={rightDetail.code} />
          <p className="compare-legend">
            <span className="legend-dot legend-removed" /> only in A &nbsp;
            <span className="legend-dot legend-added" /> only in B
          </p>
        </>
      ) : (
        <p className="compare-placeholder">Pick two snippets above to see a line-by-line comparison.</p>
      )}
    </div>
  )
}
