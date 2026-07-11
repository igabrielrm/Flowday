import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  LinearProgress,
  Stack,
  Typography,
  useTheme,
} from '@mui/material';
import { modalSlotProps } from '../theme/modal';

type Phase = 'inhale' | 'hold' | 'exhale' | 'rest';

const CYCLE: { phase: Phase; seconds: number; label: string; hint: string }[] = [
  { phase: 'inhale', seconds: 4, label: 'Inhala', hint: 'Llena los pulmones suavemente por la nariz' },
  { phase: 'hold', seconds: 4, label: 'Mantén', hint: 'Quédate en calma, hombros relajados' },
  { phase: 'exhale', seconds: 6, label: 'Exhala', hint: 'Suelta el aire lentamente por la boca' },
  { phase: 'rest', seconds: 2, label: 'Pausa', hint: 'Respira con naturalidad antes del siguiente ciclo' },
];

type Props = {
  open: boolean;
  onClose: () => void;
  onComplete?: () => void;
};

export default function BreathingModal({ open, onClose, onComplete }: Props) {
  const theme = useTheme();
  const [running, setRunning] = useState(false);
  const [cycleIndex, setCycleIndex] = useState(0);
  const [secondsLeft, setSecondsLeft] = useState(CYCLE[0].seconds);
  const [round, setRound] = useState(1);
  const totalRounds = 4;

  const step = CYCLE[cycleIndex];
  const progress = ((step.seconds - secondsLeft) / step.seconds) * 100;

  useEffect(() => {
    if (!open) {
      setRunning(false);
      setCycleIndex(0);
      setSecondsLeft(CYCLE[0].seconds);
      setRound(1);
    }
  }, [open]);

  useEffect(() => {
    if (!running || !open) return undefined;
    const id = window.setInterval(() => {
      setSecondsLeft((s) => {
        if (s > 1) return s - 1;
        const nextIndex = (cycleIndex + 1) % CYCLE.length;
        if (nextIndex === 0) {
          if (round >= totalRounds) {
            setRunning(false);
            onComplete?.();
            return 0;
          }
          setRound((r) => r + 1);
        }
        setCycleIndex(nextIndex);
        return CYCLE[nextIndex].seconds;
      });
    }, 1000);
    return () => window.clearInterval(id);
  }, [running, open, cycleIndex, round, onComplete]);

  const circleScale =
    step.phase === 'inhale' ? 1.15 : step.phase === 'exhale' ? 0.85 : step.phase === 'hold' ? 1.15 : 1;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth slotProps={modalSlotProps(theme)}>
      <DialogTitle>Respiración guiada</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={3} alignItems="center" textAlign="center" py={1}>
          <Typography variant="body2" color="text.secondary">
            Técnica 4-4-6 para calmar la ansiedad. Sigue el círculo y las indicaciones.
          </Typography>
          <Box
            sx={{
              width: 140,
              height: 140,
              borderRadius: '50%',
              bgcolor: 'primary.main',
              opacity: 0.85,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'primary.contrastText',
              transition: 'transform 1s ease-in-out',
              transform: `scale(${circleScale})`,
            }}
          >
            <Typography variant="h4" fontWeight={700}>
              {secondsLeft}
            </Typography>
          </Box>
          <Box width="100%">
            <Typography variant="h6">{step.label}</Typography>
            <Typography variant="body2" color="text.secondary" mt={0.5}>
              {step.hint}
            </Typography>
            <LinearProgress variant="determinate" value={progress} sx={{ mt: 2, borderRadius: 2, height: 6 }} />
          </Box>
          <Typography variant="caption" color="text.secondary">
            Ciclo {Math.min(round, totalRounds)} de {totalRounds}
          </Typography>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cerrar</Button>
        {!running ? (
          <Button variant="contained" onClick={() => setRunning(true)}>
            {round > 1 && secondsLeft === 0 ? 'Completado' : 'Empezar'}
          </Button>
        ) : (
          <Button variant="outlined" onClick={() => setRunning(false)}>
            Pausar
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
