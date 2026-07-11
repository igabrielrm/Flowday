import type { UsuarioDto } from '../api/client';

export type CommunityStats = {
  totalUsuarios: number;
  totalConexiones: number;
  tasaConexion: number;
};

export type CommunityUser = {
  user: UsuarioDto;
  compatibilidad: number;
  conectado: boolean;
  estadoRelacion: 'NINGUNA' | 'CONECTADO' | 'SOLICITUD_ENVIADA' | 'SOLICITUD_RECIBIDA';
  conexionId?: number | null;
};

export function compatLabel(score: number) {
  if (score > 70) return { text: 'Compatibilidad alta', level: 'alta' as const };
  if (score > 40) return { text: 'Compatibilidad media', level: 'media' as const };
  return { text: 'Compatibilidad baja', level: 'baja' as const };
}

export function userInitials(nombre: string) {
  return nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p.charAt(0).toUpperCase())
    .join('');
}
