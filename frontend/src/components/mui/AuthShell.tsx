import { ReactNode } from 'react';
import { Box, Container, Paper, alpha, useTheme } from '@mui/material';

type Props = {
  children: ReactNode;
  maxWidth?: number;
};

export default function AuthShell({ children, maxWidth = 440 }: Props) {
  const theme = useTheme();
  const isLight = theme.palette.mode === 'light';

  return (
    <Box
      sx={{
        minHeight: '100dvh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: { xs: 2, sm: 3 },
        pt: { xs: 'max(16px, env(safe-area-inset-top))', sm: 3 },
        pb: { xs: 'max(16px, env(safe-area-inset-bottom))', sm: 3 },
        position: 'relative',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          bgcolor: isLight ? alpha('#f8fafc', 0.9) : alpha('#0f172a', 0.9),
          pointerEvents: 'none',
        }}
      />
      <Container maxWidth={false} sx={{ position: 'relative', zIndex: 1, maxWidth }}>
        <Paper
          elevation={0}
          sx={{
            p: { xs: 3, sm: 4 },
            borderRadius: 3,
            border: `1px solid ${theme.palette.divider}`,
          }}
        >
          {children}
        </Paper>
      </Container>
    </Box>
  );
}
