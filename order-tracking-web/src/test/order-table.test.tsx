import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test/test-utils';
import { ordersApi } from '@/api/order';
import type { Order } from '@/models/order';
import type { Page } from '@/models/pageable';
import { OrderTable } from '@components/order-table';

const { toastMessage } = vi.hoisted(() => ({ toastMessage: vi.fn() }));

vi.mock('@/api/order', () => ({
  ordersApi: { list: vi.fn(), create: vi.fn(), changeStatus: vi.fn() },
}));

vi.mock('@/context/snackbar-context/context', () => ({
  useSnackbar: () => ({ toastMessage }),
}));

const makeOrder = (overrides: Partial<Order> = {}) =>
  ({
    id: 1,
    client: 'Maria Silva',
    address: 'Rua das Flores, 100',
    status: 'PENDING',
    items: [],
    ...overrides,
  }) as unknown as Order;

const makePage = (content: Order[], totalElements = content.length) =>
  ({ content, totalElements }) as unknown as Page<Order>;

describe('OrderTable', () => {
  beforeEach(() => {
    vi.mocked(ordersApi.list).mockResolvedValue(makePage([]));
  });

  it('mostra o loader enquanto carrega', () => {
    vi.mocked(ordersApi.list).mockReturnValue(new Promise(() => {}));

    renderWithProviders(<OrderTable />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument(); // ajuste conforme o SpinnerLoader
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument();
  });

  it('renderiza os pedidos retornados pela API', async () => {
    vi.mocked(ordersApi.list).mockResolvedValue(
      makePage([
        makeOrder({ id: 1, client: 'Maria Silva' }),
        makeOrder({ id: 2, client: 'João Souza' }),
      ]),
    );

    renderWithProviders(<OrderTable />);

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument();
    expect(screen.getByText('João Souza')).toBeInTheDocument();
    expect(ordersApi.list).toHaveBeenCalledWith('', 0, 10);
  });

  it('mostra o estado vazio quando não há pedidos', async () => {
    renderWithProviders(<OrderTable />);

    // troque pelo texto real do RowEmpty
    expect(await screen.findByText(/nenhum pedido/i)).toBeInTheDocument();
  });

  it('mostra o estado de erro quando a API falha', async () => {
    vi.mocked(ordersApi.list).mockRejectedValue(new Error('500'));

    renderWithProviders(<OrderTable />);

    // troque pelo texto real do RowError
    expect(
      await screen.findByText(/erro|não foi possível/i),
    ).toBeInTheDocument();
  });

  it('busca na API com o termo digitado (após o debounce)', async () => {
    const user = userEvent.setup();
    renderWithProviders(<OrderTable />);
    await waitFor(() => expect(ordersApi.list).toHaveBeenCalled());

    await user.type(screen.getByRole('textbox'), 'maria'); // ajuste a query do campo de busca

    await waitFor(
      () => expect(ordersApi.list).toHaveBeenLastCalledWith('maria', 0, 10),
      { timeout: 2000 }, // maior que o delay do useDebounce
    );
  });

  it('pagina para a próxima página', async () => {
    const user = userEvent.setup();
    vi.mocked(ordersApi.list).mockResolvedValue(
      makePage([makeOrder()], 30), // total > rowsPerPage habilita o botão
    );

    renderWithProviders(<OrderTable />);
    await screen.findByText('Maria Silva');

    await user.click(screen.getByRole('button', { name: /next page/i }));

    await waitFor(() =>
      expect(ordersApi.list).toHaveBeenLastCalledWith('', 1, 10),
    );
  });

  it('troca linhas por página e volta para a primeira página', async () => {
    const user = userEvent.setup();
    vi.mocked(ordersApi.list).mockResolvedValue(makePage([makeOrder()], 100));

    renderWithProviders(<OrderTable />);
    await screen.findByText('Maria Silva');

    await user.click(screen.getByRole('combobox'));
    await user.click(await screen.findByRole('option', { name: '25' }));

    await waitFor(() =>
      expect(ordersApi.list).toHaveBeenLastCalledWith('', 0, 25),
    );
  });

  it('exibe os itens com preço unitário e calcula o total do pedido', async () => {
    const user = userEvent.setup();
    vi.mocked(ordersApi.list).mockResolvedValue(
      makePage([
        makeOrder({
          id: 7,
          client: 'Maria Silva',
          status: 'RECEIVED',
          items: [
            { id: 1, name: 'Pizza', quantity: 2, unitPrice: 25, order: 0 },
            { id: 2, name: 'Coca', quantity: 3, unitPrice: 6.5, order: 1 },
          ],
        }),
      ]),
    );

    renderWithProviders(<OrderTable />);

    await screen.findByText('Maria Silva');
    await user.click(screen.getByText('Maria Silva'));

    // itens do pedido com o preço unitário de cada um
    expect(await screen.findByText('Pizza')).toBeInTheDocument();
    expect(screen.getByText('Coca')).toBeInTheDocument();
    expect(screen.getByText(/R\$\s*25,00/)).toBeInTheDocument();
    expect(screen.getByText(/R\$\s*6,50/)).toBeInTheDocument();

    // total: 2 x 25,00 + 3 x 6,50 = 69,50
    expect(screen.getByText(/R\$\s*69,50/)).toBeInTheDocument();
  });
});
