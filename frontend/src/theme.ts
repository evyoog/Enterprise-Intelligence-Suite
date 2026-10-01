import { alpha, createTheme, type Theme } from '@mui/material/styles'

// Shared shape/typography — only the palette actually changes between modes.
// C60: corporate standard — firmer headings, tinted table headers, gradient
// primary buttons and tab indicator, accent-bordered alerts, rounder
// dialogs and cards. Every screen picks these up without its own styling.
const base = {
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: '"Inter", system-ui, "Segoe UI", Roboto, sans-serif',
    h1: { fontWeight: 800, letterSpacing: '-0.02em' },
    h2: { fontWeight: 800, letterSpacing: '-0.02em' },
    h3: { fontWeight: 800, letterSpacing: '-0.015em' },
    h4: { fontWeight: 800, letterSpacing: '-0.015em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 700 },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    overline: { fontWeight: 700, letterSpacing: '0.08em' },
    button: { textTransform: 'none' as const, fontWeight: 600 },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 9 },
        containedPrimary: ({ theme }: { theme: Theme }) => ({
          backgroundImage: `linear-gradient(135deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
          boxShadow: `0 6px 16px -8px ${alpha(theme.palette.primary.main, 0.7)}`,
          '&:hover': {
            backgroundImage: `linear-gradient(135deg, ${theme.palette.primary.dark}, ${theme.palette.secondary.dark})`,
            boxShadow: `0 8px 20px -8px ${alpha(theme.palette.primary.main, 0.8)}`,
          },
          '&.Mui-disabled': { backgroundImage: 'none' },
        }),
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
        rounded: { borderRadius: 14 },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: ({ theme }: { theme: Theme }) => ({
          borderRadius: 14,
          border: `1px solid ${theme.palette.divider}`,
          boxShadow: theme.palette.mode === 'dark' ? 'none' : '0 1px 2px rgba(16,24,40,.04), 0 4px 16px -8px rgba(16,24,40,.08)',
        }),
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: ({ theme }: { theme: Theme }) => ({
          height: 3,
          borderRadius: 3,
          backgroundImage: `linear-gradient(90deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
        }),
      },
    },
    MuiTab: {
      styleOverrides: {
        root: { fontWeight: 600, minHeight: 48 },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: ({ theme }: { theme: Theme }) => ({
          fontWeight: 700,
          fontSize: 12,
          letterSpacing: '0.04em',
          textTransform: 'uppercase' as const,
          color: theme.palette.text.secondary,
          backgroundColor: alpha(theme.palette.primary.main, theme.palette.mode === 'dark' ? 0.12 : 0.05),
          borderBottom: `1px solid ${alpha(theme.palette.primary.main, 0.25)}`,
        }),
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: ({ theme }: { theme: Theme }) => ({
          '&.MuiTableRow-hover:hover, tbody &:hover': { backgroundColor: alpha(theme.palette.primary.main, 0.035) },
        }),
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, borderRadius: 8 },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 10, alignItems: 'center' },
        standard: ({ ownerState, theme }: { ownerState: { severity?: string }; theme: Theme }) => {
          const color = theme.palette[(ownerState.severity ?? 'info') as 'info' | 'success' | 'warning' | 'error'].main
          return { borderLeft: `4px solid ${color}` }
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { borderRadius: 16 },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: { fontWeight: 700 },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { fontSize: 12, borderRadius: 8 },
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
        success: { main: '#34d399' },
        warning: { main: '#fbbf24' },
        error: { main: '#fb7185' },
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
      success: { main: '#047857' },
      warning: { main: '#b45309' },
      error: { main: '#e11d48' },
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
