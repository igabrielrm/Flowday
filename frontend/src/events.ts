export const CHAT_UNREAD_EVENT = 'flowday:chat-unread';
export const OFFLINE_QUEUE_EVENT = 'flowday:offline-queue';
export const ASSISTANT_ACTION_EVENT = 'flowday:assistant-action';

export function notifyChatUnreadChanged() {
  window.dispatchEvent(new CustomEvent(CHAT_UNREAD_EVENT));
}

export function notifyOfflineQueueChanged() {
  window.dispatchEvent(new CustomEvent(OFFLINE_QUEUE_EVENT));
}
