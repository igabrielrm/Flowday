const CACHE = 'flowday-spa-v3';
const PRECACHE = ['/app/', '/app/index.html', '/manifest.json'];

function isSpaNavigation(url) {
  if (url.origin !== self.location.origin) return false;
  const path = url.pathname;
  if (path.startsWith('/api/') || path.startsWith('/ws') || path.startsWith('/admin/reportes')) {
    return false;
  }
  return path === '/app' || path === '/app/' || path.startsWith('/app/');
}

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(CACHE)
      .then((cache) => cache.addAll(PRECACHE))
      .then(() => self.skipWaiting())
      .catch(() => self.skipWaiting()),
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()),
  );
});

async function serveSpaShell() {
  try {
    const response = await fetch('/app/index.html');
    if (response.ok) return response;
  } catch {
    /* cache fallback */
  }
  const cached = await caches.match('/app/index.html');
  if (cached) return cached;
  return new Response('Offline', { status: 503, headers: { 'Content-Type': 'text/plain' } });
}

function shouldBypass(url) {
  if (url.origin !== self.location.origin) return true;
  if (url.pathname.startsWith('/api/') || url.pathname.startsWith('/ws')) return true;
  if (url.pathname.startsWith('/@') || url.pathname.includes('/@vite/')) return true;
  if (url.pathname.includes('/node_modules/')) return true;
  if (url.search.includes('import')) return true;
  return false;
}

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  const url = new URL(event.request.url);
  if (shouldBypass(url)) return;

  if (event.request.mode === 'navigate' && isSpaNavigation(url)) {
    event.respondWith(serveSpaShell());
    return;
  }

  event.respondWith(
    fetch(event.request)
      .then((response) => response)
      .catch(async () => {
        const cached = await caches.match(event.request);
        return cached || new Response('Offline', { status: 503, headers: { 'Content-Type': 'text/plain' } });
      }),
  );
});
