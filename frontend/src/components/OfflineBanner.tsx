import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Snackbar,
  Stack,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useOfflineSync } from '../offline/useOfflineSync';

export default function OfflineBanner() {
  const { online, pending, entries, syncing, lastError, syncNow, retry, discard } = useOfflineSync();
  const [dismissed, setDismissed] = useState(false);
  const [detailsOpen, setDetailsOpen] = useState(false);

  const showOffline = !online;
  const showPending = online && pending > 0;
  const hasContent = showOffline || showPending || !!lastError;

  useEffect(() => {
    if (online) setDismissed(false);
  }, [online]);

  useEffect(() => {
    if (lastError) setDismissed(false);
  }, [lastError]);

  const open = hasContent && !dismissed;

  let message = '';
  let severity: 'warning' | 'info' | 'error' = 'warning';

  if (showOffline) {
    message =
      pending > 0
        ? `Sin conexión — ${pending} borrador${pending === 1 ? '' : 'es'} pendiente${pending === 1 ? '' : 's'} de sincronizar.`
        : 'Sin conexión — puedes crear borradores; se sincronizarán al reconectar.';
    severity = 'warning';
  } else if (syncing) {
    message = `Sincronizando ${pending} borrador${pending === 1 ? '' : 'es'}…`;
    severity = 'info';
  } else if (lastError) {
    message = lastError;
    severity = 'error';
  } else if (showPending) {
    message = `${pending} borrador${pending === 1 ? '' : 'es'} pendiente${pending === 1 ? '' : 's'} de sincronizar.`;
    severity = 'info';
  }

  return (
    <>
      <Snackbar
        open={open}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        sx={{ top: { xs: 'calc(56px + env(safe-area-inset-top))', sm: 'calc(64px + env(safe-area-inset-top))' } }}
      >
        <Alert
        severity={severity}
        variant="filled"
        sx={{ width: '100%', alignItems: 'center', pr: 1 }}
        action={
          <Stack direction="row" alignItems="center" spacing={0.5}>
            {online && pending > 0 && !syncing && (
              <Button color="inherit" size="small" onClick={() => syncNow()}>
                Sincronizar
              </Button>
            )}
            {pending > 0 && (
              <Button color="inherit" size="small" onClick={() => setDetailsOpen(true)}>
                Detalles
              </Button>
            )}
            <IconButton
              aria-label="Cerrar aviso"
              color="inherit"
              size="small"
              onClick={() => setDismissed(true)}
            >
              <CloseIcon fontSize="small" />
            </IconButton>
          </Stack>
        }
        >
          <Stack spacing={0.25}>{message}</Stack>
        </Alert>
      </Snackbar>
      <Dialog open={detailsOpen} onClose={() => setDetailsOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Sincronización pendiente</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={1.5}>
            {entries.length === 0 && (
              <Typography color="text.secondary">No hay cambios pendientes.</Typography>
            )}
            {entries.map((entry) => (
              <Stack key={entry.id} spacing={0.75} sx={{ p: 1.5, border: 1, borderColor: 'divider', borderRadius: 2 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center" gap={1}>
                  <Typography variant="body2" fontWeight={700}>{entry.label}</Typography>
                  <Chip
                    size="small"
                    color={entry.status === 'CONFLICT' || entry.status === 'FAILED' ? 'error' : 'default'}
                    label={
                      entry.status === 'CONFLICT'
                        ? 'Conflicto'
                        : entry.status === 'FAILED'
                          ? 'Falló'
                          : entry.status === 'SYNCING'
                            ? 'Sincronizando'
                            : 'Pendiente'
                    }
                  />
                </Stack>
                {entry.lastError && (
                  <Typography variant="caption" color="error">{entry.lastError}</Typography>
                )}
                {(entry.status === 'CONFLICT' || entry.status === 'FAILED') && (
                  <Stack direction="row" spacing={1}>
                    <Button size="small" onClick={() => retry(entry.id)} disabled={!online}>Reintentar</Button>
                    <Button
                      size="small"
                      color="inherit"
                      onClick={async () => {
                        await discard(entry.id);
                        if (online) window.location.reload();
                      }}
                    >
                      Usar servidor
                    </Button>
                  </Stack>
                )}
              </Stack>
            ))}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsOpen(false)}>Cerrar</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
