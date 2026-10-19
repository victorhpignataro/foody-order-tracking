import { Box } from '@mui/material';
import { Navigate, Outlet } from 'react-router';
import { AppHeader } from '../components/header';
import { useContext } from 'react';
import { AuthContext } from '../context/auth-context/context';

export const AuthenticatedLayout = () => {
  const authContext = useContext(AuthContext);

  if (!authContext?.isAuthenticated) {
    return <Navigate to={'/login'}></Navigate>;
  }

  return (
    <Box>
      <AppHeader />
      <Outlet />
    </Box>
  );
};
