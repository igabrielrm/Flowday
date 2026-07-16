import { useEffect } from 'react';
import { App as CapacitorApp } from '@capacitor/app';
import { LocalNotifications } from '@capacitor/local-notifications';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { OFFLINE_QUEUE_EVENT } from '../events';
import { isNative } from '../platform';
import { ensureLocalNotificationPermission, syncLocalReminders } from './localReminders';

/**
 * Schedules OS local notifications from cached/online activities + schedule.
 * Once scheduled on-device they fire offline; resync when online or queue changes.
 */
export default function LocalRemindersBridge() {
  const { user } = useAuth();

  useEffect(() => {
    if (!isNative || !user) return;

    let cancelled = false;

    async function refresh() {
      await ensureLocalNotificationPermission();
      if (cancelled) return;
      const [activities, schedule] = await Promise.all([
        api.activities.list(),
        api.schedule.list(),
      ]);
      if (cancelled) return;
      await syncLocalReminders({
        activities: activities.ok && activities.data ? activities.data : [],
        schedule: schedule.ok && schedule.data ? schedule.data : [],
      });
    }

    refresh();

    const onOnline = () => refresh();
    const onQueue = () => refresh();
    window.addEventListener('online', onOnline);
    window.addEventListener(OFFLINE_QUEUE_EVENT, onQueue);

    const resume = CapacitorApp.addListener('appStateChange', ({ isActive }) => {
      if (isActive) refresh();
    });

    const tap = LocalNotifications.addListener('localNotificationActionPerformed', (event) => {
      const route = event.notification.extra?.route;
      if (typeof route === 'string' && route.startsWith('/')) {
        window.location.hash = route;
      }
    });

    return () => {
      cancelled = true;
      window.removeEventListener('online', onOnline);
      window.removeEventListener(OFFLINE_QUEUE_EVENT, onQueue);
      resume.then((l) => l.remove());
      tap.then((l) => l.remove());
    };
  }, [user]);

  return null;
}
