import { AppHeader } from '@components/main-navbar';
import { AuthContext } from '@context/auth-context/context';
import { Box } from '@mui/material';
import { useContext } from 'react';
import { Navigate, Outlet } from 'react-router';

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
