import { alpha, Theme } from '@mui/material/styles';
import { SxProps } from '@mui/material';

export function glassSurface(theme: Theme, opts?: { strong?: boolean }): SxProps<Theme> {
  const isLight = theme.palette.mode === 'light';
  const bg = isLight ? alpha('#ffffff', opts?.strong ? 0.88 : 0.72) : alpha('#1e293b', opts?.strong ? 0.9 : 0.78);
  return {
    background: bg,
    backdropFilter: 'blur(14px) saturate(160%)',
    WebkitBackdropFilter: 'blur(14px) saturate(160%)',
    border: `1px solid ${isLight ? alpha('#cbd5e1', 0.85) : alpha('#475569', 0.7)}`,
    boxShadow: isLight
      ? '0 8px 32px -8px rgba(15, 23, 42, 0.12), inset 0 1px 0 rgba(255,255,255,0.6)'
      : '0 8px 32px -8px rgba(0, 0, 0, 0.45), inset 0 1px 0 rgba(255,255,255,0.06)',
  };
}

export function glassChip(theme: Theme, active?: boolean): SxProps<Theme> {
  const isLight = theme.palette.mode === 'light';
  return {
    background: isLight ? alpha('#ffffff', 0.82) : alpha('#1e3a5f', 0.72),
    backdropFilter: 'blur(12px) saturate(150%)',
    WebkitBackdropFilter: 'blur(12px) saturate(150%)',
    border: `1px solid ${isLight ? alpha('#cbd5e1', 0.9) : alpha('#334155', 0.85)}`,
    color: isLight ? theme.palette.text.primary : '#e2e8f0',
    fontWeight: 600,
    borderRadius: 999,
    boxShadow: isLight
      ? 'inset 0 1px 0 rgba(255,255,255,0.7)'
      : 'inset 0 1px 0 rgba(255,255,255,0.05)',
    ...(active && {
      bgcolor: isLight ? alpha(theme.palette.primary.main, 0.14) : alpha(theme.palette.primary.main, 0.28),
      borderColor: alpha(theme.palette.primary.main, 0.5),
      color: isLight ? theme.palette.primary.dark : theme.palette.primary.light,
    }),
  };
}

export function glassField(theme: Theme): SxProps<Theme> {
  const isLight = theme.palette.mode === 'light';
  return {
    '& .MuiOutlinedInput-root': {
      background: isLight ? alpha('#ffffff', 0.82) : alpha('#1e3a5f', 0.65),
      backdropFilter: 'blur(12px)',
      borderRadius: 12,
    },
  };
}

export function glassButton(theme: Theme): SxProps<Theme> {
  const isLight = theme.palette.mode === 'light';
  return {
    ...glassSurface(theme),
    borderRadius: 12,
    textTransform: 'none',
    fontWeight: 600,
    color: theme.palette.text.primary,
    '&:hover': {
      bgcolor: isLight ? alpha('#ffffff', 0.92) : alpha('#334155', 0.92),
    },
  };
}
