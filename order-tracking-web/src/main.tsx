import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router/dom';
import { router } from './routes';
import { AuthProvider } from './context/auth-context/provider';
import './index.css';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { theme } from './themes/default';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from './lib/react-query';
import { SnackbarProvider } from '@context/snackbar-context/provider';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <SnackbarProvider>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <CssBaseline />

            <RouterProvider router={router} />
          </ThemeProvider>
        </AuthProvider>
      </SnackbarProvider>
    </QueryClientProvider>
  </React.StrictMode>,
);
