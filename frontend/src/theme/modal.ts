import { alpha, Theme } from '@mui/material/styles';
import { SxProps } from '@mui/material';

/** Fondo sólido para modales — no deja ver el contenido detrás. */
export function modalPaper(theme: Theme): SxProps<Theme> {
  const isLight = theme.palette.mode === 'light';
  return {
    bgcolor: isLight ? '#ffffff' : '#0f172a',
    backgroundImage: 'none',
    backdropFilter: 'none',
    WebkitBackdropFilter: 'none',
    border: `1px solid ${isLight ? alpha('#cbd5e1', 0.9) : alpha('#334155', 0.9)}`,
    boxShadow: isLight
      ? '0 24px 48px -12px rgba(15, 23, 42, 0.18)'
      : '0 24px 48px -12px rgba(0, 0, 0, 0.55)',
  };
}

/** MUI v9: usar slotProps en Dialog/Menu en lugar de PaperProps. */
export function modalSlotProps(theme: Theme, extra?: SxProps<Theme>) {
  return {
    paper: {
      sx: {
        ...modalPaper(theme),
        ...extra,
      },
    },
  };
}
