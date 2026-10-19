import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router/dom';
import { router } from './routes';
import { AuthProvider } from './context/auth-context/provider';
import './index.css';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { theme } from './themes/default';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <ThemeProvider theme={theme}>
        <CssBaseline />

        <RouterProvider router={router} />
      </ThemeProvider>
    </AuthProvider>
  </React.StrictMode>,
);
