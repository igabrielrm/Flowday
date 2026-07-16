import type { UsuarioDto } from '../api/client';
import type { ChatMessage, Conversation } from '../types/chat';
import type { CommunityUser } from '../types/community';
import type { NotificationItem } from '../notifications/types';
import type { Profile, UpdateProfilePayload } from '../types/profile';
import type { WellbeingStats } from '../types/wellbeing';
import {
  cacheApiGet,
  readApiGet,
  readSessionUser,
  updateApiGet,
  updateMatchingApiCaches,
} from './cache';

export function applyProfileUpdate(payload: Partial<UpdateProfilePayload> & { tema?: string }) {
  const current = readApiGet<Profile>('/api/v1/profile');
  if (!current) return null;
  const next = { ...current, ...payload };
  cacheApiGet('/api/v1/profile', next);
  return next;
}

export function applyWellbeingRecord(kind: 'POMODORO' | 'PAUSA', value: number, description?: string) {
  updateApiGet<WellbeingStats>('/api/bienestar/estadisticas', (current) => {
    const base = current ?? {
      minutosPomodoro: 0,
      sesionesPomodoro: 0,
      totalPomodoros: 0,
      totalPausas: 0,
      ultimasSesiones: [],
    };
    return {
      ...base,
      minutosPomodoro: base.minutosPomodoro + (kind === 'POMODORO' ? value : 0),
      sesionesPomodoro: base.sesionesPomodoro + (kind === 'POMODORO' ? 1 : 0),
      totalPomodoros: base.totalPomodoros + (kind === 'POMODORO' ? 1 : 0),
      totalPausas: base.totalPausas + (kind === 'PAUSA' ? 1 : 0),
      ultimasSesiones: [
        {
          id: -Date.now(),
          tipo: kind,
          valor: value,
          descripcion,
          fecha: new Date().toISOString(),
        },
        ...(base.ultimasSesiones ?? []),
      ].slice(0, 20),
    };
  });
}

function patchCommunityUser(
  matcher: (item: CommunityUser) => boolean,
  patch: Partial<CommunityUser>,
) {
  updateMatchingApiCaches<CommunityUser[]>(
    (path) => path.startsWith('/api/v1/community/users') || path.startsWith('/api/v1/community/suggestions'),
    (items) => items.map((item) => (matcher(item) ? { ...item, ...patch } : item)),
  );
}

export function applyCommunityConnect(userId: number) {
  patchCommunityUser((item) => item.user.id === userId, {
    conectado: false,
    estadoRelacion: 'SOLICITUD_ENVIADA',
  });
}

export function applyCommunityDecision(
  connectionId: number,
  state: 'CONECTADO' | 'NINGUNA',
) {
  patchCommunityUser((item) => item.conexionId === connectionId, {
    conectado: state === 'CONECTADO',
    estadoRelacion: state,
    ...(state === 'NINGUNA' ? { conexionId: null } : {}),
  });
}

export function applyChatSend(destinatarioId: number, contenido: string, tempId: number) {
  const user = readSessionUser();
  const message: ChatMessage = {
    id: tempId,
    remitenteId: user?.id ?? 0,
    destinatarioId,
    contenido,
    fecha: new Date().toISOString(),
    leida: false,
    propio: true,
  };
  updateApiGet<ChatMessage[]>(`/api/v1/chat/messages/${destinatarioId}`, (items) => [
    ...(items ?? []),
    message,
  ]);
  updateApiGet<Conversation[]>('/api/v1/chat/conversations', (items) =>
    (items ?? []).map((conversation) =>
      conversation.user.id === destinatarioId
        ? { ...conversation, ultimoMensaje: contenido, ultimaFecha: message.fecha }
        : conversation,
    ),
  );
  return message;
}

export function replaceChatTempId(tempId: number, serverMessage: ChatMessage) {
  const otherId = serverMessage.propio ? serverMessage.destinatarioId : serverMessage.remitenteId;
  updateApiGet<ChatMessage[]>(`/api/v1/chat/messages/${otherId}`, (items) =>
    (items ?? []).map((message) => (message.id === tempId ? serverMessage : message)),
  );
}

export function applyChatRead(userId: number) {
  updateApiGet<ChatMessage[]>(`/api/v1/chat/messages/${userId}`, (items) =>
    (items ?? []).map((message) => ({ ...message, leida: true })),
  );
  updateApiGet<Conversation[]>('/api/v1/chat/conversations', (items) =>
    (items ?? []).map((conversation) =>
      conversation.user.id === userId ? { ...conversation, noLeidos: 0 } : conversation,
    ),
  );
}

export function applyChatDelete(userId: number) {
  cacheApiGet(`/api/v1/chat/messages/${userId}`, []);
  updateApiGet<Conversation[]>('/api/v1/chat/conversations', (items) =>
    (items ?? []).filter((conversation) => conversation.user.id !== userId),
  );
}

export function applyNotificationRead(id?: number) {
  updateApiGet<NotificationItem[]>('/api/v1/notifications', (items) =>
    (items ?? []).map((item) => (id == null || item.id === id ? { ...item, leida: true } : item)),
  );
  const unread = readApiGet<NotificationItem[]>('/api/v1/notifications')?.filter((item) => !item.leida).length ?? 0;
  cacheApiGet('/api/v1/notifications/unread-count', { count: unread });
}

export function applyNotificationDelete(id: number) {
  updateApiGet<NotificationItem[]>('/api/v1/notifications', (items) =>
    (items ?? []).filter((item) => item.id !== id),
  );
  const unread = readApiGet<NotificationItem[]>('/api/v1/notifications')?.filter((item) => !item.leida).length ?? 0;
  cacheApiGet('/api/v1/notifications/unread-count', { count: unread });
}

export function applyConnectionsList(user: UsuarioDto, connected: boolean) {
  updateApiGet<UsuarioDto[]>('/api/v1/community/connections', (items) => {
    const base = (items ?? []).filter((item) => item.id !== user.id);
    return connected ? [...base, user] : base;
  });
}
