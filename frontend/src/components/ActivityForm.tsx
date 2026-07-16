import { FormEvent, useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  Checkbox,
  FormControl,
  FormControlLabel,
  FormGroup,
  FormLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { api, UsuarioDto } from '../api/client';
import {
  ACTIVITY_STATES,
  ACTIVITY_TYPES,
  ACTIVITY_COLORS,
  ActividadDetail,
  CreateActividadPayload,
  isGroupActivityType,
} from '../types/activity';
import ColorSwatchPicker from './ColorSwatchPicker';
import { localDateIso } from '../utils/localDate';

export type ActivityFormValues = {
  titulo: string;
  tipo: string;
  fechaInicio: string;
  horaInicio: string;
  duracionMinutos: number;
  materia: string;
  prioridad: string;
  descripcion: string;
  estado: string;
  companerosIds: number[];
  color?: string;
};

type Props = {
  initial?: Partial<ActivityFormValues>;
  submitLabel: string;
  onSubmit: (payload: CreateActividadPayload & { estado?: string }) => Promise<string | null>;
  onCancelTo?: string;
};

function todayIso() {
  return localDateIso();
}

export default function ActivityForm({
  initial,
  submitLabel,
  onSubmit,
  onCancelTo = '/activities',
}: Props) {
  const [titulo, setTitulo] = useState(initial?.titulo ?? '');
  const [tipo, setTipo] = useState(initial?.tipo ?? 'DEBER');
  const [fechaInicio, setFechaInicio] = useState(initial?.fechaInicio ?? todayIso());
  const [horaInicio, setHoraInicio] = useState(initial?.horaInicio ?? '09:00');
  const [duracionMinutos, setDuracionMinutos] = useState(initial?.duracionMinutos ?? 60);
  const [materia, setMateria] = useState(initial?.materia ?? '');
  const [prioridad, setPrioridad] = useState(initial?.prioridad ?? 'MEDIA');
  const [descripcion, setDescripcion] = useState(initial?.descripcion ?? '');
  const [estado, setEstado] = useState(initial?.estado ?? 'PENDIENTE');
  const [companerosIds, setCompanerosIds] = useState<number[]>(initial?.companerosIds ?? []);
  const [color, setColor] = useState(initial?.color ?? '#3b82f6');
  const [connections, setConnections] = useState<UsuarioDto[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isGroupActivityType(tipo)) {
      setCompanerosIds([]);
      return;
    }
    api.community.connections().then((res) => {
      if (res.ok && res.data) setConnections(res.data);
    });
  }, [tipo]);

  function toggleCompanion(id: number) {
    setCompanerosIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    const err = await onSubmit({
      titulo,
      tipo,
      fechaInicio,
      horaInicio: horaInicio || undefined,
      duracionMinutos,
      materia: materia || undefined,
      prioridad,
      descripcion: descripcion || undefined,
      companerosIds: isGroupActivityType(tipo) && companerosIds.length > 0 ? companerosIds : undefined,
      color,
      estado,
    });
    if (err) setError(err);
    setSubmitting(false);
  }

  return (
    <Card component="form" onSubmit={handleSubmit} sx={{ p: { xs: 2, sm: 3 } }}>
      <Stack spacing={2.5}>
        {error && <Alert severity="error">{error}</Alert>}

        <TextField
          label="Título"
          value={titulo}
          onChange={(e) => setTitulo(e.target.value)}
          required
          inputProps={{ maxLength: 200 }}
        />

        <FormControl>
          <InputLabel>Tipo</InputLabel>
          <Select label="Tipo" value={tipo} onChange={(e) => setTipo(e.target.value)}>
            {ACTIVITY_TYPES.map((t) => (
              <MenuItem key={t.value} value={t.value}>
                {t.label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField
            label="Fecha"
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
            required
            InputLabelProps={{ shrink: true }}
            fullWidth
          />
          <TextField
            label="Hora"
            type="time"
            value={horaInicio}
            onChange={(e) => setHoraInicio(e.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
          />
        </Stack>

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField
            label="Duración (min)"
            type="number"
            inputProps={{ min: 15, step: 15 }}
            value={duracionMinutos}
            onChange={(e) => setDuracionMinutos(Number(e.target.value))}
            fullWidth
          />
          <FormControl fullWidth>
            <InputLabel>Prioridad</InputLabel>
            <Select label="Prioridad" value={prioridad} onChange={(e) => setPrioridad(e.target.value)}>
              <MenuItem value="ALTA">Alta</MenuItem>
              <MenuItem value="MEDIA">Media</MenuItem>
              <MenuItem value="BAJA">Baja</MenuItem>
            </Select>
          </FormControl>
        </Stack>

        {initial?.estado !== undefined && (
          <FormControl>
            <InputLabel>Estado</InputLabel>
            <Select label="Estado" value={estado} onChange={(e) => setEstado(e.target.value)}>
              {ACTIVITY_STATES.map((s) => (
                <MenuItem key={s.value} value={s.value}>
                  {s.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        <TextField
          label="Materia / etiqueta (opcional)"
          value={materia}
          onChange={(e) => setMateria(e.target.value)}
        />

        <ColorSwatchPicker
          value={color}
          onChange={setColor}
          colors={ACTIVITY_COLORS}
          legend="Color en calendario"
        />

        <TextField
          label="Descripción (opcional)"
          multiline
          rows={3}
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
        />

        {isGroupActivityType(tipo) && (
          <FormControl component="fieldset">
            <FormLabel component="legend">Compañeros conectados</FormLabel>
            {connections.length === 0 ? (
              <Typography variant="body2" color="text.secondary" mt={1}>
                No tienes conexiones aún.{' '}
                <Typography component={RouterLink} to="/community" variant="body2" color="primary">
                  Ir a Comunidad
                </Typography>{' '}
                para enviar solicitudes.
              </Typography>
            ) : (
              <FormGroup>
                {connections.map((c) => (
                  <FormControlLabel
                    key={c.id}
                    control={
                      <Checkbox
                        checked={companerosIds.includes(c.id)}
                        onChange={() => toggleCompanion(c.id)}
                      />
                    }
                    label={
                      <Box>
                        {c.nombre}
                        <Typography variant="caption" color="text.secondary" display="block">
                          {c.correo}
                        </Typography>
                      </Box>
                    }
                  />
                ))}
              </FormGroup>
            )}
          </FormControl>
        )}

        <Stack direction={{ xs: 'column-reverse', sm: 'row' }} spacing={1} justifyContent="flex-end">
          <Button component={RouterLink} to={onCancelTo}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? 'Guardando…' : submitLabel}
          </Button>
        </Stack>
      </Stack>
    </Card>
  );
}

export function detailToFormValues(detail: ActividadDetail): ActivityFormValues {
  return {
    titulo: detail.titulo,
    tipo: detail.tipo,
    fechaInicio: detail.fechaInicio ?? todayIso(),
    horaInicio: detail.horaInicio?.slice(0, 5) ?? '09:00',
    duracionMinutos: detail.duracionMinutos ?? 60,
    materia: detail.materia ?? '',
    prioridad: detail.prioridad ?? 'MEDIA',
    descripcion: detail.descripcion ?? '',
    estado: detail.estado,
    companerosIds: detail.companerosIds ?? [],
    color: detail.color ?? '#3b82f6',
  };
}
