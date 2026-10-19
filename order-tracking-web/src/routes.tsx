import { createBrowserRouter } from 'react-router';
import App from './App';

import { HomePage } from '@pages/home.tsx';
import { LoginPage } from '@pages/login.tsx';
import { AuthenticatedLayout } from './layouts/authenticated-layout.tsx';
import { ErrorPage } from './pages/error-page.tsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: '',
        element: <AuthenticatedLayout />,
        children: [
          {
            path: 'home',
            element: <HomePage />,
          },
        ],
      },
    ],
  },
]);
