import { type ReactElement } from 'react';
import { render, type RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { AuthContext } from '@/context/auth-context/context';

const defaultAuth = {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn(),
  logout: vi.fn(),
};

export function renderWithProviders(
  ui: ReactElement,
  {
    route = '/',
    auth = {},
    ...options
  }: { route?: string; auth?: Partial<typeof defaultAuth> } & Omit<
    RenderOptions,
    'wrapper'
  > = {},
) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  return render(ui, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>
        <AuthContext.Provider value={{ ...defaultAuth, ...auth } as never}>
          <ThemeProvider theme={createTheme()}>
            <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
          </ThemeProvider>
        </AuthContext.Provider>
      </QueryClientProvider>
    ),
    ...options,
  });
}

export { render, screen, fireEvent } from '@testing-library/react';
