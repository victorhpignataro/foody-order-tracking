import type { ReactNode } from 'react';
import { Box, Button, Paper, Typography } from '@mui/material';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutlineOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import RefreshIcon from '@mui/icons-material/Refresh';
import SearchOffIcon from '@mui/icons-material/SearchOff';
import { isRouteErrorResponse, useNavigate, useRouteError } from 'react-router';

interface ErrorContent {
  code: string;
  title: string;
  description: string;
  icon: ReactNode;
  canRetry: boolean;
}

function getContent(error: unknown): ErrorContent {
  const status =
    error === undefined
      ? 404
      : isRouteErrorResponse(error)
        ? error.status
        : 500;

  if (status === 404) {
    return {
      code: '404',
      title: 'Página não encontrada',
      description: 'O endereço que você acessou não existe ou foi movido.',
      icon: <SearchOffIcon fontSize="large" />,
      canRetry: false,
    };
  }

  if (status === 401 || status === 403) {
    return {
      code: String(status),
      title: 'Acesso negado',
      description:
        'Você não tem permissão para ver esta página. Entre novamente e tente de novo.',
      icon: <LockOutlinedIcon fontSize="large" />,
      canRetry: false,
    };
  }

  return {
    code: String(status),
    title: 'Algo deu errado',
    description:
      'Tivemos um problema inesperado. Tente novamente em instantes.',
    icon: <ErrorOutlineIcon fontSize="large" />,
    canRetry: true,
  };
}

export const ErrorPage = () => {
  const error = useRouteError();
  const navigate = useNavigate();
  const content = getContent(error);

  const debugMessage =
    import.meta.env.DEV && error instanceof Error ? error.message : undefined;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
        bgcolor: 'background.default',
      }}>
      <Paper
        elevation={0}
        sx={{
          width: '100%',
          maxWidth: 460,
          p: 4,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          textAlign: 'center',
          borderRadius: 2,
          border: '1px solid #E2E8F0',
          boxShadow:
            '0 4px 24px rgba(11, 28, 48, 0.08), 0 1px 3px rgba(11, 28, 48, 0.04)',
        }}>
        <Box
          sx={{
            width: 56,
            height: 56,
            mb: 2,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: 2,
            bgcolor: 'primary.main',
            color: 'primary.contrastText',
            boxShadow: '0 6px 16px rgba(211, 47, 47, 0.25)',
          }}>
          {content.icon}
        </Box>

        <Typography
          sx={{
            fontSize: 48,
            lineHeight: '56px',
            fontWeight: 700,
            letterSpacing: '-0.02em',
            color: 'primary.main',
            fontVariantNumeric: 'tabular-nums',
          }}>
          {content.code}
        </Typography>

        <Typography component="h1" variant="h5" sx={{ mt: 1, fontWeight: 600 }}>
          {content.title}
        </Typography>
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ mt: 1, mb: 3 }}>
          {content.description}
        </Typography>

        {debugMessage && (
          <Box
            component="code"
            sx={{
              width: '100%',
              mb: 3,
              p: 1.5,
              borderRadius: 1,
              bgcolor: '#FEE2E2',
              color: '#93000A',
              fontSize: 12,
              textAlign: 'left',
              wordBreak: 'break-word',
            }}>
            {debugMessage}
          </Box>
        )}

        <Box
          sx={{
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
          }}>
          <Button
            fullWidth
            size="large"
            variant="contained"
            onClick={() => navigate('/home')}
            sx={{
              py: 1.25,
              fontWeight: 700,
              boxShadow: '0 6px 16px rgba(211, 47, 47, 0.2)',
            }}>
            Ir para o início
          </Button>
          {content.canRetry && (
            <Button
              fullWidth
              size="large"
              variant="outlined"
              color="inherit"
              startIcon={<RefreshIcon />}
              onClick={() => window.location.reload()}
              sx={{ py: 1.25, borderColor: '#CBD5E1', color: '#334155' }}>
              Tentar novamente
            </Button>
          )}
        </Box>
      </Paper>
    </Box>
  );
};
