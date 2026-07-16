import type { UsuarioDto } from '../api/client';
import { dbDelete, dbGet, dbGetAll, dbPut, requestPersistentStorage } from './db';

const API_PREFIX = 'flowday-offline-api:';
const USER_KEY = 'flowday-offline-user';
const MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000;

type CachedEntry<T> = {
  data: T;
  savedAt: number;
};

type StoredCacheEntry = {
  id: string;
  userId: string;
  key: string;
  value: string;
};

function activeUserId() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    const parsed = raw ? (JSON.parse(raw) as CachedEntry<UsuarioDto>) : null;
    return String(parsed?.data?.id ?? 'anonymous');
  } catch {
    return 'anonymous';
  }
}

function storedId(key: string, userId = activeUserId()) {
  return key === USER_KEY ? `session|${USER_KEY}` : `${userId}|${key}`;
}

function readEntry<T>(key: string): CachedEntry<T> | null {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as CachedEntry<T>;
    if (!parsed?.data || !parsed.savedAt) return null;
    if (Date.now() - parsed.savedAt > MAX_AGE_MS) {
      localStorage.removeItem(key);
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

function writeEntry<T>(key: string, data: T) {
  try {
    const entry: CachedEntry<T> = { data, savedAt: Date.now() };
    const value = JSON.stringify(entry);
    localStorage.setItem(key, value);
    const userId = key === USER_KEY ? String((data as UsuarioDto).id) : activeUserId();
    void dbPut('cache', { id: storedId(key, userId), userId, key, value } satisfies StoredCacheEntry);
  } catch {
    /* quota */
  }
}

export function isBrowserOffline(): boolean {
  return typeof navigator !== 'undefined' && !navigator.onLine;
}

export function cacheApiGet<T>(path: string, data: T) {
  writeEntry(`${API_PREFIX}${path}`, data);
}

export function readApiGet<T>(path: string): T | null {
  return readEntry<T>(`${API_PREFIX}${path}`)?.data ?? null;
}

export function cacheSessionUser(user: UsuarioDto) {
  const previous = readSessionUser();
  if (previous && previous.id !== user.id) clearApiMirror();
  writeEntry(USER_KEY, user);
}

export function readSessionUser(): UsuarioDto | null {
  return readEntry<UsuarioDto>(USER_KEY)?.data ?? null;
}

export function clearSessionUser() {
  localStorage.removeItem(USER_KEY);
  void dbDelete('cache', `session|${USER_KEY}`);
}

export function isTempEntityId(id: number) {
  return id < 0;
}

export function updateApiGet<T>(path: string, updater: (current: T | null) => T | null) {
  const current = readApiGet<T>(path);
  const next = updater(current);
  if (next != null) {
    cacheApiGet(path, next);
  } else {
    localStorage.removeItem(`${API_PREFIX}${path}`);
    void dbDelete('cache', storedId(`${API_PREFIX}${path}`));
  }
}

export function removeApiGet(path: string) {
  localStorage.removeItem(`${API_PREFIX}${path}`);
  void dbDelete('cache', storedId(`${API_PREFIX}${path}`));
}

export function updateMatchingApiCaches<T>(
  predicate: (path: string) => boolean,
  updater: (current: T, path: string) => T,
) {
  for (let index = 0; index < localStorage.length; index += 1) {
    const key = localStorage.key(index);
    if (!key?.startsWith(API_PREFIX)) continue;
    const path = key.slice(API_PREFIX.length);
    if (!predicate(path)) continue;
    const current = readApiGet<T>(path);
    if (current != null) cacheApiGet(path, updater(current, path));
  }
}

function clearApiMirror() {
  for (let index = localStorage.length - 1; index >= 0; index -= 1) {
    const key = localStorage.key(index);
    if (key?.startsWith(API_PREFIX)) localStorage.removeItem(key);
  }
}

export async function hydrateOfflineState() {
  await requestPersistentStorage();
  const session = await dbGet<StoredCacheEntry>('cache', `session|${USER_KEY}`);
  if (session?.value && !localStorage.getItem(USER_KEY)) {
    localStorage.setItem(USER_KEY, session.value);
  }
  const userId = activeUserId();
  clearApiMirror();
  const cached = await dbGetAll<StoredCacheEntry>('cache');
  for (const entry of cached) {
    if (entry.userId === userId && entry.key.startsWith(API_PREFIX)) {
      localStorage.setItem(entry.key, entry.value);
    }
  }
}

export async function clearOfflineMirrorForUser(userId: string) {
  clearApiMirror();
  const cached = await dbGetAll<StoredCacheEntry>('cache');
  await Promise.all(
    cached
      .filter((entry) => entry.userId === userId && entry.key.startsWith(API_PREFIX))
      .map((entry) => dbDelete('cache', entry.id)),
  );
}
