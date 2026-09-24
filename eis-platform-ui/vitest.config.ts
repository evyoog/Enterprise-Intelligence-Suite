import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// Phase 6 (2026.3.3): kept separate from vite.config.ts (which only ever
// needs to know about `vite dev`/`vite build`) rather than merging test
// config into it — one file per concern.
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    // React Testing Library's automatic per-test DOM cleanup only registers
    // itself when it can see a global afterEach — without this, state (and
    // rendered nodes) from one test leak into the next.
    globals: true,
  },
})
