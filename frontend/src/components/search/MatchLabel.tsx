import { useTranslation } from 'react-i18next'
import { Chip } from '@mui/material'
import type { SearchMatchType } from '../../api/globalSearchApi'

const COLOR: Record<SearchMatchType, 'primary' | 'success' | 'default' | 'warning' | 'secondary' | 'info'> = {
  EXACT_ID: 'primary',
  EXACT_PHRASE: 'success',
  KEYWORD: 'default',
  PARTIAL: 'info',
  TYPO: 'warning',
  SEMANTIC: 'secondary',
}

/** C70: why a result matched ("Exact phrase", "Similar meaning", …). */
export function MatchLabel({ matchType }: { matchType?: SearchMatchType }) {
  const { t } = useTranslation()
  if (!matchType) return null
  return (
    <Chip
      size="small"
      variant={matchType === 'KEYWORD' ? 'outlined' : 'filled'}
      color={COLOR[matchType]}
      label={t(`search.match.${matchType}`)}
      title={t(`search.matchHelp.${matchType}`)}
      sx={{ height: 22, fontSize: 11.5, fontWeight: 600 }}
    />
  )
}
