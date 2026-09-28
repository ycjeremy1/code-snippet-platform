import { diffLines } from 'diff'

/**
 * Turns two code strings into two equal-length, row-aligned line lists so a left/right
 * pane can be rendered as independent lists that still line up visually: every removed
 * line gets a blank placeholder on the right at that row, and vice versa for additions.
 */
function buildDiffRows(oldText, newText) {
  const parts = diffLines(oldText || '', newText || '')
  const left = []
  const right = []
  let leftNo = 1
  let rightNo = 1

  for (const part of parts) {
    const lines = part.value.replace(/\n$/, '').split('\n')

    if (part.removed) {
      for (const text of lines) {
        left.push({ no: leftNo++, text, type: 'removed' })
        right.push({ no: null, text: '', type: 'empty' })
      }
    } else if (part.added) {
      for (const text of lines) {
        left.push({ no: null, text: '', type: 'empty' })
        right.push({ no: rightNo++, text, type: 'added' })
      }
    } else {
      for (const text of lines) {
        left.push({ no: leftNo++, text, type: 'unchanged' })
        right.push({ no: rightNo++, text, type: 'unchanged' })
      }
    }
  }

  return { left, right }
}

function DiffPane({ rows }) {
  return (
    <pre className="diff-pane">
      {rows.map((row, i) => (
        <div key={i} className={`diff-line diff-${row.type}`}>
          <span className="diff-line-no">{row.no ?? ''}</span>
          <span className="diff-line-text">{row.text}</span>
        </div>
      ))}
    </pre>
  )
}

export default function DiffView({ leftCode, rightCode }) {
  const { left, right } = buildDiffRows(leftCode, rightCode)

  return (
    <div className="diff-view">
      <DiffPane rows={left} />
      <DiffPane rows={right} />
    </div>
  )
}
