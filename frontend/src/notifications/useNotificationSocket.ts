import { useCallback, useEffect, useRef, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { api } from '../api/client';
import type { NotificationItem, NotificationPushPayload } from './types';
import { websocketUrl } from '../platform';
import { nativeAuthHeaders } from '../auth/nativeAuth';

const POLL_MS = 60_000;

type Options = {
  enabled: boolean;
  onPush?: (payload: NotificationPushPayload) => void;
};

export function useNotificationSocket({ enabled, onPush }: Options) {
  const [unread, setUnread] = useState(0);
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const clientRef = useRef<Client | null>(null);
  const onPushRef = useRef(onPush);
  onPushRef.current = onPush;

  const refreshCount = useCallback(async () => {
    const res = await api.notifications.unreadCount();
    if (res.ok && res.data) setUnread(res.data.count);
  }, []);

  const loadItems = useCallback(async () => {
    setLoading(true);
    const res = await api.notifications.list();
    if (res.ok && res.data) setItems(res.data);
    setLoading(false);
  }, []);

  const refreshAll = useCallback(async () => {
    await Promise.all([refreshCount(), loadItems()]);
  }, [loadItems, refreshCount]);

  const handlePush = useCallback(
    (payload: NotificationPushPayload) => {
      if (payload.noLeidas != null) setUnread(payload.noLeidas);
      if (payload.id) {
        setItems((prev) => {
          const next = [payload, ...prev.filter((n) => n.id !== payload.id)];
          return next.slice(0, 10);
        });
      }
      onPushRef.current?.(payload);
    },
    [],
  );

  const startPolling = useCallback(() => {
    if (pollRef.current) return;
    pollRef.current = setInterval(() => {
      refreshCount();
    }, POLL_MS);
  }, [refreshCount]);

  const stopPolling = useCallback(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }, []);

  const connectStomp = useCallback(() => {
    if (clientRef.current?.active) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(websocketUrl('/ws')) as unknown as WebSocket,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {},
      beforeConnect: async () => {
        client.connectHeaders = await nativeAuthHeaders();
      },
      onConnect: () => {
        stopPolling();
        client.subscribe('/user/queue/notifications', (message: IMessage) => {
          try {
            handlePush(JSON.parse(message.body) as NotificationPushPayload);
          } catch {
            /* ignore malformed payload */
          }
        });
      },
      onStompError: () => startPolling(),
      onWebSocketClose: () => startPolling(),
      onDisconnect: () => startPolling(),
    });

    clientRef.current = client;
    client.activate();
  }, [handlePush, startPolling, stopPolling]);

  useEffect(() => {
    if (!enabled) return undefined;

    refreshCount();
    connectStomp();

    return () => {
      stopPolling();
      clientRef.current?.deactivate();
      clientRef.current = null;
    };
  }, [connectStomp, enabled, refreshCount, stopPolling]);

  const markRead = useCallback(
    async (id: number) => {
      const res = await api.notifications.markRead(id);
      if (res.ok && res.data && typeof res.data.count === 'number') {
        setUnread(res.data.count);
      } else {
        await refreshCount();
      }
      setItems((prev) => prev.map((n) => (n.id === id ? { ...n, leida: true } : n)));
    },
    [refreshCount],
  );

  const markAllRead = useCallback(async () => {
    const res = await api.notifications.markAllRead();
    if (res.ok) {
      setUnread(0);
      setItems((prev) => prev.map((n) => ({ ...n, leida: true })));
    }
  }, []);

  return {
    unread,
    items,
    loading,
    loadItems,
    refreshCount,
    refreshAll,
    markRead,
    markAllRead,
  };
}
