import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  IconButton,
  Link,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
  useTheme,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { api } from '../api/client';
import { modalSlotProps } from '../theme/modal';
import type { ActividadDetail } from '../types/activity';
import { estadoLabel, formatDate, tipoLabel } from '../types/activity';

type Props = {
  activityId: number | null;
  onClose: () => void;
  onChanged?: () => void;
};

type Resource = { titulo: string; url?: string; descripcion?: string };

function estadoChipColor(estado: string): 'default' | 'success' | 'warning' | 'info' {
  if (estado === 'COMPLETADA') return 'success';
  if (estado === 'EN_PROCESO') return 'info';
  if (estado === 'REAGENDADA') return 'warning';
  return 'default';
}

export default function ActivityDetailModal({ activityId, onClose, onChanged }: Props) {
  const navigate = useNavigate();
  const theme = useTheme();
  const [detail, setDetail] = useState<ActividadDetail | null>(null);
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!activityId) {
      setDetail(null);
      setResources([]);
      return;
    }
    setLoading(true);
    setError(null);
    api.activities.get(activityId).then(async (res) => {
      if (!res.ok || !res.data) {
        setError(res.error || 'No se pudo cargar la actividad');
        setDetail(null);
        setLoading(false);
        return;
      }
      setDetail(res.data);
      if (res.data.tipo === 'DEBER' || res.data.tipo === 'EXAMEN') {
        const ia = await api.ia.activityResources(activityId);
        if (ia.ok && ia.data?.recursos) setResources(ia.data.recursos);
      }
      setLoading(false);
    });
  }, [activityId]);

  async function complete() {
    if (!activityId) return;
    setBusy(true);
    const res = await api.activities.updateStatus(activityId, 'COMPLETADA');
    if (!res.ok) setError(res.error || 'No se pudo completar');
    else {
      onChanged?.();
      onClose();
    }
    setBusy(false);
  }

  return (
    <Dialog open={!!activityId} onClose={onClose} maxWidth="sm" fullWidth slotProps={modalSlotProps(theme)}>
      <DialogTitle sx={{ pr: 6, pb: 1 }}>
        <IconButton
          aria-label="Cerrar"
          onClick={onClose}
          sx={{ position: 'absolute', right: 8, top: 8 }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ pt: 1 }}>
        {loading ? (
          <Typography color="text.secondary">Cargando…</Typography>
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : detail ? (
          <Stack spacing={2.5}>
            <Box>
              <Stack direction="row" spacing={1} alignItems="center" mb={1}>
                <Box
                  sx={{
                    width: 12,
                    height: 12,
                    borderRadius: '50%',
                    bgcolor: detail.color || '#5082ef',
                  }}
                />
                <Typography variant="overline" color="text.secondary" letterSpacing={1}>
                  {tipoLabel(detail.tipo)}
                </Typography>
              </Stack>
              <Typography variant="h5" fontWeight={700} lineHeight={1.3}>
                {detail.titulo}
              </Typography>
              <Chip
                label={estadoLabel(detail.estado)}
                size="small"
                color={estadoChipColor(detail.estado)}
                sx={{ mt: 1.5 }}
              />
            </Box>

            <Divider />

            <Stack direction="row" flexWrap="wrap" gap={2}>
              {[
                { label: 'Fecha', value: formatDate(detail.fechaInicio) },
                { label: 'Hora', value: detail.horaInicio?.slice(0, 5) || '—' },
                { label: 'Prioridad', value: detail.prioridad || '—' },
                { label: 'Entrega', value: formatDate(detail.fechaEntrega) },
              ].map((row) => (
                <Box key={row.label} sx={{ width: { xs: '100%', sm: 'calc(50% - 8px)' } }}>
                  <Typography variant="caption" color="text.secondary" display="block" mb={0.5}>
                    {row.label}
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {row.value}
                  </Typography>
                </Box>
              ))}
            </Stack>

            {detail.descripcion && (
              <>
                <Divider />
                <Box>
                  <Typography variant="caption" color="text.secondary" display="block" mb={0.5}>
                    Descripción
                  </Typography>
                  <Typography variant="body2">{detail.descripcion}</Typography>
                </Box>
              </>
            )}

            {resources.length > 0 && (
              <>
                <Divider />
                <Box>
                  <Typography variant="subtitle2" fontWeight={700} gutterBottom>
                    Recursos recomendados
                  </Typography>
                  <List dense disablePadding>
                    {resources.map((r, i) => (
                      <ListItem key={i} disableGutters sx={{ py: 0.5 }}>
                        <ListItemText
                          primary={
                            r.url ? (
                              <Link href={r.url} target="_blank" rel="noreferrer">
                                {r.titulo}
                              </Link>
                            ) : (
                              r.titulo
                            )
                          }
                          secondary={r.descripcion}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              </>
            )}
          </Stack>
        ) : null}
      </DialogContent>

      {detail && (
        <DialogActions sx={{ px: 3, py: 2, gap: 1 }}>
          {detail.estado !== 'COMPLETADA' && (
            <Button variant="contained" disabled={busy} onClick={complete}>
              Finalizar
            </Button>
          )}
          {detail.puedeEditar && (
            <Button
              disabled={busy}
              onClick={() => {
                onClose();
                navigate(`/activities/${detail.id}/edit`);
              }}
            >
              Editar
            </Button>
          )}
        </DialogActions>
      )}
    </Dialog>
  );
}
