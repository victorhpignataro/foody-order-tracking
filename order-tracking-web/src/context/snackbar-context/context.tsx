import { createContext, useContext } from 'react';
import type { AlertColor } from '@mui/material';

interface SnackbarContextType {
  toastMessage: (message: string, severity?: AlertColor) => void;
}

export const SnackbarContext = createContext<SnackbarContextType | undefined>(
  undefined,
);

export const useSnackbar = () => {
  const context = useContext(SnackbarContext);
  if (!context)
    throw new Error('useSnackbar deve ser usado dentro de um SnackbarProvider');
  return context;
};
