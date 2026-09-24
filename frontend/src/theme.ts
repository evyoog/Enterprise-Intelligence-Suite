import { createTheme, type Theme } from '@mui/material/styles'

// Shared shape/typography — only the palette actually changes between modes.
const base = {
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: '"Inter", system-ui, "Segoe UI", Roboto, sans-serif',
    h1: { fontWeight: 700 },
    button: { textTransform: 'none' as const, fontWeight: 600 },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 9 },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: { borderRadius: 9 },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
  },
}

// Phase 21: real light/dark themes (previously hardcoded light-only "by
// design") — same brand blue accent in both, everything else re-tuned for
// contrast in dark mode rather than just flipping background/text.
export function getTheme(mode: 'light' | 'dark'): Theme {
  if (mode === 'dark') {
    return createTheme({
      ...base,
      palette: {
        mode: 'dark',
        primary: {
          main: '#60a5fa',
          dark: '#3b82f6',
          light: '#93c5fd',
          contrastText: '#0b1220',
        },
        background: {
          default: '#0f172a',
          paper: '#1e293b',
        },
        text: {
          primary: '#f1f5f9',
          secondary: '#94a3b8',
        },
        divider: '#334155',
      },
    })
  }

  return createTheme({
    ...base,
    palette: {
      mode: 'light',
      primary: {
        main: '#2563eb',
        dark: '#1d4ed8',
        light: '#3b82f6',
        contrastText: '#ffffff',
      },
      background: {
        default: '#f8fafc',
        paper: '#ffffff',
      },
      text: {
        primary: '#1e293b',
        secondary: '#64748b',
      },
      divider: '#e2e8f0',
    },
  })
}
