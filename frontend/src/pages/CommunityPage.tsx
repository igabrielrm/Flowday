import { Link as RouterLink } from 'react-router-dom';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { api } from '../api/client';
import PageHeader from '../components/mui/PageHeader';
import PageStack from '../components/mui/PageStack';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import type { CommunityUser } from '../types/community';
import { userInitials } from '../types/community';
import { assetUrl } from '../platform';

function RelationActions({
  item,
  busyId,
  onAction,
}: {
  item: CommunityUser;
  busyId: number | null;
  onAction: (action: string, userId: number, conexionId?: number | null) => void;
}) {
  const { user, estadoRelacion, conexionId } = item;

  if (estadoRelacion === 'CONECTADO') {
    return (
      <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
        <Chip label="Conectado" color="success" size="small" />
        <Button size="small" component={RouterLink} to={`/chat?user=${user.id}`}>
          Chat
        </Button>
      </Stack>
    );
  }
  if (estadoRelacion === 'SOLICITUD_ENVIADA') {
    return (
      <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
        <Chip label="Solicitud enviada" color="info" size="small" />
        {conexionId && (
          <Button
            size="small"
            disabled={busyId === user.id}
            onClick={() => onAction('cancel', user.id, conexionId)}
          >
            Cancelar
          </Button>
        )}
      </Stack>
    );
  }
  if (estadoRelacion === 'SOLICITUD_RECIBIDA' && conexionId) {
    return (
      <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
        <Button
          size="small"
          variant="contained"
          disabled={busyId === user.id}
          onClick={() => onAction('accept', user.id, conexionId)}
        >
          Aceptar
        </Button>
        <Button
          size="small"
          disabled={busyId === user.id}
          onClick={() => onAction('reject', user.id, conexionId)}
        >
          Rechazar
        </Button>
      </Stack>
    );
  }
  return (
    <Button
      size="small"
      variant="contained"
      disabled={busyId === user.id}
      onClick={() => onAction('request', user.id)}
    >
      {busyId === user.id ? 'Enviando…' : 'Solicitar amistad'}
    </Button>
  );
}

function UserRow({
  item,
  busyId,
  onAction,
}: {
  item: CommunityUser;
  busyId: number | null;
  onAction: (action: string, userId: number, conexionId?: number | null) => void;
}) {
  const { user } = item;

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
          {user.foto ? (
            <Avatar src={assetUrl(user.foto)} alt="" sx={{ width: 48, height: 48 }} />
          ) : (
            <Avatar sx={{ width: 48, height: 48 }}>{userInitials(user.nombre)}</Avatar>
          )}
          <Box flex={1} minWidth={0}>
            <Typography fontWeight={700} color="text.primary" noWrap>
              {user.nombre}
            </Typography>
            <Typography variant="body2" color="text.secondary" noWrap>
              {user.correo}
            </Typography>
          </Box>
          <Box>
            <RelationActions item={item} busyId={busyId} onAction={onAction} />
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}

export default function CommunityPage() {
  const [users, setUsers] = useState<CommunityUser[]>([]);
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebouncedValue(query);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = useCallback(async (q?: string) => {
    setLoading(true);
    setError(null);
    const usersRes = await api.community.users(q);
    if (usersRes.ok && usersRes.data) setUsers(usersRes.data);
    else setError(usersRes.error || 'No se pudieron cargar los usuarios');
    setLoading(false);
  }, []);

  useEffect(() => {
    load(debouncedQuery.trim() || undefined);
  }, [debouncedQuery, load]);

  const filteredUsers = useMemo(() => users, [users]);

  const pendingRequests = useMemo(
    () => users.filter((u) => u.estadoRelacion === 'SOLICITUD_RECIBIDA'),
    [users],
  );

  async function handleAction(action: string, userId: number, conexionId?: number | null) {
    setBusyId(userId);
    setError(null);
    let res;
    if (action === 'request') res = await api.community.connect(userId);
    else if (action === 'accept' && conexionId) res = await api.community.accept(conexionId);
    else if (action === 'reject' && conexionId) res = await api.community.reject(conexionId);
    else if (action === 'cancel' && conexionId) res = await api.community.removeConnection(conexionId);
    else res = { ok: false, error: 'Acción no válida' };

    if (!res?.ok) setError(res?.error || 'No se pudo completar la acción');
    else await load(debouncedQuery.trim() || undefined);
    setBusyId(null);
  }

  return (
    <PageStack>
      <PageHeader
        title="Comunidad"
        subtitle="Envía solicitudes de amistad y chatea con tus conexiones"
      />

      {error && <Alert severity="error">{error}</Alert>}

      {pendingRequests.length > 0 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Solicitudes pendientes ({pendingRequests.length})
            </Typography>
            <Stack spacing={2}>
              {pendingRequests.map((item) => (
                <UserRow key={item.user.id} item={item} busyId={busyId} onAction={handleAction} />
              ))}
            </Stack>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Buscar compañeros
          </Typography>
          <TextField
            placeholder="Buscar por nombre o correo…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            fullWidth
            size="small"
            sx={{ mb: 2 }}
          />

          {loading ? (
            <Stack alignItems="center" py={3}>
              <CircularProgress size={28} />
            </Stack>
          ) : filteredUsers.length === 0 ? (
            <Typography color="text.secondary">No hay usuarios para mostrar.</Typography>
          ) : (
            <Stack spacing={2}>
              {filteredUsers.map((item) => (
                <UserRow key={item.user.id} item={item} busyId={busyId} onAction={handleAction} />
              ))}
            </Stack>
          )}
        </CardContent>
      </Card>
    </PageStack>
  );
}
