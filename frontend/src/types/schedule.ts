export type ScheduleBlock = {
  id: number;
  version?: number;
  materia: string;
  diaSemana: number;
  diaNombre: string;
  horaInicio: string;
  horaFin: string;
  aula?: string | null;
  profesor?: string | null;
  color: string;
};

export type ScheduleAlert = ScheduleBlock & {
  enCurso: boolean;
  mensaje: string;
};

export type CreateScheduleBlockPayload = {
  materia: string;
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
  aula?: string;
  profesor?: string;
  color?: string;
};

export const GRID_START = 7;
export const GRID_END = 21;
export const SLOT_PX = 52;

export const DAY_LABELS = ['', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

export const DAY_OPTIONS = [
  { value: 1, label: 'Lunes' },
  { value: 2, label: 'Martes' },
  { value: 3, label: 'Miércoles' },
  { value: 4, label: 'Jueves' },
  { value: 5, label: 'Viernes' },
  { value: 6, label: 'Sábado' },
  { value: 7, label: 'Domingo' },
];

export function todayDow() {
  const d = new Date().getDay();
  return d === 0 ? 7 : d;
}

export function timeToMinutes(t: string) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

export function minutesToTop(min: number, gridStart = GRID_START) {
  return ((min - gridStart * 60) / 60) * SLOT_PX;
}

export function sumarHora(hora: string, horas: number) {
  const m = timeToMinutes(hora) + horas * 60;
  const hh = Math.min(Math.floor(m / 60), 23);
  const mm = m % 60;
  return `${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`;
}

export function blockHeight(horaInicio: string, horaFin: string) {
  const diff = timeToMinutes(horaFin) - timeToMinutes(horaInicio);
  return Math.max((diff / 60) * SLOT_PX - 4, 28);
}
