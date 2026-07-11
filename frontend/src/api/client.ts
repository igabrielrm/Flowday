export type UsuarioDto = {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  tema?: string;
  foto?: string;
};

export type ApiResponse<T> = {
  ok: boolean;
  data: T | null;
  error: string | null;
  meta?: Record<string, unknown>;
};

import type { ActividadDetail, ActividadListItem, CreateActividadPayload, PriorityAlert, ReschedulableItem, UpdateActividadPayload } from '../types/activity';
import type { NotificationItem } from '../notifications/types';
import type { Profile, UpdateProfilePayload } from '../types/profile';
import type { CommunityStats, CommunityUser } from '../types/community';
import type { CreateScheduleBlockPayload, ScheduleAlert, ScheduleBlock } from '../types/schedule';
import type { ChatMessage, Conversation } from '../types/chat';
import type {
  AdminAnnouncement,
  AdminStats,
  AdminTopUser,
  AdminUser,
  AdminWellbeing,
} from '../types/admin';
import type { StressReport, WellbeingStats } from '../types/wellbeing';

export type { NotificationItem };

const API_BASE = import.meta.env.DEV ? '' : '';

async function request<T>(path: string, init?: RequestInit): Promise<ApiResponse<T>> {
  const res = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(init?.headers || {}) },
    ...init,
  });
  if (res.status === 401) {
    return { ok: false, data: null, error: 'No autenticado' };
  }
  if (res.status === 204) {
    return { ok: true, data: null, error: null };
  }
  return res.json();
}

async function legacyJson<T>(path: string, init?: RequestInit): Promise<{ data: T | null; error: string | null }> {
  const res = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(init?.headers || {}) },
    ...init,
  });
  if (res.status === 401) {
    return { data: null, error: 'No autenticado' };
  }
  const json = (await res.json()) as T & { error?: string };
  if (json && typeof json === 'object' && 'error' in json && json.error) {
    return { data: null, error: String(json.error) };
  }
  return { data: json, error: null };
}

export async function downloadAdminReport(
  format: 'excel' | 'pdf' | 'csv',
  desde: string,
  hasta: string,
) {
  const params = new URLSearchParams();
  if (desde) params.set('desde', desde);
  if (hasta) params.set('hasta', hasta);
  const qs = params.toString();
  const res = await fetch(`/admin/reportes/export/${format}${qs ? `?${qs}` : ''}`, {
    credentials: 'include',
  });
  if (!res.ok) {
    throw new Error('No se pudo descargar el reporte');
  }
  const blob = await res.blob();
  const disposition = res.headers.get('Content-Disposition') || '';
  const match = disposition.match(/filename="?([^";]+)"?/i);
  const filename = match?.[1] || `reporte-flowday.${format === 'csv' ? 'zip' : format === 'excel' ? 'xlsx' : 'pdf'}`;
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

