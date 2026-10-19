import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    primary: { main: '#D32F2F', contrastText: '#FFFFFF' },
    secondary: { main: '#FF5722' },
    background: { default: '#F8FAFC', paper: '#FFFFFF' },
    text: { primary: '#0F172A', secondary: '#64748B' },
    divider: '#E2E8F0',
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: 'Sen,sans-serif',
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiButton: { defaultProps: { disableElevation: true } },
    MuiPaper: { styleOverrides: { root: { backgroundImage: 'none' } } },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-notchedOutline': { borderColor: '#CBD5E1' },
          '&.Mui-focused': { boxShadow: '0 0 0 3px rgba(211, 47, 47, 0.15)' },
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          '&.MuiInputLabel-outlined.MuiInputLabel-shrink': {
            backgroundColor: '#FFFFFF',
            paddingInline: '8px',
            marginLeft: '-6px',
          },
        },
      },
    },
  },
});
