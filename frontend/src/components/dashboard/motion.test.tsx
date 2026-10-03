import { act, renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { useCountUp, useDrawn } from './motion'

describe('C69 dashboard motion', () => {
  it('counts up to the loaded value once when motion is allowed', async () => {
    const { result, rerender } = renderHook(({ v }) => useCountUp(v, true, 50), { initialProps: { v: undefined as number | undefined } })
    expect(result.current).toBeUndefined()
    rerender({ v: 5 })
    expect(result.current).toBe(0)
    await act(() => new Promise((r) => setTimeout(r, 200)))
    expect(result.current).toBe(5)
    // A later change (e.g. a refresh) shows the new value without replaying.
    rerender({ v: 7 })
    expect(result.current).toBe(7)
  })

  it('shows the value immediately and draws at once when motion is off (Reduce motion)', () => {
    const { result } = renderHook(() => useCountUp(5, false))
    expect(result.current).toBe(5)
    const drawn = renderHook(() => useDrawn(false))
    expect(drawn.result.current).toBe(true)
  })
})
