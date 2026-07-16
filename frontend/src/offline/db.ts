const DB_NAME = 'flowday-offline';
const DB_VERSION = 1;

export type OfflineStore = 'outbox' | 'cache' | 'meta';

let dbPromise: Promise<IDBDatabase> | null = null;

export function openOfflineDb() {
  if (!dbPromise) {
    dbPromise = new Promise<IDBDatabase>((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains('outbox')) {
          const outbox = db.createObjectStore('outbox', { keyPath: 'id' });
          outbox.createIndex('userId', 'userId', { unique: false });
          outbox.createIndex('status', 'status', { unique: false });
        }
        if (!db.objectStoreNames.contains('cache')) {
          const cache = db.createObjectStore('cache', { keyPath: 'id' });
          cache.createIndex('userId', 'userId', { unique: false });
        }
        if (!db.objectStoreNames.contains('meta')) {
          db.createObjectStore('meta', { keyPath: 'id' });
        }
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }
  return dbPromise;
}

function complete<T>(
  storeName: OfflineStore,
  mode: IDBTransactionMode,
  action: (store: IDBObjectStore) => IDBRequest<T>,
) {
  return openOfflineDb().then(
    (db) =>
      new Promise<T>((resolve, reject) => {
        const transaction = db.transaction(storeName, mode);
        const request = action(transaction.objectStore(storeName));
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
      }),
  );
}

export function dbPut<T>(store: OfflineStore, value: T) {
  return complete(store, 'readwrite', (objectStore) => objectStore.put(value));
}

export function dbDelete(store: OfflineStore, key: IDBValidKey) {
  return complete(store, 'readwrite', (objectStore) => objectStore.delete(key));
}

export function dbGet<T>(store: OfflineStore, key: IDBValidKey) {
  return complete(store, 'readonly', (objectStore) => objectStore.get(key)) as Promise<T | undefined>;
}

export function dbGetAll<T>(store: OfflineStore) {
  return complete(store, 'readonly', (objectStore) => objectStore.getAll()) as Promise<T[]>;
}

export async function requestPersistentStorage() {
  try {
    await navigator.storage?.persist?.();
  } catch {
    // Persistence is a best-effort browser capability.
  }
}
