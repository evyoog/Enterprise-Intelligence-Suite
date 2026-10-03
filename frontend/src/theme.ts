import { alpha, createTheme, type Theme } from '@mui/material/styles'

// Shared shape/typography — only the palette actually changes between modes.
// C60: corporate standard — firmer headings, tinted table headers, gradient
// primary buttons and tab indicator, accent-bordered alerts, rounder
// dialogs and cards. Every screen picks these up without its own styling.
/**
 * C66 design system (2026-10-03): a calm enterprise SaaS look — light
 * neutral background, white surfaces with subtle borders and soft shadows,
 * one indigo primary, solid buttons (no gradients), 12 px cards. Colour is
 * kept for status, category, product identity and primary actions.
 */
const base = {
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: '"Inter", system-ui, "Segoe UI", Roboto, sans-serif',
    h1: { fontWeight: 700, letterSpacing: '-0.02em' },
    h2: { fontWeight: 700, letterSpacing: '-0.02em' },
    h3: { fontWeight: 700, letterSpacing: '-0.015em' },
    h4: { fontWeight: 700, letterSpacing: '-0.015em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    overline: { fontWeight: 600, letterSpacing: '0.08em', fontSize: 11 },
    button: { textTransform: 'none' as const, fontWeight: 600 },
  },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 8 },
        outlined: ({ theme }: { theme: Theme }) => ({
          borderColor: theme.palette.divider,
          '&:hover': { borderColor: alpha(theme.palette.primary.main, 0.5), backgroundColor: alpha(theme.palette.primary.main, 0.04) },
        }),
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: ({ theme }: { theme: Theme }) => ({
          borderRadius: 8,
          backgroundColor: theme.palette.background.paper,
        }),
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
        rounded: { borderRadius: 12 },
        outlined: ({ theme }: { theme: Theme }) => ({
          borderColor: theme.palette.divider,
          boxShadow: theme.palette.mode === 'dark' ? 'none' : '0 1px 2px rgba(15, 23, 42, 0.04)',
        }),
      },
    },
    MuiCard: {
      styleOverrides: {
        root: ({ theme }: { theme: Theme }) => ({
          borderRadius: 12,
          border: `1px solid ${theme.palette.divider}`,
          boxShadow: theme.palette.mode === 'dark' ? 'none' : '0 1px 2px rgba(15, 23, 42, 0.04)',
        }),
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: ({ theme }: { theme: Theme }) => ({
          height: 2,
          borderRadius: 2,
          backgroundColor: theme.palette.primary.main,
        }),
      },
    },
    MuiTab: {
      styleOverrides: {
        root: { fontWeight: 600, minHeight: 44 },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: ({ theme }: { theme: Theme }) => ({
          fontWeight: 600,
          fontSize: 12,
          letterSpacing: '0.03em',
          textTransform: 'uppercase' as const,
          color: theme.palette.text.secondary,
          backgroundColor: theme.palette.mode === 'dark' ? alpha('#ffffff', 0.03) : '#F8FAFC',
          borderBottom: `1px solid ${theme.palette.divider}`,
        }),
        root: ({ theme }: { theme: Theme }) => ({ borderColor: theme.palette.divider }),
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: ({ theme }: { theme: Theme }) => ({
          '&.MuiTableRow-hover:hover, tbody &:hover': { backgroundColor: alpha(theme.palette.primary.main, 0.03) },
        }),
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, borderRadius: 6 },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 10, alignItems: 'center' },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { borderRadius: 12 },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: { fontWeight: 700 },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { fontSize: 12, borderRadius: 6 },
      },
    },
    MuiSwitch: {
      styleOverrides: {
        switchBase: ({ theme }: { theme: Theme }) => ({ '&.Mui-focusVisible + .MuiSwitch-track': { outline: `2px solid ${theme.palette.primary.main}` } }),
      },
    },
  },
}

/** C66 brand: indigo. #6366F1 is the brand and default showcase colour;
 * #4F46E5 is used where white text sits on the colour (buttons), because
 * white on #6366F1 is just under the WCAG AA 4.5:1 ratio. */
export const BRAND_INDIGO = '#6366F1'

const BRAND = {
  primary: '#4F46E5',
  primaryDark: '#4338CA',
  primaryLight: BRAND_INDIGO,
  violet: '#7C3AED',
}

export function getTheme(mode: 'light' | 'dark'): Theme {
  if (mode === 'dark') {
    return createTheme({
      ...base,
      palette: {
        mode: 'dark',
        primary: {
          main: '#818CF8',
          dark: BRAND_INDIGO,
          light: '#A5B4FC',
          contrastText: '#0b1020',
        },
        secondary: {
          main: '#A78BFA',
          dark: BRAND.violet,
          light: '#C4B5FD',
          contrastText: '#0b1020',
        },
        info: { main: '#38BDF8' },
        success: { main: '#34D399' },
        warning: { main: '#FBBF24' },
        error: { main: '#FB7185' },
        background: {
          default: '#0B1020',
          paper: '#121829',
        },
        text: {
          primary: '#F1F5F9',
          secondary: '#94A3B8',
        },
        divider: '#232B3F',
      },
    })
  }

  return createTheme({
    ...base,
    palette: {
      mode: 'light',
      primary: {
        main: BRAND.primary,
        dark: BRAND.primaryDark,
        light: BRAND.primaryLight,
        contrastText: '#ffffff',
      },
      secondary: {
        main: BRAND.violet,
        dark: '#6D28D9',
        light: '#A78BFA',
        contrastText: '#ffffff',
      },
      info: { main: '#0369A1' },
      success: { main: '#047857' },
      warning: { main: '#B45309' },
      error: { main: '#DC2626' },
      background: {
        default: '#F7F8FC',
        paper: '#FFFFFF',
      },
      text: {
        primary: '#0F172A',
        secondary: '#64748B',
      },
      divider: '#E6E8F0',
    },
  })
}
