import { ReactNode, useEffect, useMemo, useState } from 'react';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { createFlowdayTheme } from './flowdayTheme';

export const THEME_EVENT = 'flowday-theme-change';

function readThemeMode(): 'light' | 'dark' {
  const attr = document.documentElement.getAttribute('data-theme');
  if (attr === 'light' || attr === 'dark') return attr;
  const saved = localStorage.getItem('theme');
  return saved === 'light' ? 'light' : 'dark';
}

export default function MuiThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<'light' | 'dark'>(readThemeMode);

  useEffect(() => {
    const sync = () => setMode(readThemeMode());
    window.addEventListener(THEME_EVENT, sync);
    window.addEventListener('storage', sync);
    return () => {
      window.removeEventListener(THEME_EVENT, sync);
      window.removeEventListener('storage', sync);
    };
  }, []);

  const theme = useMemo(() => createFlowdayTheme(mode), [mode]);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
}
