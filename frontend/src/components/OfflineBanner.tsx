import { useEffect, useState } from 'react';
import { Alert, Button, IconButton, Snackbar, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useOfflineSync } from '../offline/useOfflineSync';

export default function OfflineBanner() {
  const { online, pending, syncing, lastError, syncNow } = useOfflineSync();
  const [dismissed, setDismissed] = useState(false);

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
  );
}
