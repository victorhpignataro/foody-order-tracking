import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import { renderWithProviders } from '@/test/test-utils';
import { LoginPage } from '@/pages/login';
import { Route, Routes } from 'react-router';

describe('LoginPage', () => {
  it('mostra o erro quando o login falha', async () => {
    const user = userEvent.setup();
    const login = vi
      .fn()
      .mockRejectedValueOnce(new Error('Credenciais inválidas'));

    renderWithProviders(<LoginPage />, { auth: { login } });

    await user.type(screen.getByLabelText(/e-mail/i), 'a@b.com');
    await user.type(screen.getByLabelText(/^senha/i), '123456');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    await waitFor(() =>
      expect(login).toHaveBeenCalledWith('a@b.com', '123456'),
    );
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Credenciais inválidas',
    );
  });

  it('navega para /home no sucesso', async () => {
    const user = userEvent.setup();
    const login = vi.fn().mockResolvedValueOnce(undefined);

    renderWithProviders(
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/home" element={<div>home</div>} />
      </Routes>,
      { auth: { login } },
    );
    await user.type(screen.getByLabelText(/e-mail/i), 'victor@email.com');
    await user.type(screen.getByLabelText(/^senha/i), 'senha');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    await waitFor(() =>
      expect(login).toHaveBeenCalledWith('victor@email.com', 'senha'),
    );
    expect(await screen.findByText('home')).toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Entrar' }),
    ).not.toBeInTheDocument();
  });

  it('alterna para cadastro e mostra o campo de nome', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.click(screen.getByRole('tab', { name: /cadastrar/i }));

    expect(screen.getByLabelText(/nome completo/i)).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: 'Criar conta' }),
    ).toBeInTheDocument();
  });
});
