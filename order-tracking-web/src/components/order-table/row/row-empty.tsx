import { AddOutlined } from '@mui/icons-material';
import { Box, Button, TableCell, TableRow, Typography } from '@mui/material';

type Props = {
  isTrulyEmpty: boolean;
  toggleOrderDrawer: (newOpen: boolean) => () => void;
};

export const RowEmpty: React.FC<Props> = ({
  isTrulyEmpty,
  toggleOrderDrawer,
}) => {
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
          <Typography color="text.secondary" sx={{ mb: 1 }}>
            {isTrulyEmpty
              ? 'Nenhum pedido cadastrado.'
              : 'Nenhum pedido encontrado'}
          </Typography>
          {isTrulyEmpty && (
            <Button
              variant="contained"
              startIcon={<AddOutlined />}
              onClick={toggleOrderDrawer(true)}>
              Criar primeiro pedido
            </Button>
          )}
        </Box>
      </TableCell>
    </TableRow>
  );
};
