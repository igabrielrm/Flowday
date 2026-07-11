import { FormEvent, useEffect, useState } from 'react';
import { Link as RouterLink, Navigate, useSearchParams } from 'react-router-dom';
import { Alert, Button, Link, Stack, TextField, Typography } from '@mui/material';
import { useAuth } from '../../auth/AuthContext';
import { api } from '../../api/client';
import AuthShell from '../../components/mui/AuthShell';

export default function AdminLoginPage() {
  const { user, refresh } = useAuth();
  const [searchParams] = useSearchParams();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (searchParams.get('error') === 'admin') {
      setError('Los administradores deben usar este acceso interno.');
    }
  }, [searchParams]);

  if (user?.rol === 'ADMIN') return <Navigate to="/admin" replace />;
  if (user) return <Navigate to="/" replace />;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    const res = await api.adminLogin(correo, contrasena);
    if (!res.ok || !res.data) {
      setError(res.error || 'No se pudo iniciar sesión');
      setSubmitting(false);
      return;
    }
    await refresh();
    setSubmitting(false);
    window.location.href = '/app/admin';
  }

  return (
    <AuthShell>
      <Stack component="form" spacing={2.5} onSubmit={onSubmit}>
        <Typography variant="overline" color="text.secondary" textAlign="center">
          Personal autorizado
        </Typography>
        <Typography variant="h4" color="primary.main" textAlign="center">
          Acceso interno
        </Typography>
        <Typography variant="body2" color="text.secondary" textAlign="center">
          Panel de administración Flowday
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}

        <TextField
          label="Correo"
          type="email"
          value={correo}
          onChange={(e) => setCorreo(e.target.value)}
          required
          autoComplete="username"
          placeholder="admin@ejemplo.com"
        />
        <TextField
          label="Contraseña"
          type="password"
          value={contrasena}
          onChange={(e) => setContrasena(e.target.value)}
          required
          inputProps={{ minLength: 8 }}
          autoComplete="current-password"
        />

        <Button type="submit" variant="contained" color="secondary" size="large" disabled={submitting}>
          {submitting ? 'Verificando…' : 'Ingresar'}
        </Button>

        <Typography variant="body2" color="text.secondary" textAlign="center">
          <Link component={RouterLink} to="/login">
            Volver al login de usuarios
          </Link>
        </Typography>
      </Stack>
    </AuthShell>
  );
}
