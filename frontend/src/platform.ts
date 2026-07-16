import { Capacitor } from '@capacitor/core';

export const isNative = Capacitor.isNativePlatform() || import.meta.env.VITE_NATIVE === 'true';

const configuredBase = String(import.meta.env.VITE_API_BASE || '').replace(/\/+$/, '');

export const API_BASE = isNative ? configuredBase : '';

export function apiUrl(path: string) {
  if (/^https?:\/\//i.test(path)) return path;
  const normalized = path.startsWith('/') ? path : `/${path}`;
  return `${API_BASE}${normalized}`;
}

export function assetUrl(path?: string | null) {
  if (!path) return path ?? '';
  if (/^(https?:|data:|blob:)/i.test(path)) return path;
  return apiUrl(path);
}

export function websocketUrl(path = '/ws') {
  return apiUrl(path);
}
