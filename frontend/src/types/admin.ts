export type AdminStats = {
  totalUsuarios: number;
  totalUsers: number;
  totalActividades: number;
  actividadesPendientes: number;
  actividadesCompletadas: number;
  totalPomodorosSemana: number;
  totalPausasSemana: number;
  actividadesPorMateria: { materia: string; total: number }[];
  actividadesPorDia: { fecha: string; total: number }[];
  promedioActividadesPorUsuario?: number;
};

export type AdminUser = {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  rolDisplay?: string;
};

export type AdminTopUser = {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  cohorte?: string;
  totalActividades: number;
  completadas: number;
};

export type AdminAnnouncement = {
  id: number;
  titulo: string;
  descripcion: string;
  fechaLimite: string;
  fechaPublicacion?: string;
  estado: string;
};

export type CohortRow = {
  cohorte?: string;
  carrera?: string;
  usuarios?: number;
  estresPromedio?: number;
  pomodoros?: number;
  pausas?: number;
  alerta?: string;
  nivel?: string;
};

export type CriticalWeek = {
  semana: string;
  actividades: number;
  alerta?: string;
};

export type AdminWellbeing = {
  cargaPorCohorte: CohortRow[];
  semanasCriticas: CriticalWeek[];
  totalPomodorosSemana?: number;
  totalPausasSemana?: number;
};

export type AnalyticsSeries = Record<string, unknown>;
