import { NavLink, Outlet } from 'react-router-dom';
import {
  AppBar,
  Avatar,
  Box,
  Button,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import LogoutOutlinedIcon from '@mui/icons-material/LogoutOutlined';
import { useAuth } from '../auth/AuthContext';
import { profileInitials } from '../types/profile';

const DRAWER_WIDTH = 240;

export default function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', width: '100%' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        <Box sx={{ px: 2, py: 2 }}>
          <Typography variant="h6" color="primary.main" fontWeight={800}>
            Flowday
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Panel administrativo
          </Typography>
        </Box>
        <Divider />
        <List sx={{ px: 1 }}>
          <ListItemButton component={NavLink} to="/admin" end sx={{ borderRadius: 2 }}>
            <ListItemText primary="Panel general" />
          </ListItemButton>
          <ListItemButton component={NavLink} to="/" end sx={{ borderRadius: 2 }}>
            <ListItemText primary="Vista de usuario" />
          </ListItemButton>
        </List>
        <Box sx={{ mt: 'auto', p: 2 }}>
          <Box sx={{ display: 'flex', gap: 1.5, mb: 2, alignItems: 'center' }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 36, height: 36 }}>
              {profileInitials(user?.nombre || 'A')}
            </Avatar>
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body2" fontWeight={600} noWrap>
                {user?.nombre}
              </Typography>
              <Typography variant="caption" color="text.secondary" noWrap display="block">
                {user?.correo}
              </Typography>
            </Box>
          </Box>
          <Button
            fullWidth
            variant="outlined"
            startIcon={<LogoutOutlinedIcon />}
            onClick={() => logout()}
          >
            Salir
          </Button>
        </Box>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, minWidth: 0 }}>
        <AppBar position="sticky" color="transparent" elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Toolbar>
            <Typography variant="subtitle1" color="text.secondary">
              Panel administrativo
            </Typography>
          </Toolbar>
        </AppBar>
        <Box sx={{ maxWidth: 1200, mx: 'auto', px: { xs: 2, md: 4 }, py: 3 }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
