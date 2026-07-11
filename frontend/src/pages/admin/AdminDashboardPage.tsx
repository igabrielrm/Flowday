import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import { useAuth } from '../../auth/AuthContext';
import { api } from '../../api/client';
import PageHeader from '../../components/mui/PageHeader';
import PageStack from '../../components/mui/PageStack';
import type { AdminAnnouncement, AdminUser } from '../../types/admin';

type TabId = 'usuarios' | 'anuncios';

export default function AdminDashboardPage() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { user } = useAuth();
  const [tab, setTab] = useState<TabId>('usuarios');
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [announcements, setAnnouncements] = useState<{
    activos: AdminAnnouncement[];
    archivados: AdminAnnouncement[];
  }>({ activos: [], archivados: [] });
  const [userSearch, setUserSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [annForm, setAnnForm] = useState({ titulo: '', descripcion: '', fechaLimite: '' });
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    const [usersRes, annRes] = await Promise.all([
      api.admin.users(),
      api.admin.announcements(),
    ]);
    if (usersRes.ok && usersRes.data) setUsers(usersRes.data);
    if (annRes.ok && annRes.data) setAnnouncements(annRes.data);
    setLoading(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const filteredUsers = useMemo(() => {
    const q = userSearch.trim().toLowerCase();
    if (!q) return users;
    return users.filter(
      (u) => u.nombre.toLowerCase().includes(q) || u.correo.toLowerCase().includes(q),
    );
  }, [users, userSearch]);

  async function toggleRole(id: number) {
    setBusy(true);
    const res = await api.admin.toggleRole(id);
    if (!res.ok) setError(res.error || 'No se pudo cambiar el rol');
    else {
      setMessage('Rol actualizado');
      await load();
    }
    setBusy(false);
  }

  async function deleteUser(id: number) {
    if (!confirm('¿Eliminar este usuario?')) return;
    setBusy(true);
    const res = await api.admin.deleteUser(id);
    if (!res.ok) setError(res.error || 'No se pudo eliminar');
    else {
      setMessage('Usuario eliminado');
      await load();
    }
    setBusy(false);
  }

  async function publishAnnouncement(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    const res = await api.admin.createAnnouncement(annForm);
    if (!res.ok) setError(res.error || 'No se pudo publicar');
    else {
      setMessage('Anuncio publicado y notificado');
      setAnnForm({ titulo: '', descripcion: '', fechaLimite: '' });
      await load();
    }
    setBusy(false);
  }

  async function archiveAnnouncement(id: number) {
    setBusy(true);
    const res = await api.admin.archiveAnnouncement(id);
    if (!res.ok) setError(res.error || 'Error al archivar');
    else await load();
    setBusy(false);
  }

  async function restoreAnnouncement(id: number) {
    setBusy(true);
    const res = await api.admin.restoreAnnouncement(id);
    if (!res.ok) setError(res.error || 'Error al restaurar');
    else await load();
    setBusy(false);
  }

  async function deleteAnnouncement(id: number) {
    if (!confirm('¿Eliminar anuncio permanentemente?')) return;
    setBusy(true);
    const res = await api.admin.deleteAnnouncement(id);
    if (!res.ok) setError(res.error || 'No se pudo eliminar');
    else await load();
    setBusy(false);
  }

  const tabs: { id: TabId; label: string }[] = [
    { id: 'usuarios', label: 'Usuarios' },
    { id: 'anuncios', label: 'Anuncios' },
  ];

  return (
    <PageStack>
      <PageHeader
        title={`Admin — ${user?.nombre}`}
        subtitle="Gestión de usuarios y comunicados."
      />

      {message && <Alert severity="success">{message}</Alert>}
      {error && <Alert severity="error">{error}</Alert>}

      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        variant={isMobile ? 'fullWidth' : 'standard'}
        sx={{ borderBottom: 1, borderColor: 'divider' }}
      >
        {tabs.map((t) => (
          <Tab key={t.id} value={t.id} label={t.label} />
        ))}
      </Tabs>

      {loading ? (
        <Stack alignItems="center" py={4}>
          <CircularProgress size={28} />
        </Stack>
      ) : tab === 'usuarios' ? (
        <Card>
          <CardContent sx={{ p: { xs: 2, sm: 2.5 } }}>
            <TextField
              size="small"
              placeholder="Buscar por nombre o correo…"
              value={userSearch}
              onChange={(e) => setUserSearch(e.target.value)}
              fullWidth
              sx={{ mb: 2.5 }}
            />

            {isMobile ? (
              <Stack spacing={2}>
                {filteredUsers.map((u) => (
                  <Card key={u.id} variant="outlined">
                    <CardContent>
                      <Typography fontWeight={700}>{u.nombre}</Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {u.correo}
                      </Typography>
                      <Chip
                        label={u.rolDisplay || u.rol}
                        size="small"
                        color={u.rol === 'ADMIN' ? 'primary' : 'default'}
                        sx={{ mb: 1.5 }}
                      />
                      <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
                        {u.id !== user?.id && (
                          <Button size="small" disabled={busy} onClick={() => toggleRole(u.id)}>
                            {u.rol === 'ADMIN' ? 'Bajar a USER' : 'Ascender ADMIN'}
                          </Button>
                        )}
                        {u.rol !== 'ADMIN' && u.id !== user?.id && (
                          <Button size="small" color="error" disabled={busy} onClick={() => deleteUser(u.id)}>
                            Eliminar
                          </Button>
                        )}
                      </Stack>
                    </CardContent>
                  </Card>
                ))}
              </Stack>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Nombre</TableCell>
                      <TableCell>Correo</TableCell>
                      <TableCell>Rol</TableCell>
                      <TableCell>Acciones</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredUsers.map((u) => (
                      <TableRow key={u.id}>
                        <TableCell>{u.nombre}</TableCell>
                        <TableCell>{u.correo}</TableCell>
                        <TableCell>
                          <Chip
                            label={u.rolDisplay || u.rol}
                            size="small"
                            color={u.rol === 'ADMIN' ? 'primary' : 'default'}
                          />
                        </TableCell>
                        <TableCell>
                          <Stack direction="row" spacing={1}>
                            {u.id !== user?.id && (
                              <Button size="small" disabled={busy} onClick={() => toggleRole(u.id)}>
                                {u.rol === 'ADMIN' ? 'Bajar a USER' : 'Ascender ADMIN'}
                              </Button>
                            )}
                            {u.rol !== 'ADMIN' && u.id !== user?.id && (
                              <Button size="small" color="error" disabled={busy} onClick={() => deleteUser(u.id)}>
                                Eliminar
                              </Button>
                            )}
                          </Stack>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </Card>
      ) : (
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2.5}>
          <Card sx={{ flex: 1 }}>
            <CardContent component="form" onSubmit={publishAnnouncement} sx={{ p: { xs: 2, sm: 2.5 } }}>
              <Typography variant="h6" gutterBottom>
                Publicar anuncio
              </Typography>
              <Stack spacing={2}>
                <TextField
                  label="Título"
                  required
                  value={annForm.titulo}
                  onChange={(e) => setAnnForm({ ...annForm, titulo: e.target.value })}
                />
                <TextField
                  label="Descripción"
                  multiline
                  rows={3}
                  value={annForm.descripcion}
                  onChange={(e) => setAnnForm({ ...annForm, descripcion: e.target.value })}
                />
                <TextField
                  label="Fecha límite"
                  type="date"
                  required
                  value={annForm.fechaLimite}
                  onChange={(e) => setAnnForm({ ...annForm, fechaLimite: e.target.value })}
                  slotProps={{ inputLabel: { shrink: true } }}
                />
                <Button type="submit" variant="contained" disabled={busy}>
                  Publicar
                </Button>
              </Stack>
            </CardContent>
          </Card>
          <Card sx={{ flex: 1 }}>
            <CardContent sx={{ p: { xs: 2, sm: 2.5 } }}>
              <Typography variant="h6" gutterBottom>
                Activos
              </Typography>
              <Stack spacing={2} mb={3}>
                {announcements.activos.map((a) => (
                  <Box key={a.id}>
                    <Typography fontWeight={700}>{a.titulo}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {a.descripcion}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
                      Hasta {a.fechaLimite}
                    </Typography>
                    <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: 'wrap', gap: 1 }}>
                      <Button size="small" disabled={busy} onClick={() => archiveAnnouncement(a.id)}>
                        Archivar
                      </Button>
                      <Button size="small" color="error" disabled={busy} onClick={() => deleteAnnouncement(a.id)}>
                        Eliminar
                      </Button>
                    </Stack>
                  </Box>
                ))}
              </Stack>
              <Typography variant="h6" gutterBottom>
                Archivados
              </Typography>
              <Stack spacing={2}>
                {announcements.archivados.map((a) => (
                  <Box key={a.id}>
                    <Typography fontWeight={700}>{a.titulo}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {a.descripcion}
                    </Typography>
                    <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: 'wrap', gap: 1 }}>
                      <Button size="small" disabled={busy} onClick={() => restoreAnnouncement(a.id)}>
                        Restaurar
                      </Button>
                      <Button size="small" color="error" disabled={busy} onClick={() => deleteAnnouncement(a.id)}>
                        Eliminar
                      </Button>
                    </Stack>
                  </Box>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Stack>
      )}
    </PageStack>
  );
}
