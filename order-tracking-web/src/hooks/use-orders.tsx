import { useCallback, useEffect, useState } from 'react';
import { ordersApi } from '@/api/order';
import type { Order, OrderStatus } from '../models/order';

export function useOrders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await ordersApi.list();
      setOrders([...data].sort((a, b) => b.criadoEm.localeCompare(a.criadoEm)));
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Erro ao carregar pedidos.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  const changeStatus = useCallback(
    async (id: number, status: OrderStatus) => {},
    [],
  );

  return { orders, loading, error, reload, changeStatus };
}
