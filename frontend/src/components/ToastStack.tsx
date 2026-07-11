import { Snackbar, Alert, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useNotifications } from '../notifications/NotificationsContext';

export default function ToastStack() {
  const { toasts, dismissToast, openToast } = useNotifications();
  const toast = toasts[0];

  if (!toast) return null;

  return (
    <Snackbar
      open
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      sx={{ mb: { xs: 8, md: 2 } }}
    >
      <Alert
        severity="info"
        action={
          <>
            <IconButton size="small" color="inherit" onClick={() => openToast(toast)}>
              Abrir
            </IconButton>
            <IconButton size="small" color="inherit" onClick={() => dismissToast(toast.id)}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </>
        }
        sx={{ width: '100%', cursor: 'pointer' }}
        onClick={() => openToast(toast)}
      >
        {toast.titulo}
      </Alert>
    </Snackbar>
  );
}
