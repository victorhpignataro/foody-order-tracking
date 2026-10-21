import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test/test-utils';
import { ordersApi } from '@/api/order';
import { OrderDrawer } from '@components/order-drawer';

const { toastMessage } = vi.hoisted(() => ({ toastMessage: vi.fn() }));

vi.mock('@/api/order', () => ({
  ordersApi: { list: vi.fn(), create: vi.fn(), changeStatus: vi.fn() },
}));

vi.mock('@/context/snackbar-context/context', () => ({
  useSnackbar: () => ({ toastMessage }),
}));

const setup = (drawerOpen = true) => {
  const user = userEvent.setup();
  const onCloseDrawer = vi.fn();
  renderWithProviders(
    <OrderDrawer drawerOpen={drawerOpen} onCloseDrawer={onCloseDrawer} />,
  );
  return { user, onCloseDrawer };
};

const fillValidOrder = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(screen.getByLabelText(/^cliente/i), 'Maria Silva');
  await user.type(screen.getByLabelText(/endereço de entrega/i), 'Rua A, 100');
  await user.type(screen.getByLabelText(/nome do item/i), 'Pizza');
  await user.type(screen.getByLabelText(/qtd/i), '2');
  await user.type(screen.getByLabelText(/preço unitário/i), '1500');
};

describe('DrawerOrder', () => {
  beforeEach(() => {
    vi.mocked(ordersApi.create).mockResolvedValue({} as never);
  });

  it('não renderiza o formulário quando fechado', () => {
    setup(false);
    expect(screen.queryByText('Novo pedido')).not.toBeInTheDocument();
  });

  it('mostra os erros de campos obrigatórios e não chama a API', async () => {
    const { user } = setup();

    await user.click(screen.getByRole('button', { name: /criar pedido/i }));

    expect(
      await screen.findByText('Cliente é obrigatório'),
    ).toBeInTheDocument();
    expect(screen.getByText('Endereço é obrigatório')).toBeInTheDocument();
    expect(screen.getAllByText('Obrigatório')).toHaveLength(3); // nome, qtd, preço
    expect(ordersApi.create).not.toHaveBeenCalled();
  });

  it('valida o limite de 256 caracteres do cliente', async () => {
    const { user } = setup();

    await user.click(screen.getByLabelText(/^cliente/i));
    await user.paste('a'.repeat(257)); // paste evita digitar 257 vezes
    await user.click(screen.getByRole('button', { name: /criar pedido/i }));

    expect(
      await screen.findByText(/no máximo 256 caracteres/i),
    ).toBeInTheDocument();
    expect(ordersApi.create).not.toHaveBeenCalled();
  });

  it('formata o preço digitado como moeda em reais', async () => {
    const { user } = setup();

    const priceInput = screen.getByLabelText(/preço unitário/i);
    await user.type(priceInput, '1550');

    expect((priceInput as HTMLInputElement).value.replace(/\u00a0/g, ' ')).toBe(
      'R$ 15,50',
    );
  });

  it('rejeita quantidade menor que 1', async () => {
    const { user } = setup();

    await user.type(screen.getByLabelText(/qtd/i), '0');
    await user.click(screen.getByRole('button', { name: /criar pedido/i }));

    expect(await screen.findByText('Mín. 1')).toBeInTheDocument();
  });

  it('envia o pedido, mostra o toast e fecha o drawer', async () => {
    const { user, onCloseDrawer } = setup();

    await fillValidOrder(user);
    await user.click(screen.getByRole('button', { name: /criar pedido/i }));

    await waitFor(() =>
      expect(ordersApi.create).toHaveBeenCalledWith({
        client: 'Maria Silva',
        address: 'Rua A, 100',
        items: [{ name: 'Pizza', quantity: 2, unitPrice: 15 }],
      }),
    );
    await waitFor(() =>
      expect(toastMessage).toHaveBeenCalledWith('Pedido criado com sucesso'),
    );
    expect(onCloseDrawer).toHaveBeenCalled();
  });

  it('adiciona um segundo item', async () => {
    const { user } = setup();

    await user.click(screen.getByRole('button', { name: /adicionar item/i }));

    expect(screen.getAllByLabelText(/nome do item/i)).toHaveLength(2);
  });

  it('não deixa remover o único item, mas remove quando há mais de um', async () => {
    const { user } = setup();
    expect(
      screen.getByRole('button', { name: /remover item/i }),
    ).toBeDisabled();

    await user.click(screen.getByRole('button', { name: /adicionar item/i }));
    const removeButtons = screen.getAllByRole('button', {
      name: /remover item/i,
    });
    expect(removeButtons[0]).toBeEnabled();

    await user.click(removeButtons[0]);
    expect(screen.getAllByLabelText(/nome do item/i)).toHaveLength(1);
  });

  it('fecha ao clicar em Cancelar', async () => {
    const { user, onCloseDrawer } = setup();

    await user.click(screen.getByRole('button', { name: /cancelar/i }));

    expect(onCloseDrawer).toHaveBeenCalledTimes(1);
  });
});
