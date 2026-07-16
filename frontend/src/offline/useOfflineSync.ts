import { useCallback, useEffect, useState } from 'react';
import { OFFLINE_QUEUE_EVENT } from '../events';
import {
  pendingCount,
  readQueue,
  removeFromQueue,
  updateMutation,
  type OfflineMutation,
} from './queue';
import { notifyOfflineQueueChanged } from '../events';
import { flushOfflineQueue } from './sync';
import { useOnlineStatus } from './useOnlineStatus';

export function useOfflineSync() {
  const online = useOnlineStatus();
  const [pending, setPending] = useState(0);
  const [entries, setEntries] = useState<OfflineMutation[]>([]);
  const [syncing, setSyncing] = useState(false);
  const [lastError, setLastError] = useState<string | null>(null);

  const refreshPending = useCallback(() => {
    pendingCount().then(setPending);
    readQueue(true).then(setEntries);
  }, []);

  const syncNow = useCallback(async () => {
    if (!navigator.onLine || (await pendingCount()) === 0) return;
    setSyncing(true);
    setLastError(null);
    const result = await flushOfflineQueue();
    refreshPending();
    setSyncing(false);
    if (result.errors.length > 0) {
      setLastError(result.errors[0]);
    }
  }, [refreshPending]);

  useEffect(() => {
    refreshPending();
    const onQueue = () => refreshPending();
    window.addEventListener(OFFLINE_QUEUE_EVENT, onQueue);
    return () => window.removeEventListener(OFFLINE_QUEUE_EVENT, onQueue);
  }, [refreshPending]);

  useEffect(() => {
    if (online) pendingCount().then((count) => count > 0 && syncNow());
  }, [online, syncNow]);

  const retry = useCallback(async (id: string) => {
    await updateMutation(id, { status: 'PENDING', lastError: undefined, serverData: undefined });
    notifyOfflineQueueChanged();
    if (navigator.onLine) await syncNow();
  }, [syncNow]);

  const discard = useCallback(async (id: string) => {
    await removeFromQueue(id);
    notifyOfflineQueueChanged();
  }, []);

  return { online, pending, entries, syncing, lastError, syncNow, retry, discard, refreshPending };
}
