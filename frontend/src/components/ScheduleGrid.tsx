import { Fragment, useEffect, useMemo, useState } from 'react';
import { Box, Typography, alpha, useTheme } from '@mui/material';
import type { ScheduleBlock } from '../types/schedule';
import {
  DAY_LABELS,
  GRID_END,
  GRID_START,
  SLOT_PX,
  blockHeight,
  minutesToTop,
  timeToMinutes,
  todayDow,
} from '../types/schedule';

type Props = {
  blocks: ScheduleBlock[];
  gridStart?: number;
  gridEnd?: number;
  onCellClick: (diaSemana: number, hora: string) => void;
  onBlockClick: (block: ScheduleBlock) => void;
};

const TIME_COL = 56;
const COL_TEMPLATE = `${TIME_COL}px repeat(7, minmax(0, 1fr))`;

export default function ScheduleGrid({
  blocks,
  gridStart = GRID_START,
  gridEnd = GRID_END,
  onCellClick,
  onBlockClick,
}: Props) {
  const theme = useTheme();
  const hoy = todayDow();
  const hours = useMemo(
    () => Array.from({ length: gridEnd - gridStart }, (_, i) => gridStart + i),
    [gridEnd, gridStart],
  );
  const gridHeight = hours.length * SLOT_PX;
  const [nowTop, setNowTop] = useState<number | null>(null);
  const [nowLabel, setNowLabel] = useState('');

  const borderColor = theme.palette.divider;
  const todayBg = alpha(theme.palette.primary.main, theme.palette.mode === 'light' ? 0.08 : 0.14);
  const cellBg = theme.palette.mode === 'light' ? alpha('#ffffff', 0.55) : alpha('#1e293b', 0.45);

  useEffect(() => {
    function tick() {
      const now = new Date();
      const minutes = now.getHours() * 60 + now.getMinutes();
      if (minutes < gridStart * 60 || minutes >= gridEnd * 60) {
        setNowTop(null);
        return;
      }
      setNowTop(minutesToTop(minutes, gridStart));
      setNowLabel(
        `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`,
      );
    }
    tick();
    const id = window.setInterval(tick, 60_000);
    return () => window.clearInterval(id);
  }, [gridEnd, gridStart]);

  return (
    <Box sx={{ width: '100%', overflowX: 'auto' }}>
      <Box sx={{ minWidth: 680, width: '100%' }}>
        {/* Cabecera días */}
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: COL_TEMPLATE,
            borderBottom: `1px solid ${borderColor}`,
            bgcolor: alpha(theme.palette.background.paper, 0.9),
          }}
        >
          <Box sx={{ borderRight: `1px solid ${borderColor}` }} />
          {DAY_LABELS.slice(1).map((label, idx) => {
            const dia = idx + 1;
            return (
              <Box
                key={dia}
                sx={{
                  py: 1.25,
                  textAlign: 'center',
                  fontWeight: 700,
                  fontSize: { xs: '0.7rem', sm: '0.85rem' },
                  borderRight: dia < 7 ? `1px solid ${borderColor}` : 'none',
                  bgcolor: dia === hoy ? todayBg : 'transparent',
                  color: dia === hoy ? 'primary.main' : 'text.secondary',
                }}
              >
                {label}
              </Box>
            );
          })}
        </Box>

        {/* Grilla */}
        <Box sx={{ position: 'relative', height: gridHeight }}>
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: COL_TEMPLATE,
              gridTemplateRows: `repeat(${hours.length}, ${SLOT_PX}px)`,
              height: gridHeight,
            }}
          >
            {hours.map((h) => (
              <Fragment key={h}>
                <Box
                  sx={{
                    position: 'relative',
                    borderRight: `1px solid ${borderColor}`,
                    borderBottom: `1px solid ${borderColor}`,
                    bgcolor: alpha(theme.palette.background.paper, 0.75),
                  }}
                >
                  <Typography
                    component="span"
                    sx={{
                      position: 'absolute',
                      top: 0,
                      right: 6,
                      transform: 'translateY(-50%)',
                      fontSize: '0.7rem',
                      fontWeight: 600,
                      color: 'text.secondary',
                      bgcolor: 'background.paper',
                      px: 0.25,
                      lineHeight: 1,
                    }}
                  >
                    {String(h).padStart(2, '0')}:00
                  </Typography>
                </Box>
                {DAY_LABELS.slice(1).map((_, idx) => {
                  const dia = idx + 1;
                  return (
                    <Box
                      key={`${h}-${dia}`}
                      component="button"
                      type="button"
                      onClick={() => onCellClick(dia, `${String(h).padStart(2, '0')}:00`)}
                      aria-label={`Agregar clase ${DAY_LABELS[dia]} ${h}:00`}
                      sx={{
                        border: 'none',
                        borderRight: dia < 7 ? `1px solid ${borderColor}` : 'none',
                        borderBottom: `1px solid ${borderColor}`,
                        bgcolor: dia === hoy ? todayBg : cellBg,
                        cursor: 'pointer',
                        p: 0,
                        transition: 'background-color 0.15s',
                        '&:hover': { bgcolor: alpha(theme.palette.primary.main, 0.12) },
                      }}
                    />
                  );
                })}
              </Fragment>
            ))}
          </Box>

          {/* Bloques */}
          <Box
            sx={{
              position: 'absolute',
              top: 0,
              left: TIME_COL,
              right: 0,
              height: gridHeight,
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              pointerEvents: 'none',
              zIndex: 2,
            }}
          >
            {DAY_LABELS.slice(1).map((_, idx) => {
              const dia = idx + 1;
              const dayBlocks = blocks.filter((b) => b.diaSemana === dia);
              return (
                <Box key={dia} sx={{ position: 'relative', height: '100%' }}>
                  {dayBlocks.map((block) => {
                    const top = minutesToTop(timeToMinutes(block.horaInicio), gridStart);
                    const height = blockHeight(block.horaInicio, block.horaFin);
                    return (
                      <Box
                        key={block.id}
                        component="button"
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          onBlockClick(block);
                        }}
                        sx={{
                          position: 'absolute',
                          left: 4,
                          right: 4,
                          top,
                          height,
                          borderRadius: 2,
                          px: 1,
                          py: 0.75,
                          textAlign: 'left',
                          color: '#fff',
                          fontSize: '0.72rem',
                          boxShadow: '0 4px 12px rgba(0,0,0,0.18)',
                          overflow: 'hidden',
                          pointerEvents: 'auto',
                          border: 'none',
                          cursor: 'pointer',
                          bgcolor: block.color || '#5082ef',
                          transition: 'filter 0.15s, transform 0.1s',
                          '&:hover': { filter: 'brightness(1.08)' },
                          '&:active': { transform: 'scale(0.99)' },
                        }}
                      >
                        <Typography fontWeight={700} fontSize="inherit" noWrap display="block">
                          {block.materia}
                        </Typography>
                        <Typography fontSize="0.65rem" display="block" sx={{ opacity: 0.95 }}>
                          {block.horaInicio} – {block.horaFin}
                        </Typography>
                        {block.profesor && (
                          <Typography fontSize="0.6rem" display="block" noWrap sx={{ opacity: 0.9 }}>
                            {block.profesor}
                          </Typography>
                        )}
                      </Box>
                    );
                  })}
                </Box>
              );
            })}
          </Box>

          {nowTop != null && (
            <Box
              sx={{
                position: 'absolute',
                left: TIME_COL,
                right: 0,
                top: nowTop,
                height: 2,
                bgcolor: 'error.main',
                zIndex: 3,
                pointerEvents: 'none',
              }}
            >
              <Typography
                component="span"
                sx={{
                  position: 'absolute',
                  left: -52,
                  top: -10,
                  fontSize: '0.6rem',
                  fontWeight: 700,
                  color: '#fff',
                  bgcolor: 'error.main',
                  px: 0.5,
                  borderRadius: 1,
                  lineHeight: 1.6,
                }}
              >
                {nowLabel}
              </Typography>
            </Box>
          )}
        </Box>
      </Box>
    </Box>
  );
}
