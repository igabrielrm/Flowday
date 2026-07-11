import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Badge,
  Box,
  CircularProgress,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Menu,
  Typography,
  useTheme,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import CloseIcon from '@mui/icons-material/Close';
import { useNotifications } from '../notifications/NotificationsContext';
import { api } from '../api/client';
import { notificationIcon, resolveNotificationTarget } from '../notifications/types';
import { glassSurface } from '../theme/glass';

export default function NotificationsBell() {
  const theme = useTheme();
  const navigate = useNavigate();
  const anchorRef = useRef<HTMLButtonElement>(null);
  const { unread, items, loading, open, setOpen, loadItems, markRead, markAllRead } =
    useNotifications();

  useEffect(() => {
    if (open) loadItems();
  }, [loadItems, open]);

  async function openNotification(item: (typeof items)[0]) {
    await markRead(item.id);
    setOpen(false);
    const target = resolveNotificationTarget(item.enlace, item.tipo);
    if (target) {
      navigate({ pathname: target.pathname, search: target.search });
    }
  }

  return (
    <>
      <IconButton
        ref={anchorRef}
        aria-label="Notificaciones"
        color="inherit"
        onClick={() => setOpen(!open)}
      >
        <Badge badgeContent={unread > 0 ? (unread > 99 ? '99+' : unread) : undefined} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>

      <Menu
        anchorEl={anchorRef.current}
        open={open}
        onClose={() => setOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        slotProps={{
          paper: {
            sx: {
              width: 'min(340px, calc(100vw - 32px))',
              maxHeight: 'min(420px, calc(100dvh - 120px))',
              mt: 1,
              overflow: 'hidden',
              ...glassSurface(theme, { strong: true }),
            },
          },
        }}
      >
        <Box px={2.5} py={2} display="flex" justifyContent="space-between" alignItems="center">
          <Typography fontWeight={700}>Notificaciones</Typography>
          {unread > 0 && (
            <Typography
              component="button"
              variant="caption"
              color="primary"
              onClick={() => markAllRead()}
              sx={{ border: 'none', bgcolor: 'transparent', cursor: 'pointer', fontWeight: 600 }}
            >
              Marcar todas
            </Typography>
          )}
        </Box>
        <Divider />
        {loading ? (
          <Box py={3} textAlign="center">
            <CircularProgress size={24} />
          </Box>
        ) : items.length === 0 ? (
          <Box p={2.5}>
            <Typography variant="body2" color="text.secondary" textAlign="center">
              No tienes notificaciones
            </Typography>
          </Box>
        ) : (
          <List dense disablePadding sx={{ maxHeight: 320, overflow: 'auto' }}>
            {items.map((n) => (
              <ListItem
                key={n.id}
                disablePadding
                secondaryAction={
                  <IconButton
                    edge="end"
                    size="small"
                    aria-label="Eliminar"
                    onClick={async (e) => {
                      e.stopPropagation();
                      await api.notifications.remove(n.id);
                      loadItems();
                    }}
                  >
                    <CloseIcon fontSize="small" />
                  </IconButton>
                }
                sx={{ opacity: n.leida ? 0.75 : 1 }}
              >
                <ListItemButton onClick={() => openNotification(n)} sx={{ py: 1.75, pr: 6, gap: 1 }}>
                  <Box fontSize={18} sx={{ mr: 0.5 }}>
                    {notificationIcon(n.tipo)}
                  </Box>
                  <ListItemText
                    primary={<Typography fontWeight={n.leida ? 500 : 700} sx={{ mb: 0.5 }}>{n.titulo}</Typography>}
                    secondary={
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25, lineHeight: 1.45 }}>
                        {n.mensaje}
                      </Typography>
                    }
                    slotProps={{ secondary: { component: 'div' } }}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        )}
      </Menu>
    </>
  );
}
