import {
  AddOutlined,
  RefreshOutlined,
  SearchOutlined,
} from '@mui/icons-material';
import { Box, Button, InputAdornment, Paper, TextField } from '@mui/material';
import type { ChangeEvent } from 'react';

type Props = {
  handleRefresh: () => void;
  isLoading: boolean;
  search: string;
  onSearchChange: (event: ChangeEvent<HTMLInputElement>) => void;
  onClickNewOrder: () => void;
};

export const TableHeader = ({
  handleRefresh,
  isLoading,
  search,
  onSearchChange,
  onClickNewOrder,
}: Props) => {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        display: 'flex',
        flexWrap: 'wrap',
        gap: 2,
      }}>
      <Box sx={{ flex: 1, display: 'flex', gap: 2 }}>
        <TextField
          size="small"
          placeholder="Buscar por número ou cliente"
          value={search}
          onChange={onSearchChange}
          sx={{ flex: 1, minWidth: 200, maxWidth: 700 }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchOutlined fontSize="small" />
                </InputAdornment>
              ),
            },
          }}
        />
      </Box>
      <Box
        sx={{
          display: 'flex',
          gap: 1,
          justifyContent: 'flex-end',
          width: { xs: '100%', sm: '100%', md: 'unset' },
        }}>
        <Button
          variant="outlined"
          color="inherit"
          startIcon={<RefreshOutlined />}
          onClick={handleRefresh}
          disabled={isLoading}
          sx={{ borderColor: '#CBD5E1', color: '#334155' }}>
          Atualizar
        </Button>
        <Button
          variant="contained"
          startIcon={<AddOutlined />}
          onClick={onClickNewOrder}>
          Novo pedido
        </Button>
      </Box>
    </Paper>
  );
};
