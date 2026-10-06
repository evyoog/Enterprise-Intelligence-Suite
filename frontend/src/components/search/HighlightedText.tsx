import { Fragment } from 'react'
import type { SearchHighlight } from '../../api/globalSearchApi'

/** Renders text with the matching words in <mark> (C70). The text is never
 * parsed as HTML: highlights are character ranges sent by the backend. */
export function HighlightedText({ text, highlights }: { text: string; highlights?: SearchHighlight[] | null }) {
  if (!highlights || highlights.length === 0) return <>{text}</>
  const ranges = [...highlights]
    .filter((h) => h.length > 0 && h.start >= 0 && h.start < text.length)
    .sort((a, b) => a.start - b.start)
  const parts: React.ReactNode[] = []
  let cursor = 0
  ranges.forEach((range, index) => {
    if (range.start < cursor) return
    if (range.start > cursor) parts.push(<Fragment key={`t${index}`}>{text.slice(cursor, range.start)}</Fragment>)
    const end = Math.min(text.length, range.start + range.length)
    parts.push(<mark key={`m${index}`} className="search-mark">{text.slice(range.start, end)}</mark>)
    cursor = end
  })
  if (cursor < text.length) parts.push(<Fragment key="rest">{text.slice(cursor)}</Fragment>)
  return <>{parts}</>
}
