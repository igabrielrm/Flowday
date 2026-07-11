import { ReactNode } from 'react';
import { Stack } from '@mui/material';

export default function PageStack({ children }: { children: ReactNode }) {
  return (
    <Stack spacing={3} sx={{ width: '100%', pb: { xs: 2, md: 0 } }}>
      {children}
    </Stack>
  );
}
