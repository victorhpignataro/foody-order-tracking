export const ORDER_STATUSES = [
  'RECEIVED',
  'IN_PREPARATION',
  'OUT_FOR_DELIVERY',
  'DELIVERED',
  'CANCELED',
] as const;

export type OrderStatus = (typeof ORDER_STATUSES)[number];

export interface OrderItem {
  id: number;
  name: string;
  quantity: number;
  order: number;
  unitPrice: number;
}

export interface Order {
  id: number;
  client: string;
  items: OrderItem[];
  address: string;
  status: OrderStatus;
  createdAt: string;
}

interface StatusMeta {
  label: string;
  hint: string;
  color: string;
  bg: string;
}

export const STATUS_META: Record<OrderStatus, StatusMeta> = {
  RECEIVED: {
    label: 'Recebido',
    hint: 'Fila de espera',
    color: '#0288D1',
    bg: '#E0F2FE',
  },
  IN_PREPARATION: {
    label: 'Em preparo',
    hint: 'Na cozinha',
    color: '#ED6C02',
    bg: '#FFF7ED',
  },
  OUT_FOR_DELIVERY: {
    label: 'Saiu para entrega',
    hint: 'Em rota',
    color: '#7C3AED',
    bg: '#F3E8FF',
  },
  DELIVERED: {
    label: 'Entregue',
    hint: 'Finalizados',
    color: '#2E7D32',
    bg: '#DCFCE7',
  },
  CANCELED: {
    label: 'Cancelado',
    hint: 'Cancelados',
    color: '#D32F2F',
    bg: '#FEE2E2',
  },
};

export const NEXT_STATUS: Record<OrderStatus, OrderStatus[]> = {
  RECEIVED: ['IN_PREPARATION', 'CANCELED'],
  IN_PREPARATION: ['OUT_FOR_DELIVERY', 'CANCELED'],
  OUT_FOR_DELIVERY: ['DELIVERED', 'CANCELED'],
  DELIVERED: [],
  CANCELED: [],
};
