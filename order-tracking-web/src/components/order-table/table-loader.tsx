import { Box, CircularProgress, Typography } from '@mui/material';

export const TableLoader = ({ isLoading }: { isLoading: boolean }) => {
  return (
    <Box
      sx={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'rgba(255, 255, 255, 0.85)',
        backdropFilter: 'blur(2px)',
        zIndex: 10,
      }}>
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
        }}>
        <CircularProgress size={48} color="primary" />
        {isLoading ? (
          <Typography variant="body2" color="text.secondary">
            Carregando pedidos...
          </Typography>
        ) : (
          <Typography variant="body2" color="error.main">
            Falha ao carregar
          </Typography>
        )}
      </Box>
    </Box>
  );
};
