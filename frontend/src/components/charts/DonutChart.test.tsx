import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ThemeModeProvider } from '../../theming/ThemeModeProvider'
import { DonutChart } from './DonutChart'

describe('DonutChart', () => {
  it('never shows color alone — every segment is repeated as text in the legend', () => {
    render(
      <ThemeModeProvider>
        <DonutChart
          centerValue="3"
          centerLabel="things"
          segments={[
            { label: 'Active', value: 2, tone: 'success' },
            { label: 'Suspended', value: 1, tone: 'warning' },
          ]}
        />
      </ThemeModeProvider>
    )

    expect(screen.getByText('Active')).toBeInTheDocument()
    expect(screen.getByText('2 (67%)')).toBeInTheDocument()
    expect(screen.getByText('Suspended')).toBeInTheDocument()
    expect(screen.getByText('1 (33%)')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
  })
})
