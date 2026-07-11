import { createTheme, alpha } from '@mui/material/styles';

const flowPrimary = {
  main: '#5082ef',
  light: '#89b0f0',
  dark: '#2d57ad',
  contrastText: '#ffffff',
};

const flowSecondary = {
  main: '#6faf82',
  light: '#9dccaa',
  dark: '#4d9264',
  contrastText: '#ffffff',
};

export function createFlowdayTheme(mode: 'light' | 'dark') {
  const isLight = mode === 'light';

  return createTheme({
    palette: {
      mode,
      primary: flowPrimary,
      secondary: flowSecondary,
      background: {
        default: isLight ? '#f4f7fb' : '#0f172a',
        paper: isLight ? alpha('#ffffff', 0.92) : alpha('#1e293b', 0.92),
      },
      text: {
        primary: isLight ? '#1e293b' : '#f1f5f9',
        secondary: isLight ? '#64748b' : '#94a3b8',
      },
      divider: isLight ? alpha('#cbd5e1', 0.8) : alpha('#334155', 0.8),
    },
    shape: { borderRadius: 14 },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      h1: { fontFamily: '"Poppins", "Inter", sans-serif', fontWeight: 700 },
      h2: { fontFamily: '"Poppins", "Inter", sans-serif', fontWeight: 700 },
      h3: { fontFamily: '"Poppins", "Inter", sans-serif', fontWeight: 600 },
      h4: { fontFamily: '"Poppins", "Inter", sans-serif', fontWeight: 600 },
      h5: { fontFamily: '"Poppins", "Inter", sans-serif', fontWeight: 600 },
      h6: { fontFamily: '"Poppins", "Inter", sans-serif', fontWeight: 600 },
      button: { textTransform: 'none', fontWeight: 600 },
    },
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          body: {
            position: 'relative',
            backgroundImage: isLight
              ? 'url(/images/bg-pattern-light.png)'
              : 'url(/images/bg-pattern-dark.png)',
            backgroundRepeat: 'repeat',
            backgroundSize: '360px',
            backgroundAttachment: 'scroll',
            '@media (min-width: 900px)': {
              backgroundAttachment: 'fixed',
            },
            '&::before': {
              content: '""',
              position: 'fixed',
              inset: 0,
              zIndex: -1,
              pointerEvents: 'none',
              background: isLight
                ? 'linear-gradient(180deg, rgba(255,255,255,0.08) 0%, rgba(241,245,249,0.18) 100%)'
                : 'linear-gradient(180deg, rgba(2,6,23,0.55) 0%, rgba(2,6,23,0.72) 100%)',
            },
          },
        },
      },
      MuiIconButton: {
        styleOverrides: {
          root: {
            '@media (max-width: 899.95px)': {
              minWidth: 44,
              minHeight: 44,
            },
          },
        },
      },
      MuiButton: {
        defaultProps: { disableElevation: true },
        styleOverrides: {
          root: { borderRadius: 12, paddingInline: 18 },
        },
      },
      MuiCard: {
        defaultProps: { elevation: 0 },
        styleOverrides: {
          root: {
            border: `1px solid ${isLight ? alpha('#cbd5e1', 0.7) : alpha('#334155', 0.7)}`,
            backdropFilter: 'blur(10px)',
            transition: 'box-shadow 0.25s ease, transform 0.2s ease',
            '&:hover': {
              boxShadow: isLight
                ? '0 12px 32px -8px rgba(15, 23, 42, 0.12)'
                : '0 12px 32px -8px rgba(0, 0, 0, 0.45)',
            },
          },
        },
      },
      MuiPaper: {
        defaultProps: { elevation: 0 },
      },
      MuiTextField: {
        defaultProps: { variant: 'outlined', size: 'small', fullWidth: true },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backdropFilter: 'blur(12px)',
            backgroundColor: isLight ? alpha('#ffffff', 0.82) : alpha('#0f172a', 0.82),
          },
        },
      },
      MuiDrawer: {
        styleOverrides: {
          paper: {
            backdropFilter: 'blur(14px)',
            backgroundColor: isLight ? alpha('#ffffff', 0.94) : alpha('#0f172a', 0.94),
          },
        },
      },
      MuiBottomNavigation: {
        styleOverrides: {
          root: {
            backdropFilter: 'blur(12px)',
            backgroundColor: isLight ? alpha('#ffffff', 0.92) : alpha('#0f172a', 0.92),
          },
        },
      },
      MuiBottomNavigationAction: {
        styleOverrides: {
          root: {
            minWidth: 56,
            paddingTop: 6,
            '& .MuiBottomNavigationAction-label': {
              fontSize: '0.65rem',
            },
          },
        },
      },
      MuiTab: {
        styleOverrides: {
          root: { textTransform: 'none', fontWeight: 600, minHeight: 48 },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            backgroundImage: 'none',
            backdropFilter: 'none',
            WebkitBackdropFilter: 'none',
            bgcolor: isLight ? '#ffffff' : '#0f172a',
          },
        },
      },
      MuiBackdrop: {
        styleOverrides: {
          root: {
            backgroundColor: isLight ? 'rgba(15, 23, 42, 0.35)' : 'rgba(2, 6, 23, 0.72)',
          },
        },
      },
    },
  });
}
