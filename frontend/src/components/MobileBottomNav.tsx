import { useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { BottomNavigation, BottomNavigationAction, Paper } from '@mui/material';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';
import TaskAltOutlinedIcon from '@mui/icons-material/TaskAltOutlined';
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import ScheduleOutlinedIcon from '@mui/icons-material/ScheduleOutlined';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';

const ITEMS = [
  { value: '/', label: 'Inicio', icon: <HomeOutlinedIcon /> },
  { value: '/activities', label: 'Tareas', icon: <TaskAltOutlinedIcon /> },
  { value: '/calendar', label: 'Cal.', icon: <CalendarMonthOutlinedIcon /> },
  { value: '/schedule', label: 'Horario', icon: <ScheduleOutlinedIcon /> },
  { value: '/chat', label: 'Chat', icon: <ChatOutlinedIcon /> },
  { value: '/profile', label: 'Perfil', icon: <PersonOutlineOutlinedIcon /> },
];

export default function MobileBottomNav() {
  const location = useLocation();
  const navigate = useNavigate();

  const current = useMemo(() => {
    const match = ITEMS.find(
      (item) => item.value === location.pathname || (item.value !== '/' && location.pathname.startsWith(item.value)),
    );
    return match?.value ?? '/';
  }, [location.pathname]);

  return (
    <Paper
      elevation={0}
      sx={{
        display: { xs: 'block', md: 'none' },
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: (t) => t.zIndex.appBar,
        borderTop: 1,
        borderColor: 'divider',
        pb: 'env(safe-area-inset-bottom)',
      }}
    >
      <BottomNavigation
        showLabels
        value={current}
        onChange={(_, value) => navigate(value)}
        sx={{ height: 56 }}
      >
        {ITEMS.map((item) => (
          <BottomNavigationAction key={item.value} value={item.value} label={item.label} icon={item.icon} />
        ))}
      </BottomNavigation>
    </Paper>
  );
}
