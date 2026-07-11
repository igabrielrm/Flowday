declare module 'sockjs-client' {
  export default class SockJS {
    constructor(url: string, _reserved?: unknown, options?: Record<string, unknown>);
    close(): void;
  }
}
