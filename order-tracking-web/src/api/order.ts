import { api } from '@/lib/axios';
import { type Page } from '@/models/pageable';
import type { Order, OrderStatus } from '@models/order';

export const ordersApi = {
  list: async (debouncedSearchTerm: string, page = 0, size = 10) => {
    const response = await api.get<Page<Order>>('/api/order', {
      params: {
        search: debouncedSearchTerm || undefined,
        page,
        size,
      },
    });
    return response.data;
  },
  create: (data: Pick<Order, 'id' | 'address' | 'client' | 'itens'>) =>
    api.post<Order>('/api/order', data),
  changeStatus: async (orderId: Order['id'], newStatus: OrderStatus) => {
    const { data } = await api.patch<Order>(`/api/order/${orderId}/status`, {
      status: newStatus,
    });
    return data;
  },
};
