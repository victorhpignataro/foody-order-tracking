import { Box, Container } from '@mui/material';

import { OrderTable } from '@/components/order-table';

export const HomePage = () => {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Container
        maxWidth={false}
        sx={{
          py: 2,
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
        }}>
        <OrderTable></OrderTable>
      </Container>
    </Box>
  );
};
