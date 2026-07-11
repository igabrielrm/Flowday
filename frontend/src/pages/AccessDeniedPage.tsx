import { Link as RouterLink } from 'react-router-dom';
import { Box, Button, Stack, Typography } from '@mui/material';
import AuthShell from '../components/mui/AuthShell';

export default function AccessDeniedPage() {
  return (
    <AuthShell>
      <Stack spacing={2.5} textAlign="center">
        <Box fontSize={48} aria-hidden>
          🚫
        </Box>
        <Typography variant="h4">Acceso denegado</Typography>
        <Typography color="text.secondary">
          No tienes permisos para acceder a esta página. Si crees que es un error, contacta al
          administrador.
        </Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} justifyContent="center">
          <Button component={RouterLink} to="/" variant="contained">
            Volver al inicio
          </Button>
          <Button component={RouterLink} to="/login" variant="outlined">
            Iniciar sesión
          </Button>
        </Stack>
      </Stack>
    </AuthShell>
  );
}
