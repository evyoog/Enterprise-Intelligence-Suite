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

// C45: the same blue/violet/cyan triad already used on the public landing
// page (styles/landing.css's own --blue/--violet/--cyan) is now the whole
// app's palette — before this, the signed-in tool (MUI blue #2563eb) and the
// public site (blue/violet/cyan gradients) were two different color systems
// that happened to share a building. evyoog.com itself couldn't be reached
// from this environment (see docs/08-architecture/ui-ux-redesign.md), so
// this unifies what the app already had rather than guessing a new brand.
const BRAND = {
  blue: '#4c63ff',
  blueDark: '#3548d6',
  blueLight: '#7d8dff',
  violet: '#733dff',
  cyan: '#42d8ff',
}

export function getTheme(mode: 'light' | 'dark'): Theme {
  if (mode === 'dark') {
    return createTheme({
      ...base,
      palette: {
        mode: 'dark',
        primary: {
          main: BRAND.blueLight,
          dark: BRAND.blue,
          light: '#a6b1ff',
          contrastText: '#0b1220',
        },
        secondary: {
          main: '#a685ff',
          dark: BRAND.violet,
          light: '#c3aeff',
          contrastText: '#0b1220',
        },
        info: {
          main: BRAND.cyan,
        },
        background: {
          default: '#0f1117',
          paper: '#191c26',
        },
        text: {
          primary: '#f1f5f9',
          secondary: '#98a2b3',
        },
        divider: '#2b2f3d',
      },
    })
  }

  return createTheme({
    ...base,
    palette: {
      mode: 'light',
      primary: {
        main: BRAND.blue,
        dark: BRAND.blueDark,
        light: BRAND.blueLight,
        contrastText: '#ffffff',
      },
      secondary: {
        main: BRAND.violet,
        dark: '#5c2ed6',
        light: '#9a6bff',
        contrastText: '#ffffff',
      },
      info: {
        main: '#1090ad',
      },
      background: {
        default: '#f6f7fb',
        paper: '#ffffff',
      },
      text: {
        primary: '#181b27',
        secondary: '#5c6270',
      },
      divider: '#e4e6ee',
    },
  })
}
