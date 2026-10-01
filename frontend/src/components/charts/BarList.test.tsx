import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { BarList } from './BarList'

describe('BarList', () => {
  it('prints every value as text, never only as a bar width', () => {
    render(<BarList items={[{ label: 'Valam.ai', value: 42 }, { label: 'Varthan.ai', value: 17 }]} />)

    expect(screen.getByText('Valam.ai')).toBeInTheDocument()
    expect(screen.getByText('42')).toBeInTheDocument()
    expect(screen.getByText('Varthan.ai')).toBeInTheDocument()
    expect(screen.getByText('17')).toBeInTheDocument()
  })

  it('shows an explicit empty state instead of a blank chart', () => {
    render(<BarList items={[]} />)
    expect(screen.getByText('No data yet.')).toBeInTheDocument()
  })
})
