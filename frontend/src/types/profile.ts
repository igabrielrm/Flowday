export type Profile = {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  telefono?: string | null;
  fechaNacimiento?: string | null;
  genero?: string | null;
  nombreEmergencia?: string | null;
  telefonoEmergencia?: string | null;
  relacionEmergencia?: string | null;
  tema?: string | null;
  foto?: string | null;
};

export type UpdateProfilePayload = {
  nombre: string;
  telefono?: string;
  fechaNacimiento?: string;
  genero?: string;
  nombreEmergencia?: string;
  telefonoEmergencia?: string;
  relacionEmergencia?: string;
};

export const GENERO_OPTIONS = [
  'Masculino',
  'Femenino',
  'No binario',
  'Prefiero no decir',
] as const;

export function profileInitials(nombre: string) {
  return nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p.charAt(0).toUpperCase())
    .join('');
}

export function applyTheme(tema: string) {
  const value = tema === 'light' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', value);
  localStorage.setItem('theme', value);
  window.dispatchEvent(new Event('flowday-theme-change'));
}
