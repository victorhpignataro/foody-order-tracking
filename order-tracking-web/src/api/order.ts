import { api } from '@/lib/axios';
import type { Order } from '@models/order';

export const ordersApi = {
  list: () => api.get<Order[]>('/api/order'),
};
