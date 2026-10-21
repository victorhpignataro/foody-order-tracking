import { RefreshOutlined } from '@mui/icons-material';
import { Box, Button, TableCell, TableRow, Typography } from '@mui/material';

export const RowError = ({ handleRefresh }: { handleRefresh: () => void }) => {
  return (
    <TableRow>
      <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 2,
          }}>
          <Typography color="error.main" sx={{ mb: 1 }}>
            Erro ao carregar pedidos
          </Typography>
          <Button
            variant="outlined"
            startIcon={<RefreshOutlined />}
            onClick={handleRefresh}>
            Tentar novamente
          </Button>
        </Box>
      </TableCell>
    </TableRow>
  );
};
