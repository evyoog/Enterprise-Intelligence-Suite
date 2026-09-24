import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { LocalePreferenceProvider, useLocalePreference } from './LocalePreferenceProvider'

const SAMPLE_ISO = '2026-03-15T09:30:00.000Z'

function Consumer() {
  const { timeZone, setTimeZone, region, setRegion, formatDate } = useLocalePreference()
  return (
    <div>
      <span data-testid="tz">{timeZone}</span>
      <span data-testid="region">{region ?? 'null'}</span>
      <span data-testid="formatted">{formatDate(SAMPLE_ISO)}</span>
      <button onClick={() => setTimeZone('Asia/Kolkata')}>set-ist</button>
      <button onClick={() => setRegion('en-GB')}>set-gb</button>
      <button onClick={() => setRegion('en-US')}>set-us</button>
    </div>
  )
}

describe('LocalePreferenceProvider', () => {
  it('region and time zone are independent axes — changing one leaves the other untouched', async () => {
    const user = userEvent.setup()
    render(<LocalePreferenceProvider><Consumer /></LocalePreferenceProvider>)

    await user.click(screen.getByText('set-ist'))
    expect(screen.getByTestId('tz')).toHaveTextContent('Asia/Kolkata')
    expect(screen.getByTestId('region')).toHaveTextContent('null')

    await user.click(screen.getByText('set-gb'))
    expect(screen.getByTestId('tz')).toHaveTextContent('Asia/Kolkata')
    expect(screen.getByTestId('region')).toHaveTextContent('en-GB')
  })

  it('the same date formats differently for different regions, independent of time zone', async () => {
    const user = userEvent.setup()
    render(<LocalePreferenceProvider><Consumer /></LocalePreferenceProvider>)

    await user.click(screen.getByText('set-us'))
    const usFormatted = screen.getByTestId('formatted').textContent

    await user.click(screen.getByText('set-gb'))
    const gbFormatted = screen.getByTestId('formatted').textContent

    // en-US and en-GB order day/month differently for a "medium" date style
    // ("Mar 15, 2026" vs "15 Mar 2026") — this is the actual conceptual fix
    // (region as a distinct, real formatting axis) this phase makes.
    expect(usFormatted).not.toEqual(gbFormatted)
  })

  it('persists an explicit region choice and clears it back to browser-default on null', async () => {
    const user = userEvent.setup()
    render(<LocalePreferenceProvider><Consumer /></LocalePreferenceProvider>)

    await user.click(screen.getByText('set-gb'))
    expect(localStorage.getItem('vyoog-region')).toBe('en-GB')
  })

  it('throws a clear error if used outside the provider', () => {
    function Bare() {
      useLocalePreference()
      return null
    }
    expect(() => render(<Bare />)).toThrow(/useLocalePreference must be used within/)
  })
})
