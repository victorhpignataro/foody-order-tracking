import { Box, CircularProgress, Typography } from '@mui/material';

type Props = {
  isLoading: boolean;
};
export const SpinnerLoader: React.FC<Props> = ({ isLoading }) => {
  if (!isLoading) return null;
  return (
    <Box
      sx={{
        position: 'absolute',
        inset: 0,
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
        <Typography variant="body2" color="text.secondary">
          Carregando...
        </Typography>
      </Box>
    </Box>
  );
};
