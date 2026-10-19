export const ORDER_STATUSES = [
  'RECEBIDO',
  'EM_PREPARO',
  'SAIU_PARA_ENTREGA',
  'ENTREGUE',
  'CANCELADO',
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
  itens: OrderItem[];
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
  RECEBIDO: {
    label: 'Recebido',
    hint: 'Fila de espera',
    color: '#0288D1',
    bg: '#E0F2FE',
  },
  EM_PREPARO: {
    label: 'Em preparo',
    hint: 'Na cozinha',
    color: '#ED6C02',
    bg: '#FFF7ED',
  },
  SAIU_PARA_ENTREGA: {
    label: 'Saiu para entrega',
    hint: 'Em rota',
    color: '#7C3AED',
    bg: '#F3E8FF',
  },
  ENTREGUE: {
    label: 'Entregue',
    hint: 'Finalizados',
    color: '#2E7D32',
    bg: '#DCFCE7',
  },
  CANCELADO: {
    label: 'Cancelado',
    hint: 'Cancelados',
    color: '#D32F2F',
    bg: '#FEE2E2',
  },
};

export const NEXT_STATUS: Record<OrderStatus, OrderStatus[]> = {
  RECEBIDO: ['EM_PREPARO', 'CANCELADO'],
  EM_PREPARO: ['SAIU_PARA_ENTREGA', 'CANCELADO'],
  SAIU_PARA_ENTREGA: ['ENTREGUE', 'CANCELADO'],
  ENTREGUE: [],
  CANCELADO: [],
};
