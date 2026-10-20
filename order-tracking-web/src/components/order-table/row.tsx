import { StatusChip } from '@components/status-chip';
import {
  NEXT_STATUS,
  STATUS_META,
  type Order,
  type OrderStatus,
} from '@models/order';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import {
  Box,
  Button,
  Collapse,
  IconButton,
  TableCell,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import { useState } from 'react';

interface Props {
  order: Order;
  onEditStatus: (order: Order) => void;
  onQuickChange: (id: number, status: OrderStatus) => void;
}

function timeAgo(iso: string) {
  const min = Math.max(
    0,
    Math.round((Date.now() - new Date(iso).getTime()) / 60000),
  );
  if (min < 1) return 'agora';
  if (min < 60) return `há ${min} min`;
  const hours = Math.floor(min / 60);
  if (hours < 24) return `há ${hours} h`;
  return `há ${Math.floor(hours / 24)} d`;
}

export const OrderRow = ({ order, onEditStatus, onQuickChange }: Props) => {
  const [open, setOpen] = useState(false);
  const next = NEXT_STATUS[order.status];
  const created = new Date(order.createdAt);

  return (
    <>
      <TableRow
        hover
        onClick={() => setOpen((v) => !v)}
        sx={{
          cursor: 'pointer',
          '& > td': { borderBottom: open ? 0 : undefined },
        }}>
        <TableCell
          sx={{
            fontWeight: 600,
            color: 'primary.main',
            fontVariantNumeric: 'tabular-nums',
          }}>
          #{order.id}
        </TableCell>
        <TableCell sx={{ whiteSpace: 'nowrap' }}>
          <Typography variant="subtitle2">
            {created.toLocaleTimeString('pt-BR', {
              hour: '2-digit',
              minute: '2-digit',
            })}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {created.toLocaleDateString('pt-BR', {
              day: '2-digit',
              month: '2-digit',
            })}
            , {timeAgo(order.createdAt)}
          </Typography>
        </TableCell>
        <TableCell sx={{ fontWeight: 600 }}>{order.client}</TableCell>

        <TableCell sx={{ maxWidth: 240 }}>
          <Typography variant="body2" noWrap title={order.address}>
            {order.address}
          </Typography>
        </TableCell>
        <TableCell>
          <StatusChip status={order.status} />
        </TableCell>
        <TableCell
          align="center"
          sx={{ whiteSpace: 'nowrap' }}
          onClick={(e) => e.stopPropagation()}>
          <Tooltip
            title={next?.length ? 'Alterar status' : 'Pedido finalizado'}>
            <span>
              <IconButton
                size="small"
                disabled={!next?.length}
                onClick={() => onEditStatus(order)}>
                <SwapHorizIcon fontSize="small" />
              </IconButton>
            </span>
          </Tooltip>
          <IconButton
            size="small"
            aria-label={open ? 'Recolher detalhes' : 'Ver detalhes'}
            onClick={() => setOpen((v) => !v)}
            sx={{
              transform: open ? 'rotate(180deg)' : 'none',
              transition: 'transform 0.2s',
            }}>
            <ExpandMoreIcon fontSize="small" />
          </IconButton>
        </TableCell>
      </TableRow>

      <TableRow>
        <TableCell colSpan={7} sx={{ py: 0, border: open ? undefined : 0 }}>
          <Collapse in={open} unmountOnExit>
            <Box
              sx={{
                my: 2,
                p: 2,
                display: 'grid',
                gap: 3,
                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                bgcolor: 'background.default',
                borderRadius: 1,
              }}>
              <Box>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>
                  Itens
                </Typography>
                {order.itens?.map((item, idx) => (
                  <Typography key={idx} variant="body2" sx={{ py: 0.5 }}>
                    {item.quantity}x {item.name}
                  </Typography>
                ))}
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ display: 'block', mt: 1 }}>
                  Entrega: {order.address}
                </Typography>
              </Box>
              <Box>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>
                  Mover para
                </Typography>
                {next?.length ? (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {next?.map((status) => {
                      const meta = STATUS_META[status];
                      const isCancel = status === 'CANCELED';
                      return (
                        <Button
                          key={status}
                          size="small"
                          variant={isCancel ? 'outlined' : 'contained'}
                          onClick={() => onQuickChange(order.id, status)}
                          sx={
                            isCancel
                              ? { color: meta.color, borderColor: meta.color }
                              : {
                                  bgcolor: meta.color,
                                  '&:hover': {
                                    bgcolor: meta.color,
                                    opacity: 0.9,
                                  },
                                }
                          }>
                          {isCancel ? 'Cancelar pedido' : meta?.label}
                        </Button>
                      );
                    })}
                  </Box>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Este pedido está finalizado e não aceita novas mudanças.
                  </Typography>
                )}
              </Box>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
};
