import { ReactNode } from 'react';
import { Stack, SxProps, Theme } from '@mui/material';

type Props = {
  children: ReactNode;
  sx?: SxProps<Theme>;
};

export default function PageStack({ children, sx }: Props) {
  return (
    <Stack
      spacing={3}
      sx={{ width: '100%', flex: 1, minHeight: 0, pb: { xs: 1, md: 0 }, ...sx }}
    >
      {children}
    </Stack>
  );
}
