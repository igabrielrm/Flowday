export type NotificationItem = {
  id: number;
  tipo: string;
  titulo: string;
  mensaje: string;
  leida: boolean;
  fecha?: string | null;
  enlace?: string | null;
};

export type NotificationPushPayload = NotificationItem & {
  noLeidas?: number;
};

export type NotificationNavTarget = {
  pathname: string;
  search?: string;
};

const ICONS: Record<string, string> = {
  CONEXION: '👥',
  SOLICITUD_AMISTAD: '🤝',
  MENSAJE: '💬',
  PRIORIDAD: '🔴',
  ACTIVIDAD: '📋',
  SISTEMA: 'ℹ️',
  ANUNCIO: '📢',
  REAGENDAMIENTO_AUTO: '🔄',
};

export function notificationIcon(tipo: string) {
  return ICONS[tipo] || '🔔';
}

function stripAppPrefix(path: string) {
  return path.startsWith('/app') ? path.slice(4) || '/' : path;
}

function parseChatLink(enlace: string): NotificationNavTarget | null {
  const match = enlace.match(/\/app\/chat\?user=(\d+)/);
  if (match) return { pathname: '/chat', search: `?user=${match[1]}` };
  const rel = enlace.match(/^\/chat\?user=(\d+)/);
  if (rel) return { pathname: '/chat', search: `?user=${rel[1]}` };
  return null;
}

export function resolveNotificationTarget(
  enlace?: string | null,
  tipo?: string,
): NotificationNavTarget | null {
  if (!enlace) {
    if (tipo === 'SOLICITUD_AMISTAD' || tipo === 'CONEXION') return { pathname: '/community' };
    if (tipo === 'MENSAJE') return { pathname: '/chat' };
    if (tipo === 'ACTIVIDAD') return { pathname: '/activities' };
    return null;
  }

  if (tipo === 'ANUNCIO' || enlace.startsWith('anuncio:')) {
    return { pathname: '/community' };
  }

  const chat = parseChatLink(enlace);
  if (chat) return chat;

  if (enlace === '/comunidad') return { pathname: '/community' };
  if (enlace === '/actividades' || enlace === '/app/activities') return { pathname: '/activities' };
  if (enlace.startsWith('/actividades/editar/')) {
    const id = enlace.split('/').pop();
    return id ? { pathname: `/activities/${id}/edit` } : { pathname: '/activities' };
  }
  if (enlace.startsWith('/horario') || enlace === '/app/schedule') return { pathname: '/schedule' };
  if (enlace.startsWith('/app/')) return { pathname: stripAppPrefix(enlace) };
  if (enlace.startsWith('/')) return { pathname: enlace };

  return null;
}

/** @deprecated use resolveNotificationTarget */
export function resolveNotificationLink(enlace?: string | null, tipo?: string) {
  const target = resolveNotificationTarget(enlace, tipo);
  if (!target) return null;
  return `/app${target.pathname}${target.search ?? ''}`;
}
