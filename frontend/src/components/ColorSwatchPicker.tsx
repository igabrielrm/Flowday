import { memo } from 'react';
import { Box, FormLabel, IconButton, Stack } from '@mui/material';
import CheckIcon from '@mui/icons-material/Check';

type ColorOption = { value: string; label: string };

type Props = {
  value: string;
  onChange: (color: string) => void;
  colors: readonly ColorOption[];
  legend?: string;
};

function ColorSwatchPicker({ value, onChange, colors, legend = 'Color' }: Props) {
  return (
    <Box component="fieldset" sx={{ border: 'none', p: 0, m: 0 }}>
      <FormLabel component="legend" sx={{ mb: 1, display: 'block' }}>
        {legend}
      </FormLabel>
      <Stack direction="row" flexWrap="wrap" gap={1.75} role="radiogroup" aria-label={legend}>
        {colors.map((c) => (
          <IconButton
            key={c.value}
            role="radio"
            aria-checked={value === c.value}
            title={c.label}
            onClick={() => {
              if (value !== c.value) onChange(c.value);
            }}
            sx={{
              width: 40,
              height: 40,
              bgcolor: c.value,
              border: 2,
              borderColor: value === c.value ? 'primary.main' : 'transparent',
              '&:hover': { bgcolor: c.value, opacity: 0.9 },
            }}
          >
            {value === c.value && <CheckIcon sx={{ color: '#fff', fontSize: 18 }} />}
          </IconButton>
        ))}
      </Stack>
    </Box>
  );
}

export default memo(ColorSwatchPicker);
