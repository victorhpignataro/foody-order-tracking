import React from 'react';
import { Box, Button, Container, createTheme, CssBaseline, ThemeProvider, Typography } from '@mui/material'
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#90caf9',
    },
  },
});
export const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline /> 
      
      <Container maxWidth="sm">
        <Box sx={{ my: 4, textAlign: 'center' }}>
          <Typography variant="h4">
            TODO
          </Typography>
          <Button variant="contained" color="primary">
            Button
          </Button>
        </Box>
      </Container>
    </ThemeProvider>
  );
}

export default App
