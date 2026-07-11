import { FormEvent, useCallback, useEffect, useRef, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  Fab,
  IconButton,
  Stack,
  TextField,
  Tooltip,
  Typography,
  useTheme,
} from '@mui/material';
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined';
import SendRoundedIcon from '@mui/icons-material/SendRounded';
import CloseIcon from '@mui/icons-material/Close';
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined';
import { api } from '../api/client';
import { modalSlotProps } from '../theme/modal';

type Msg = { rol: 'user' | 'assistant'; contenido: string };

const STORAGE_KEY = 'flowday-ia-chat';

function loadStoredMessages(): Msg[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as Msg[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

type Props = {
  /** En chat, el FAB va arriba a la derecha para no tapar el enviar. */
  fabOnTop?: boolean;
};

export default function VirtualCompanion({ fabOnTop = false }: Props) {
  const theme = useTheme();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<Msg[]>(() => loadStoredMessages());
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [iaReady, setIaReady] = useState<boolean | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  const persistMessages = useCallback((next: Msg[]) => {
    setMessages(next);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } catch {
      /* ignore quota */
    }
  }, []);

  useEffect(() => {
    if (!open) return;
    api.ia.status().then((res) => {
      if (res.ok && res.data) setIaReady(res.data.ready);
      else setIaReady(false);
    });
  }, [open]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, open]);

  function clearChat() {
    persistMessages([]);
  }

  async function send(e: FormEvent) {
    e.preventDefault();
    if (!text.trim()) return;
    const userMsg = text.trim();
    setText('');
    const next = [...messages, { rol: 'user' as const, contenido: userMsg }];
    persistMessages(next);
    setSending(true);
    const res = await api.ia.chat(
      userMsg,
      next.map((m) => ({ rol: m.rol, contenido: m.contenido })),
    );
    if (res.ok && res.data?.respuesta) {
      const note = res.data.fallback ? ' (modo sin IA — revisa Groq en el servidor)' : '';
      persistMessages([...next, { rol: 'assistant', contenido: res.data!.respuesta + note }]);
    } else {
      persistMessages([
        ...next,
        {
          rol: 'assistant',
          contenido:
            res.error ||
            'No pude responder. Verifica que GROQ_API_KEY esté en .env y reinicia el backend.',
        },
      ]);
    }
    setSending(false);
  }

  return (
    <>
      <Fab
        color="primary"
        aria-label="Compañero virtual"
        onClick={() => setOpen(true)}
        size={fabOnTop ? 'medium' : 'large'}
        sx={{
          position: 'fixed',
          right: 16,
          zIndex: (t) => t.zIndex.speedDial,
          ...(fabOnTop
            ? { top: { xs: 72, md: 80 } }
            : { bottom: { xs: 'calc(64px + env(safe-area-inset-bottom))', md: 24 } }),
        }}
      >
        <SmartToyOutlinedIcon />
      </Fab>

      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        fullWidth
        maxWidth="sm"
        slotProps={{
          paper: {
            sx: {
              ...modalSlotProps(theme).paper.sx,
              height: { xs: '72dvh', sm: 520 },
              display: 'flex',
              flexDirection: 'column',
            },
          },
        }}
      >
        <Stack
          direction="row"
          alignItems="center"
          justifyContent="space-between"
          sx={{ flexShrink: 0, borderBottom: 1, borderColor: 'divider', px: 2.5, py: 1.75 }}
        >
          <Typography variant="h6" fontWeight={700}>
            Compañero virtual
          </Typography>
          <Stack direction="row" spacing={0.5} alignItems="center">
            {messages.length > 0 && (
              <Tooltip title="Limpiar conversación">
                <IconButton size="small" onClick={clearChat} aria-label="Limpiar chat">
                  <DeleteOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
            <IconButton onClick={() => setOpen(false)} aria-label="Cerrar" sx={{ ml: 0.5 }}>
              <CloseIcon />
            </IconButton>
          </Stack>
        </Stack>

        {iaReady === false && (
          <Alert severity="warning" sx={{ mx: 2.5, mt: 1.5, flexShrink: 0 }}>
            Groq no está configurado. Añade GROQ_API_KEY al archivo .env en la raíz del proyecto y reinicia el backend.
          </Alert>
        )}

        <Box sx={{ flex: 1, overflow: 'auto', px: 2.5, py: 2, bgcolor: 'background.paper' }}>
          <Stack spacing={1.5}>
            {messages.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                Pregúntame sobre organización, estudio o bienestar. Tu historial se guarda en este dispositivo.
              </Typography>
            )}
            {messages.map((m, i) => (
              <Box
                key={i}
                sx={{
                  alignSelf: m.rol === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '85%',
                  px: 1.5,
                  py: 1.25,
                  borderRadius: m.rol === 'user' ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                  bgcolor: m.rol === 'user' ? 'primary.main' : 'action.hover',
                  color: m.rol === 'user' ? 'primary.contrastText' : 'text.primary',
                }}
              >
                <Typography variant="body2" sx={{ lineHeight: 1.5 }}>
                  {m.contenido}
                </Typography>
              </Box>
            ))}
            <div ref={bottomRef} />
          </Stack>
        </Box>

        <Box
          component="form"
          onSubmit={send}
          sx={{
            flexShrink: 0,
            px: 2.5,
            py: 1.75,
            borderTop: 1,
            borderColor: 'divider',
            bgcolor: 'background.paper',
          }}
        >
          <Stack direction="row" spacing={1} alignItems="flex-end">
            <TextField
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Escribe tu mensaje…"
              slotProps={{ htmlInput: { maxLength: 500 } }}
              size="small"
              fullWidth
              disabled={sending}
            />
            <IconButton
              type="submit"
              disabled={sending || !text.trim()}
              sx={{
                width: 44,
                height: 44,
                flexShrink: 0,
                bgcolor: 'primary.main',
                color: 'primary.contrastText',
                '&:hover': { bgcolor: 'primary.dark' },
              }}
            >
              <SendRoundedIcon fontSize="small" />
            </IconButton>
          </Stack>
        </Box>
      </Dialog>
    </>
  );
}
