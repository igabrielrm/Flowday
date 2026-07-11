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
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
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
