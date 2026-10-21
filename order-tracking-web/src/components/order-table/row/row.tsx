import { timeAgo } from '@/utils/formatters';
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
  Chip,
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

export const OrderRow = ({ order, onEditStatus, onQuickChange }: Props) => {
  const [open, setOpen] = useState(false);
  const next = NEXT_STATUS[order.status];
  const created = new Date(order.createdAt);

  const toggleOpen = () => setOpen((current) => !current);

  const formattedCreatedTime = created.toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  });

  const formattedDate = created.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
  });

  return (
    <>
      <TableRow
        hover
        onClick={toggleOpen}
        sx={{
          cursor: 'pointer',
          bgcolor: open ? '#fbfbfb' : 'unset',
          '> td': {
            borderBottom: '1px solid #eee',
          },
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
          <Typography variant="subtitle2">{formattedCreatedTime}</Typography>
          <Typography variant="caption" color="text.secondary">
            {formattedDate}, {timeAgo(order.createdAt)}
          </Typography>
        </TableCell>

        <TableCell sx={{ fontWeight: 600 }}>{order.client}</TableCell>

        <TableCell sx={{ maxWidth: 240 }}>
          <Typography variant="body2" noWrap title={order.address}>
            {order.address}
          </Typography>
        </TableCell>

        <TableCell width={270}>
          <StatusChip status={order.status} />
        </TableCell>

        <TableCell
          align="center"
          sx={{ whiteSpace: 'nowrap' }}
          onClick={(e) => e.stopPropagation()}>
          <Tooltip
            title={next?.length ? 'Alterar status' : 'Pedido finalizado'}>
            <IconButton
              size="small"
              disabled={!next?.length}
              onClick={() => onEditStatus(order)}>
              <SwapHorizIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <IconButton
            size="small"
            aria-label={open ? 'Recolher detalhes' : 'Ver detalhes'}
            onClick={toggleOpen}
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
                gridTemplateColumns: { xs: '1fr', md: '3fr 1fr' },
                bgcolor: '#fbfbfb',
                borderRadius: 1,
              }}>
              <Box>
                <Typography
                  variant="subtitle2"
                  sx={{
                    mb: 1,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 0.75,
                  }}>
                  <Box
                    component={'span'}
                    sx={{
                      fontWeight: 600,
                      color: 'primary.main',
                    }}>
                    Pedido#{order.id}
                  </Box>
                  <Box
                    sx={{
                      width: 5,
                      height: 5,
                      bgcolor: '#ccc',
                      borderRadius: '100%',
                    }}></Box>
                  Itens
                </Typography>
                <Box sx={{ display: 'flex', gap: 1.5 }}>
                  {order.items?.map((item, idx) => (
                    <Chip
                      key={idx}
                      size="small"
                      label={`${item.quantity}x ${item.name}`}></Chip>
                  ))}
                </Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ display: 'block', mt: 1 }}>
                  <Box component={'span'} sx={{ fontWeight: 700 }}>
                    Endereço:{' '}
                  </Box>
                  {order.address}
                </Typography>
              </Box>
              {next?.length ? (
                <Box>
                  <Typography variant="subtitle2" sx={{ mb: 1 }}>
                    Mover para
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {next?.map((status) => {
                      const meta = STATUS_META[status];
                      const isCancel = status === 'CANCELED';
                      return (
                        <Button
                          key={status}
                          size="small"
                          variant="contained"
                          onClick={() => onQuickChange(order.id, status)}
                          sx={{
                            bgcolor: meta.color,
                            '&:hover': {
                              bgcolor: meta.color,
                              opacity: 0.9,
                            },
                          }}>
                          {isCancel ? 'Cancelado' : meta?.label}
                        </Button>
                      );
                    })}
                  </Box>
                </Box>
              ) : (
                <Box>Pedido finalizado</Box>
              )}
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
};
