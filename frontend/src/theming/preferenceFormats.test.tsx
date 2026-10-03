import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { getTheme } from '../theme'
import { ACCENT_KEYS, ACCENT_PALETTES } from './accentPalette'
import { LocalePreferenceProvider, useLocalePreference } from './LocalePreferenceProvider'

// 15 March 2026, 14:05 in Asia/Kolkata.
const SAMPLE_ISO = '2026-03-15T08:35:00.000Z'

function Probe() {
  const { formatDate, formatDateTime, setDateFormat, setTimeFormat, setTimeZone, setRegion } = useLocalePreference()
  return (
    <div>
      <span data-testid="date">{formatDate(SAMPLE_ISO)}</span>
      <span data-testid="datetime">{formatDateTime(SAMPLE_ISO)}</span>
      <button onClick={() => { setTimeZone('Asia/Kolkata'); setRegion('en-GB') }}>ist</button>
      <button onClick={() => setDateFormat('MM/DD/YYYY')}>us</button>
      <button onClick={() => setDateFormat('YYYY-MM-DD')}>iso</button>
      <button onClick={() => setTimeFormat('12h')}>12h</button>
      <button onClick={() => setTimeFormat('24h')}>24h</button>
    </div>
  )
}

describe('C67 date and time formats', () => {
  beforeEach(() => localStorage.clear())

  it('keeps the region format by default and applies explicit date and time formats', async () => {
    const user = userEvent.setup()
    render(<LocalePreferenceProvider><Probe /></LocalePreferenceProvider>)
    await user.click(screen.getByText('ist'))
    expect(screen.getByTestId('date')).toHaveTextContent('15 Mar 2026')

    await user.click(screen.getByText('us'))
    expect(screen.getByTestId('date')).toHaveTextContent('03/15/2026')
    await user.click(screen.getByText('iso'))
    expect(screen.getByTestId('date')).toHaveTextContent('2026-03-15')
    expect(localStorage.getItem('vyoog-date-format')).toBe('YYYY-MM-DD')

    await user.click(screen.getByText('24h'))
    expect(screen.getByTestId('datetime')).toHaveTextContent('2026-03-15 14:05')
    await user.click(screen.getByText('12h'))
    expect(screen.getByTestId('datetime').textContent).toMatch(/^2026-03-15 0?2:05\s?pm$/i)
  })
})

describe('C67 accent palette', () => {
  it('replaces only the primary colour', () => {
    for (const key of ACCENT_KEYS) {
      const light = getTheme('light', key)
      const dark = getTheme('dark', key)
      expect(light.palette.primary.main).toBe(ACCENT_PALETTES[key].light.main)
      expect(dark.palette.primary.main).toBe(ACCENT_PALETTES[key].dark.main)
      expect(light.palette.background.default).toBe('#F7F8FC')
      expect(light.palette.secondary.main).toBe('#7C3AED')
    }
    expect(getTheme('light').palette.primary.main).toBe('#4F46E5')
  })
})
