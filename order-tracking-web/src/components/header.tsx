import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import LogoIcon from '@assets/foody-logo.png';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router';
import { useAuth } from '../context/auth-context/context';

export const AppHeader = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <AppBar
      position="sticky"
      color="inherit"
      elevation={0}
      sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
      <Toolbar sx={{ gap: 1.5 }} variant="dense">
        <Box
          sx={{
            width: 28,
            height: 28,
          }}>
          <img src={LogoIcon} height={28} width={28}></img>
        </Box>
        <Typography
          variant="h6"
          sx={{ fontWeight: 700, color: 'primary.main', flexGrow: 1 }}>
          FoodyDelivery
        </Typography>
        <Button
          color="inherit"
          startIcon={<LogoutIcon />}
          onClick={handleLogout}>
          Sair
        </Button>
      </Toolbar>
    </AppBar>
  );
};
