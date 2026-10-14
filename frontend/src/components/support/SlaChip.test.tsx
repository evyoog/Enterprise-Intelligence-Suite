import '../../i18n'
import { screen } from '@testing-library/react'
import i18n from 'i18next'
import { axe } from 'jest-axe'
import { describe, expect, it } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { formatSlaDuration, SlaChip } from './SlaChip'

describe('SlaChip', () => {
  it.each([
    ['ON_TRACK', 'On track'], ['WARNING', 'At risk'], ['BREACHED', 'Breached'], ['MET', 'Met'], ['MET_LATE', 'Met late'],
  ] as const)('labels %s', (state, label) => {
    renderWithProviders(<SlaChip state={state} />)
    expect(screen.getByText(label)).toBeInTheDocument()
  })

  it('shows the caption and the due time', () => {
    renderWithProviders(<SlaChip state="WARNING" caption="First response" dueAt="2027-02-01T10:30:00Z" />)
    expect(screen.getByText('First response: At risk')).toBeInTheDocument()
    expect(screen.getByText(/^Due .*2027/)).toBeInTheDocument()
  })

  it('shows only the due time when there is no state, and nothing without data', () => {
    const { container, rerender } = renderWithProviders(<SlaChip state={null} dueAt="2027-02-01T10:30:00Z" />)
    expect(screen.getByText(/^Due /)).toBeInTheDocument()
    expect(container.querySelector('.MuiChip-root')).toBeNull()
    rerender(<SlaChip state={null} dueAt={null} />)
    expect(container.textContent).toBe('')
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<SlaChip state="BREACHED" caption="Resolution" dueAt="2027-02-01T10:30:00Z" />)
    expect(await axe(container)).toHaveNoViolations()
  })
})

describe('formatSlaDuration', () => {
  const t = i18n.t.bind(i18n)
  it('formats business-hour targets', () => {
    expect(formatSlaDuration(30, false, t)).toBe('30 min')
    expect(formatSlaDuration(240, false, t)).toBe('4 h')
    expect(formatSlaDuration(480, false, t)).toBe('1 day (8 business h)')
    expect(formatSlaDuration(1440, false, t)).toBe('3 days (24 business h)')
    expect(formatSlaDuration(150, false, t)).toBe('2 h 30 min')
  })
  it('formats around-the-clock targets in real hours', () => {
    expect(formatSlaDuration(60, true, t)).toBe('1 h')
    expect(formatSlaDuration(480, true, t)).toBe('8 h')
    expect(formatSlaDuration(1440, true, t)).toBe('1 day (24 h)')
  })
  it('returns nothing for an invalid value', () => {
    expect(formatSlaDuration(0, false, t)).toBe('')
    expect(formatSlaDuration(Number.NaN, false, t)).toBe('')
  })
})
