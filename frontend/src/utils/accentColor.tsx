import type { ComponentType } from 'react'
import { Boxes, Cloud, Cpu, Database, Globe, Layers, Puzzle, Rocket, ShieldCheck, Sparkles, SquareStack, Zap } from 'lucide-react'

export interface Accent {
  /** Tinted tile background — always paired with `fg` for contrast. */
  bg: string
  /** Icon / hyperlink color, drawn from the same hue as `bg`. */
  fg: string
}

/**
 * A services-console palette: enough distinct, legible hues that a grid of
 * platform/app tiles reads as varied at a glance (the way AWS's own service
 * catalog color-codes categories) without any one of them being loud enough
 * to fight the page's actual content.
 */
const ACCENTS: Accent[] = [
  { bg: '#EEF4FF', fg: '#2563EB' }, // blue
  { bg: '#F5F0FF', fg: '#7C3AED' }, // violet
  { bg: '#ECFDF5', fg: '#0D9488' }, // teal
  { bg: '#FFF4EA', fg: '#EA580C' }, // orange
  { bg: '#FDF0F7', fg: '#DB2777' }, // pink
  { bg: '#EEF1FF', fg: '#4F46E5' }, // indigo
  { bg: '#F0FDF4', fg: '#16A34A' }, // green
  { bg: '#FFFBEA', fg: '#B45309' }, // amber
  { bg: '#FDF0F0', fg: '#DC2626' }, // red
  { bg: '#ECFEFF', fg: '#0E7490' }, // cyan
]

const ICONS: ComponentType<{ size?: number; color?: string; strokeWidth?: number }>[] = [
  Boxes, Database, Cloud, Cpu, Layers, Rocket, ShieldCheck, Puzzle, Globe, Zap, SquareStack, Sparkles,
]

function hashOf(seed: string): number {
  let hash = 0
  for (let i = 0; i < seed.length; i++) {
    hash = seed.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

/** Deterministic per-name accent — the same platform/app always lands on the same hue and icon. */
export function accentFor(seed: string): Accent {
  return ACCENTS[hashOf(seed) % ACCENTS.length]
}

export function iconFor(seed: string) {
  return ICONS[hashOf(seed + '#icon') % ICONS.length]
}
