export const CHAT_UNREAD_EVENT = 'flowday:chat-unread';

export function notifyChatUnreadChanged() {
  window.dispatchEvent(new CustomEvent(CHAT_UNREAD_EVENT));
}
