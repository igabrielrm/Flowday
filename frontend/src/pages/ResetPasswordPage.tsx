import { FormEvent, useEffect, useState } from 'react';
import { Link as RouterLink, Navigate } from 'react-router-dom';
import { Alert, Button, CircularProgress, Stack, TextField, Typography } from '@mui/material';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/client';
import AuthShell from '../components/mui/AuthShell';

export default function ResetPasswordPage() {
  const { user } = useAuth();
  const [active, setActive] = useState<boolean | null>(null);
  const [contrasenaNueva, setContrasenaNueva] = useState('');
  const [contrasenaConfirmacion, setContrasenaConfirmacion] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.resetPasswordSession().then((res) => {
      if (res.ok && res.data) setActive(res.data.active);
      else setActive(false);
    });
  }, []);

  if (user) return <Navigate to="/" replace />;
  if (active === false) return <Navigate to="/forgot-password" replace />;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    const res = await api.resetPassword(contrasenaNueva, contrasenaConfirmacion);
    if (!res.ok) setError(res.error || 'No se pudo actualizar');
    else setDone(true);
    setSubmitting(false);
  }

  if (active === null) {
    return (
      <AuthShell>
        <Stack alignItems="center" py={4}>
          <CircularProgress />
        </Stack>
      </AuthShell>
    );
  }

  if (done) {
    return (
      <AuthShell>
        <Stack spacing={2} textAlign="center">
          <Typography variant="h5">Contraseña actualizada</Typography>
          <Button component={RouterLink} to="/login" variant="contained" size="large">
            Iniciar sesión
          </Button>
        </Stack>
      </AuthShell>
    );
  }

  return (
    <AuthShell>
      <Stack component="form" spacing={2.5} onSubmit={onSubmit}>
        <Typography variant="h4" color="primary.main" textAlign="center">
          Nueva contraseña
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}

        <TextField
          label="Nueva contraseña"
          type="password"
          value={contrasenaNueva}
          onChange={(e) => setContrasenaNueva(e.target.value)}
          required
          inputProps={{ minLength: 8 }}
        />
        <TextField
          label="Confirmar contraseña"
          type="password"
          value={contrasenaConfirmacion}
          onChange={(e) => setContrasenaConfirmacion(e.target.value)}
          required
          inputProps={{ minLength: 8 }}
        />

        <Button type="submit" variant="contained" size="large" disabled={submitting}>
          {submitting ? 'Guardando…' : 'Guardar contraseña'}
        </Button>
      </Stack>
    </AuthShell>
  );
}
