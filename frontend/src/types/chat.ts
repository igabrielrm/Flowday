import type { UsuarioDto } from '../api/client';

export type ChatMessage = {
  id: number;
  remitenteId: number;
  destinatarioId: number;
  contenido: string;
  fecha?: string | null;
  leida: boolean;
  propio: boolean;
};

export type Conversation = {
  user: UsuarioDto;
  ultimoMensaje?: string | null;
  ultimaFecha?: string | null;
  noLeidos: number;
};
