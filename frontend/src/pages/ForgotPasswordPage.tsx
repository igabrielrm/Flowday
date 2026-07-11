import { FormEvent, useState } from 'react';
import { Link as RouterLink, Navigate } from 'react-router-dom';
import { Alert, Button, Link, Stack, TextField, Typography } from '@mui/material';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/client';
import AuthShell from '../components/mui/AuthShell';

export default function ForgotPasswordPage() {
  const { user } = useAuth();
  const [correo, setCorreo] = useState('');
  const [telefono, setTelefono] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (user) return <Navigate to="/" replace />;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    const res = await api.forgotPassword(correo, telefono);
    if (!res.ok) setError(res.error || 'No se pudo verificar');
    else window.location.href = '/app/reset-password';
    setSubmitting(false);
  }

  return (
    <AuthShell>
      <Stack component="form" spacing={2.5} onSubmit={onSubmit}>
        <Typography variant="h4" color="primary.main" textAlign="center">
          Recuperar contraseña
        </Typography>
        <Typography variant="body2" color="text.secondary" textAlign="center">
          Verifica tu identidad con correo y teléfono registrados.
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}

        <TextField
          label="Correo"
          type="email"
          value={correo}
          onChange={(e) => setCorreo(e.target.value)}
          required
        />
        <TextField
          label="Teléfono"
          value={telefono}
          onChange={(e) => setTelefono(e.target.value)}
          required
          inputProps={{ pattern: '\\d{10}', maxLength: 10 }}
        />

        <Button type="submit" variant="contained" size="large" disabled={submitting}>
          {submitting ? 'Verificando…' : 'Continuar'}
        </Button>

        <Typography variant="body2" color="text.secondary" textAlign="center">
          <Link component={RouterLink} to="/login">
            Volver al login
          </Link>
        </Typography>
      </Stack>
    </AuthShell>
  );
}
