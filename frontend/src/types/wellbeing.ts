export type WellbeingStats = {
  minutosPomodoro: number;
  sesionesPomodoro: number;
  totalPomodoros: number;
  totalPausas: number;
  ultimasSesiones?: {
    id: number;
    tipo: string;
    valor?: number;
    descripcion?: string;
    fecha?: string;
  }[];
  error?: string;
};

export type StressReport = {
  nivel: number;
  factores: string[];
  consejo: string;
  error?: string;
};

export const PAUSE_TYPES = [
  { value: 'ESTIRAMIENTO', label: 'Estiramiento', icon: '🧘', minutes: 5 },
  { value: 'RESPIRACION', label: 'Respiración', icon: '🌬️', minutes: 3 },
  { value: 'CAMINATA', label: 'Caminata corta', icon: '🚶', minutes: 5 },
  { value: 'GENERAL', label: 'Descanso general', icon: '☕', minutes: 5 },
] as const;

/** Duraciones de enfoque sugeridas (minutos). */
export const POMODORO_WORK_PRESETS = [15, 25, 45, 60] as const;

export const POMODORO_WORK_MIN = 25;

/**
 * Descanso proporcional al tiempo de enfoque:
 * - ≤10 min → 2 min
 * - 11–15 min → 3 min
 * - 16–30 min → 5 min (clásico)
 * - >30 min → ~20 % del trabajo, máx. 10 min
 */
export function pomodoroBreakMinutes(workMinutes: number): number {
  if (workMinutes <= 10) return 2;
  if (workMinutes <= 15) return 3;
  if (workMinutes <= 30) return 5;
  return Math.min(10, Math.round(workMinutes * 0.2));
}

export function stressLevelLabel(nivel: number) {
  if (nivel >= 70) return 'Alto';
  if (nivel >= 40) return 'Moderado';
  return 'Bajo';
}

export function stressLevelClass(nivel: number) {
  if (nivel >= 70) return 'stress-high';
  if (nivel >= 40) return 'stress-mid';
  return 'stress-low';
}
