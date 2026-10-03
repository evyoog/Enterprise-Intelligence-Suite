import { useEffect, useRef, useState } from 'react'
import { keyframes } from '@mui/material/styles'
import { useThemeMode } from '../../theming/ThemeModeProvider'

/**
 * C69: dashboard motion is allowed only when neither the EIS "Reduce motion"
 * preference nor the operating system's prefers-reduced-motion asks for less.
 */
export function useMotionAllowed(): boolean {
  const { reducedMotion } = useThemeMode()
  const [osReduced, setOsReduced] = useState(() =>
    typeof window !== 'undefined' && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches === true)
  useEffect(() => {
    const media = window.matchMedia?.('(prefers-reduced-motion: reduce)')
    if (!media?.addEventListener) return
    const onChange = (e: MediaQueryListEvent) => setOsReduced(e.matches)
    media.addEventListener('change', onChange)
    return () => media.removeEventListener('change', onChange)
  }, [])
  return !reducedMotion && !osReduced
}

/**
 * Counts from 0 to `value` once, the first time a value is known (ease-out).
 * Later changes (a refresh) show the new value directly, so interacting with
 * the page never replays the animation. With motion off it returns the value.
 */
export function useCountUp(value: number | undefined, allowed: boolean, durationMs = 700): number | undefined {
  const [animated, setAnimated] = useState<number | undefined>(undefined)
  const [done, setDone] = useState(false)
  const played = useRef(false)
  const doneRef = useRef(false)
  useEffect(() => {
    if (value === undefined || !allowed || played.current) return
    played.current = true
    const start = performance.now()
    let frame = 0
    // Read the clock here (the frame timestamp can come from another clock or precede `start`), clamped to 0–1.
    const tick = () => {
      const t = Math.min(1, Math.max(0, (performance.now() - start) / durationMs))
      setAnimated(Math.round(value * (1 - Math.pow(1 - t, 3))))
      if (t < 1) frame = requestAnimationFrame(tick)
      else { doneRef.current = true; setDone(true) }
    }
    frame = requestAnimationFrame(tick)
    return () => { cancelAnimationFrame(frame); if (!doneRef.current) played.current = false }
  }, [value, allowed, durationMs])
  if (value === undefined || !allowed || done) return value
  return animated ?? 0
}

/** True one frame after mount, so CSS transitions (bar growth, donut and progress drawing) start from zero. */
export function useDrawn(allowed: boolean): boolean {
  const [drawn, setDrawn] = useState(false)
  useEffect(() => {
    if (!allowed) return
    const frame = requestAnimationFrame(() => setDrawn(true))
    return () => cancelAnimationFrame(frame)
  }, [allowed])
  return allowed ? drawn : true
}

const fadeUpKeyframes = keyframes`
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: none; }
`

/** Entrance for a section or list item; `index` staggers siblings. No-op when motion is off. */
export function fadeUp(allowed: boolean, index = 0, stepMs = 50) {
  return allowed
    ? { animation: `${fadeUpKeyframes} 320ms ease-out both`, animationDelay: `${index * stepMs}ms` }
    : {}
}
