import { createBrowserRouter } from 'react-router';
import App from './App';
import { SpinnerLoader } from './components/spinner-loader/index.tsx';
import { ErrorPage } from './pages/error-page.tsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <ErrorPage />,
    HydrateFallback: () => <SpinnerLoader isLoading />,
    children: [
      {
        path: 'login',
        lazy: async () => {
          const { LoginPage } = await import('@/pages/login.tsx');
          return { Component: LoginPage };
        },
      },
      {
        path: '',
        lazy: async () => {
          const { AuthenticatedLayout } =
            await import('./layouts/authenticated-layout.tsx');
          return { Component: AuthenticatedLayout };
        },
        children: [
          {
            path: 'home',
            lazy: async () => {
              const { HomePage } = await import('@pages/home');
              return { Component: HomePage };
            },
          },
        ],
      },
    ],
  },
]);
