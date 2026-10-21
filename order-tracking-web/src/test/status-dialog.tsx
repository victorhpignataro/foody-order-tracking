import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test/test-utils';
import { ordersApi } from '@/api/order';
import {
  NEXT_STATUS,
  ORDER_STATUSES,
  STATUS_META,
  type Order,
} from '@/models/order';
import { StatusDialog } from '@components/status-dialog';

const { toastMessage } = vi.hoisted(() => ({ toastMessage: vi.fn() }));

vi.mock('@/api/order', () => ({
  ordersApi: { list: vi.fn(), create: vi.fn(), changeStatus: vi.fn() },
}));

vi.mock('@/context/snackbar-context/context', () => ({
  useSnackbar: () => ({ toastMessage }),
}));

const currentStatus = ORDER_STATUSES.find((s) => NEXT_STATUS[s]?.length > 0)!;
const allowedStatuses = NEXT_STATUS[currentStatus];
const blockedStatuses = ORDER_STATUSES.filter(
  (s) => s !== currentStatus && !allowedStatuses.includes(s),
);

const order = {
  id: 7,
  client: 'Maria Silva',
  address: 'Rua A, 100',
  status: currentStatus,
  items: [],
} as unknown as Order;

const radio = (status: (typeof ORDER_STATUSES)[number]) =>
  screen.getByRole('radio', { name: STATUS_META[status].label });

const setup = (o: Order | null = order) => {
  const user = userEvent.setup();
  const onClose = vi.fn();
  renderWithProviders(<StatusDialog order={o} onClose={onClose} />);
  return { user, onClose };
};

describe('StatusDialog', () => {
  beforeEach(() => {
    vi.mocked(ordersApi.changeStatus).mockResolvedValue({} as never);
  });

  it('não renderiza quando não há pedido', () => {
    setup(null);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('mostra o título com o número do pedido', () => {
    setup();
    expect(
      screen.getByRole('dialog', { name: /alterar status do pedido #7/i }),
    ).toBeInTheDocument();
  });

  it('habilita só o status atual e as transições permitidas', () => {
    setup();

    expect(radio(currentStatus)).toBeEnabled();
    allowedStatuses.forEach((s) => expect(radio(s)).toBeEnabled());
    blockedStatuses.forEach((s) => expect(radio(s)).toBeDisabled());
  });

  it('mantém Salvar desabilitado sem seleção ou com o status atual', async () => {
    const { user } = setup();
    const save = screen.getByRole('button', { name: /salvar status/i });
    expect(save).toBeDisabled();

    await user.click(radio(currentStatus));
    expect(save).toBeDisabled();

    await user.click(radio(allowedStatuses[0]));
    expect(save).toBeEnabled();
  });

  it('altera o status, mostra o toast e fecha', async () => {
    const { user, onClose } = setup();
    const next = allowedStatuses[0];

    await user.click(radio(next));
    await user.click(screen.getByRole('button', { name: /salvar status/i }));

    await waitFor(() =>
      expect(ordersApi.changeStatus).toHaveBeenCalledWith(7, next),
    );
    await waitFor(() =>
      expect(toastMessage).toHaveBeenCalledWith(
        'Status do pedido #7 alterado com sucesso',
      ),
    );
    expect(onClose).toHaveBeenCalled();
  });

  it('avisa o erro e fecha quando a API falha', async () => {
    vi.mocked(ordersApi.changeStatus).mockRejectedValueOnce(new Error('500'));
    const { user, onClose } = setup();

    await user.click(radio(allowedStatuses[0]));
    await user.click(screen.getByRole('button', { name: /salvar status/i }));

    await waitFor(() =>
      expect(toastMessage).toHaveBeenCalledWith(
        'Não foi possível atualizar o status.',
        'error',
      ),
    );
    expect(onClose).toHaveBeenCalled();
  });

  it('fecha ao clicar em Cancelar sem chamar a API', async () => {
    const { user, onClose } = setup();

    await user.click(screen.getByRole('button', { name: /cancelar/i }));

    expect(onClose).toHaveBeenCalled();
    expect(ordersApi.changeStatus).not.toHaveBeenCalled();
  });
});