export const api = {
  me: () => request<UsuarioDto>('/api/v1/session/me'),
  login: (correo: string, contrasena: string) =>
    request<UsuarioDto>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ correo, contrasena }),
    }),
  adminLogin: (correo: string, contrasena: string) =>
    request<UsuarioDto>('/api/v1/auth/admin-login', {
      method: 'POST',
      body: JSON.stringify({ correo, contrasena }),
    }),
  logout: () => request<void>('/api/v1/auth/logout', { method: 'POST', body: '{}' }),
  oauthProviders: () => request<string[]>('/api/v1/auth/oauth-providers'),
  register: (payload: {
    nombre: string;
    correo: string;
    contrasena: string;
    telefono: string;
    fechaNacimiento?: string;
    genero?: string;
  }) =>
    request<{ mensaje: string }>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  forgotPassword: (correo: string, telefono: string) =>
    request<{ mensaje: string }>('/api/v1/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ correo, telefono }),
    }),
  resetPasswordSession: () => request<{ active: boolean }>('/api/v1/auth/reset-password/session'),
  resetPassword: (contrasenaNueva: string, contrasenaConfirmacion: string) =>
    request<{ mensaje: string }>('/api/v1/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ contrasenaNueva, contrasenaConfirmacion }),
    }),

  activities: {
    list: () => request<ActividadListItem[]>('/api/v1/activities'),
    get: (id: number) => request<ActividadDetail>(`/api/v1/activities/${id}`),
    byDate: (fecha: string) =>
      request<ActividadListItem[]>(`/api/v1/activities/by-date?fecha=${encodeURIComponent(fecha)}`),
    byMonth: (year: number, month: number) =>
      request<ActividadListItem[]>(
        `/api/v1/activities/by-month?year=${year}&month=${month}`,
      ),
    create: (payload: CreateActividadPayload) =>
      request<ActividadDetail>('/api/v1/activities', {
        method: 'POST',
        body: JSON.stringify(payload),
      }),
    update: (id: number, payload: UpdateActividadPayload) =>
      request<ActividadDetail>(`/api/v1/activities/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      }),
    updateStatus: (id: number, estado: string) =>
      request<ActividadListItem>(`/api/v1/activities/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ estado }),
      }),
    remove: (id: number) =>
      request<void>(`/api/v1/activities/${id}`, { method: 'DELETE' }),
    priorityAlerts: () => request<PriorityAlert[]>('/api/v1/activities/priority-alerts'),
    reschedulable: () => request<ReschedulableItem[]>('/api/v1/activities/reschedulable'),
    reschedule: (id: number, fecha: string, hora?: string) =>
      request<ActividadDetail>(`/api/v1/activities/${id}/reschedule`, {
        method: 'POST',
        body: JSON.stringify({ fecha, hora: hora || null }),
      }),
  },

  ia: {
    chat: async (mensaje: string, historial?: { rol: string; contenido: string }[]) => {
      const res = await fetch(`${API_BASE}/api/ia/chat`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          mensaje,
          historial: (historial ?? []).map((m) => ({
            role: m.rol === 'user' ? 'user' : 'assistant',
            text: m.contenido,
          })),
        }),
      });
      if (res.status === 401) return { ok: false, data: null, error: 'Sesión expirada. Vuelve a iniciar sesión.' };
      const json = await res.json();
      if (json.ok === false) return { ok: false, data: null, error: json.mensaje || 'Error IA' };
      return { ok: true, data: json as { respuesta: string; ia?: boolean; fallback?: boolean }, error: null };
    },
    status: async () => {
      const res = await fetch(`${API_BASE}/api/ia/status`, { credentials: 'include' });
      const json = await res.json();
      if (json.ok === false) return { ok: false, data: null, error: json.error || 'No autenticado' };
      return {
        ok: true,
        data: json as { provider: string; groqConfigured: boolean; ready: boolean },
        error: null,
      };
    },
    activityResources: async (id: number) => {
      const res = await fetch(`${API_BASE}/api/ia/recursos-actividad/${id}`, { credentials: 'include' });
      const json = await res.json();
      if (json.ok === false) return { ok: false, data: null, error: json.mensaje || 'Error' };
      return {
        ok: true,
        data: json as { recursos?: { titulo: string; url?: string; descripcion?: string }[] },
        error: null,
      };
    },
  },

  notifications: {
    list: () => request<NotificationItem[]>('/api/v1/notifications'),
    unreadCount: () => request<{ count: number }>('/api/v1/notifications/unread-count'),
    markRead: (id: number) =>
      request<{ ok: boolean; count: number }>(`/api/v1/notifications/${id}/read`, {
        method: 'POST',
        body: '{}',
      }),
    markAllRead: () =>
      request<{ ok: boolean; count: number }>('/api/v1/notifications/read-all', {
        method: 'POST',
        body: '{}',
      }),
    remove: (id: number) =>
      request<{ ok: boolean; count: number }>(`/api/v1/notifications/${id}`, { method: 'DELETE' }),
  },

  profile: {
    get: () => request<Profile>('/api/v1/profile'),
    update: (payload: UpdateProfilePayload) =>
      request<Profile>('/api/v1/profile', {
        method: 'PATCH',
        body: JSON.stringify(payload),
      }),
    changePassword: (contrasenaActual: string, contrasenaNueva: string, contrasenaConfirmacion: string) =>
      request<void>('/api/v1/profile/password', {
        method: 'POST',
        body: JSON.stringify({ contrasenaActual, contrasenaNueva, contrasenaConfirmacion }),
      }),
    changeTheme: (tema: string) =>
      request<Profile>('/api/v1/profile/theme', {
        method: 'PATCH',
        body: JSON.stringify({ tema }),
      }),
    uploadPhoto: async (file: File): Promise<ApiResponse<Profile>> => {
      const form = new FormData();
      form.append('foto', file);
      const res = await fetch(`${API_BASE}/api/v1/profile/photo`, {
        method: 'POST',
        credentials: 'include',
        body: form,
      });
      if (res.status === 401) {
        return { ok: false, data: null, error: 'No autenticado' };
      }
      return res.json();
    },
  },

  community: {
    stats: () => request<CommunityStats>('/api/v1/community/stats'),
    users: (query?: string) => {
      const qs = query ? `?query=${encodeURIComponent(query)}` : '';
      return request<CommunityUser[]>(`/api/v1/community/users${qs}`);
    },
    suggestions: (limit = 4) =>
      request<CommunityUser[]>(`/api/v1/community/suggestions?limit=${limit}`),
    connections: () => request<UsuarioDto[]>('/api/v1/community/connections'),
    connect: (userId: number) =>
      request<{ mensaje: string }>('/api/v1/community/connections', {
        method: 'POST',
        body: JSON.stringify({ userId }),
      }),
    accept: (conexionId: number) =>
      request<{ mensaje: string }>(`/api/v1/community/connections/${conexionId}/accept`, {
        method: 'POST',
        body: '{}',
      }),
    reject: (conexionId: number) =>
      request<{ mensaje: string }>(`/api/v1/community/connections/${conexionId}/reject`, {
        method: 'POST',
        body: '{}',
      }),
    removeConnection: (conexionId: number) =>
      request<void>(`/api/v1/community/connections/${conexionId}`, { method: 'DELETE' }),
  },

  chat: {
    conversations: () => request<Conversation[]>('/api/v1/chat/conversations'),
    messages: (userId: number) => request<ChatMessage[]>(`/api/v1/chat/messages/${userId}`),
    send: (destinatarioId: number, contenido: string) =>
      request<ChatMessage>('/api/v1/chat/messages', {
        method: 'POST',
        body: JSON.stringify({ destinatarioId, contenido }),
      }),
    markRead: (userId: number) =>
      request<{ updated: number }>(`/api/v1/chat/messages/${userId}/read`, {
        method: 'POST',
        body: '{}',
      }),
    unreadCount: () => request<{ count: number }>('/api/v1/chat/unread-count'),
    deleteConversation: (userId: number) =>
      request<{ deleted: number }>(`/api/v1/chat/conversations/${userId}`, { method: 'DELETE' }),
  },

  schedule: {
    list: () => request<ScheduleBlock[]>('/api/v1/schedule/blocks'),
    create: (payload: CreateScheduleBlockPayload) =>
      request<ScheduleBlock>('/api/v1/schedule/blocks', {
        method: 'POST',
        body: JSON.stringify(payload),
      }),
    update: (id: number, payload: CreateScheduleBlockPayload) =>
      request<ScheduleBlock>(`/api/v1/schedule/blocks/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      }),
    remove: (id: number) =>
      request<void>(`/api/v1/schedule/blocks/${id}`, { method: 'DELETE' }),
    alert: (minutesBefore = 15) =>
      request<ScheduleAlert | null>(`/api/v1/schedule/alert?minutesBefore=${minutesBefore}`),
  },

  admin: {
    stats: () => request<AdminStats>('/api/v1/admin/stats'),
    wellbeing: () => request<AdminWellbeing>('/api/v1/admin/wellbeing'),
    users: () => request<AdminUser[]>('/api/v1/admin/users'),
    topUsers: (limit = 8) => request<AdminTopUser[]>(`/api/v1/admin/users/top?limit=${limit}`),
    toggleRole: (id: number) =>
      request<{ id: number; rol: string }>(`/api/v1/admin/users/${id}/role`, {
        method: 'PATCH',
        body: '{}',
      }),
    deleteUser: (id: number) =>
      request<{ ok: boolean }>(`/api/v1/admin/users/${id}`, { method: 'DELETE' }),
    announcements: () =>
      request<{ activos: AdminAnnouncement[]; archivados: AdminAnnouncement[] }>(
        '/api/v1/admin/announcements',
      ),
    createAnnouncement: (payload: { titulo: string; descripcion: string; fechaLimite: string }) =>
      request<AdminAnnouncement>('/api/v1/admin/announcements', {
        method: 'POST',
        body: JSON.stringify(payload),
      }),
    archiveAnnouncement: (id: number) =>
      request<AdminAnnouncement>(`/api/v1/admin/announcements/${id}/archive`, {
        method: 'POST',
        body: '{}',
      }),
    restoreAnnouncement: (id: number) =>
      request<AdminAnnouncement>(`/api/v1/admin/announcements/${id}/restore`, {
        method: 'POST',
        body: '{}',
      }),
    deleteAnnouncement: (id: number) =>
      request<{ ok: boolean }>(`/api/v1/admin/announcements/${id}`, { method: 'DELETE' }),
    analytics: (path: string, query = '') =>
      request<Record<string, unknown>>(`/api/v1/admin/analytics/${path}${query}`),
  },

  bienestar: {
    stats: () => legacyJson<WellbeingStats>('/api/bienestar/estadisticas'),
    stress: (fecha?: string) => {
      const qs = fecha ? `?fecha=${encodeURIComponent(fecha)}` : '';
      return legacyJson<StressReport>(`/api/bienestar/estres${qs}`);
    },
    savePomodoro: (duracion: number) =>
      legacyJson<{ mensaje: string }>('/api/bienestar/pomodoro', {
        method: 'POST',
        body: JSON.stringify({ duracion }),
      }),
    savePause: (tipo: string, duracion: number) =>
      legacyJson<{ mensaje: string }>('/api/bienestar/pausa', {
        method: 'POST',
        body: JSON.stringify({ tipo, duracion }),
      }),
  },
};
