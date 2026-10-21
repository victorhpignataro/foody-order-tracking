import LogoIconAlt from '@assets/foody-logo-alt.png';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import PersonOutlineIcon from '@mui/icons-material/PersonOutlineOutlined';
import VisibilityOffOutlinedIcon from '@mui/icons-material/VisibilityOffOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  IconButton,
  InputAdornment,
  Paper,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import React, { useState } from 'react';

import { useNavigate } from 'react-router';
import { useAuth } from '../context/auth-context/context';

const segmentedTabsSx = {
  width: '100%',
  mb: 3,
  p: 0.5,
  minHeight: 0,
  bgcolor: '#F1F5F9',
  border: '1px solid #E2E8F0',
  borderRadius: 1,
  '& .MuiTabs-indicator': { display: 'none' },
  '& .MuiTab-root': {
    minHeight: 0,
    py: 1,
    borderRadius: 0.75,
    color: 'text.secondary',
    fontSize: 13,
  },
  '& .MuiTab-root.Mui-selected': {
    bgcolor: 'background.paper',
    color: 'primary.main',
    boxShadow: '0 1px 2px rgba(15, 23, 42, 0.08)',
  },
};

export const LoginPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const isLogin = tabValue === 0;

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isLogin) {
        await login(email, password);
        navigate('/home');
      } else {
        await register(name, email, password);
        setSuccess(
          'Cadastro realizado com sucesso! Faça login para continuar.',
        );
        setName('');
        setPassword('');
        setTabValue(0);
      }
    } catch (err: unknown) {
      setError(
        err instanceof Error ? err.message : 'Ocorreu um erro inesperado.',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        bgcolor: 'background.default',
      }}>
      <Box
        component="main"
        sx={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 2,
          pt: 0,
        }}>
        <Paper
          elevation={0}
          sx={{
            width: '100%',
            maxWidth: 460,
            p: 4,
            pt: 2,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            borderRadius: 2,
            border: '1px solid #E2E8F0',
          }}>
          <Box
            sx={{
              padding: 1,
              mb: 2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: 1,
              bgcolor: 'primary.main',
              color: 'primary.contrastText',
            }}>
            <img src={LogoIconAlt} width={36} height={36} alt="Logo"></img>
          </Box>

          <Typography
            component="h1"
            variant="h6"
            sx={{ fontWeight: 700, letterSpacing: '-0.01em' }}>
            FoodyDelivery
          </Typography>
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{ mt: 0.5, mb: 3, textAlign: 'center' }}>
            {isLogin
              ? 'Entre para acompanhar os pedidos em andamento.'
              : 'Crie sua conta para gerenciar os pedidos.'}
          </Typography>

          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            variant="fullWidth"
            sx={segmentedTabsSx}>
            <Tab label="Entrar" />
            <Tab label="Cadastrar" />
          </Tabs>

          {error && (
            <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
              {error}
            </Alert>
          )}

          {success && (
            <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
              {success}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
            {!isLogin && (
              <TextField
                margin="normal"
                required
                fullWidth
                id="name"
                label="Nome completo"
                name="name"
                autoComplete="name"
                autoFocus
                value={name}
                onChange={(e) => setName(e.target.value)}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <PersonOutlineIcon color="action" />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            )}

            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="E-mail"
              name="email"
              type="email"
              autoComplete="email"
              autoFocus={isLogin}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <EmailOutlinedIcon color="action" />
                    </InputAdornment>
                  ),
                },
              }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Senha"
              type={showPassword ? 'text' : 'password'}
              id="password"
              autoComplete={isLogin ? 'current-password' : 'new-password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockOutlinedIcon color="action" />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        edge="end"
                        aria-label={
                          showPassword ? 'Ocultar senha' : 'Mostrar senha'
                        }
                        onClick={() => setShowPassword((v) => !v)}
                        onMouseDown={(e) => e.preventDefault()}>
                        {showPassword ? (
                          <VisibilityOffOutlinedIcon />
                        ) : (
                          <VisibilityOutlinedIcon />
                        )}
                      </IconButton>
                    </InputAdornment>
                  ),
                },
              }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                mt: 3,
                py: 1.5,
                fontWeight: 700,
                boxShadow: '0 6px 16px rgba(211, 47, 47, 0.2)',
              }}>
              {loading ? (
                <CircularProgress size={24} color="inherit" />
              ) : isLogin ? (
                'Entrar'
              ) : (
                'Criar conta'
              )}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Box>
  );
};

export default LoginPage;
