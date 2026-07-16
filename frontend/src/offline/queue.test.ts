import 'fake-indexeddb/auto';
import { beforeEach, describe, expect, it } from 'vitest';
import { dbDelete, dbGetAll } from './db';
import { enqueue, readQueue } from './queue';

class MemoryStorage implements Storage {
  private values = new Map<string, string>();

  get length() { return this.values.size; }
  clear() { this.values.clear(); }
  getItem(key: string) { return this.values.get(key) ?? null; }
  key(index: number) { return [...this.values.keys()][index] ?? null; }
  removeItem(key: string) { this.values.delete(key); }
  setItem(key: string, value: string) { this.values.set(key, value); }
}

Object.defineProperty(globalThis, 'localStorage', {
  value: new MemoryStorage(),
  configurable: true,
});

function setUser(id: number) {
  localStorage.setItem(
    'flowday-offline-user',
    JSON.stringify({
      data: { id, nombre: `User ${id}`, correo: `u${id}@example.com`, rol: 'USER' },
      savedAt: Date.now(),
    }),
  );
}

async function clearDatabase() {
  for (const store of ['outbox', 'cache', 'meta'] as const) {
    const entries = await dbGetAll<{ id: string }>(store);
    await Promise.all(entries.map((entry) => dbDelete(store, entry.id)));
  }
}

beforeEach(async () => {
  localStorage.clear();
  await clearDatabase();
});

describe('offline outbox', () => {
  it('migrates the legacy localStorage queue once', async () => {
    setUser(1);
    localStorage.setItem(
      'flowday-offline-queue',
      JSON.stringify([{
        id: 'legacy-operation',
        kind: 'activity.delete',
        label: 'Eliminar',
        method: 'DELETE',
        path: '/api/v1/activities/5',
        entityId: 5,
        createdAt: 1,
      }]),
    );

    const queue = await readQueue(true);

    expect(queue).toHaveLength(1);
    expect(queue[0]).toMatchObject({
      id: 'legacy-operation',
      userId: '1',
      status: 'PENDING',
    });
    expect(localStorage.getItem('flowday-offline-queue')).toBeNull();
  });

  it('compacts create-update-delete for a temporary entity', async () => {
    setUser(1);
    await enqueue({
      kind: 'activity.create',
      label: 'Crear',
      method: 'POST',
      path: '/api/v1/activities',
      body: JSON.stringify({ titulo: 'Inicial' }),
      entityId: -1,
      tempId: -1,
    });
    await enqueue({
      kind: 'activity.update',
      label: 'Editar',
      method: 'PUT',
      path: '/api/v1/activities/-1',
      body: JSON.stringify({ titulo: 'Final' }),
      entityId: -1,
    });

    const compacted = await readQueue(true);
    expect(compacted).toHaveLength(1);
    expect(JSON.parse(compacted[0].body!)).toMatchObject({ titulo: 'Final' });

    await enqueue({
      kind: 'activity.delete',
      label: 'Eliminar',
      method: 'DELETE',
      path: '/api/v1/activities/-1',
      entityId: -1,
    });
    expect(await readQueue(true)).toHaveLength(0);
  });

  it('keeps outboxes isolated between users', async () => {
    setUser(1);
    await enqueue({
      kind: 'notification.readAll',
      label: 'Usuario uno',
      method: 'POST',
      path: '/api/v1/notifications/read-all',
    });

    setUser(2);
    expect(await readQueue(true)).toHaveLength(0);
    await enqueue({
      kind: 'notification.readAll',
      label: 'Usuario dos',
      method: 'POST',
      path: '/api/v1/notifications/read-all',
    });

    setUser(1);
    expect((await readQueue(true)).map((item) => item.label)).toEqual(['Usuario uno']);
  });
});
