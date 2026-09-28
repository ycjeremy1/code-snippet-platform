import { useEffect, useRef } from 'react'
import Prism from 'prismjs'
import 'prismjs/themes/prism-tomorrow.css'
import 'prismjs/components/prism-java'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-typescript'
import 'prismjs/components/prism-jsx'
import 'prismjs/components/prism-bash'
import 'prismjs/components/prism-sql'
import 'prismjs/components/prism-yaml'
import 'prismjs/components/prism-go'
import 'prismjs/components/prism-json'

// Map our free-text "language" field onto a Prism grammar name, falling back to
// plain-text highlighting for anything Prism doesn't recognize.
const LANGUAGE_ALIASES = {
  js: 'javascript',
  javascript: 'javascript',
  jsx: 'jsx',
  ts: 'typescript',
  typescript: 'typescript',
  py: 'python',
  python: 'python',
  java: 'java',
  go: 'go',
  golang: 'go',
  sql: 'sql',
  bash: 'bash',
  shell: 'bash',
  sh: 'bash',
  yaml: 'yaml',
  yml: 'yaml',
  json: 'json',
}

export default function CodeBlock({ code, language }) {
  const ref = useRef(null)

  useEffect(() => {
    if (ref.current) {
      Prism.highlightElement(ref.current)
    }
  }, [code, language])

  const grammar = LANGUAGE_ALIASES[(language || '').toLowerCase()] || 'clike'

  return (
    <pre className="code-block">
      <code ref={ref} className={`language-${grammar}`}>
        {code}
      </code>
    </pre>
  )
}
