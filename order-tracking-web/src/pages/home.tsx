import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import SearchIcon from '@mui/icons-material/Search';
import {
  Box,
  Button,
  Card,
  CardActionArea,
  Container,
  InputAdornment,
  LinearProgress,
  MenuItem,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router';

import { OrderRow } from '@components/order-row';
import { StatusDialog } from '@components/status-dialog';
import { useOrders } from '../hooks/use-orders';
import {
  ORDER_STATUSES,
  STATUS_META,
  type Order,
  type OrderStatus,
} from '../models/order';

export const HomePage = () => {
  const navigate = useNavigate();
  const { orders, loading, reload, changeStatus } = useOrders();

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [dialogOrder, setDialogOrder] = useState<Order | null>(null);
  const [toast, setToast] = useState('');

  const counts = useMemo(() => {
    const base = Object.fromEntries(
      ORDER_STATUSES.map((s) => [s, 0]),
    ) as Record<OrderStatus, number>;
    orders.forEach((o) => (base[o.status] += 1));
    return base;
  }, [orders]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase().replace('#', '');
    return orders.filter(
      (o) =>
        (statusFilter === 'ALL' || o.status === statusFilter) &&
        (!q || String(o.id).includes(q) || o.client.toLowerCase().includes(q)),
    );
  }, [orders, search, statusFilter]);

  const visible = filtered.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage,
  );

  const applyStatusFilter = (value: OrderStatus | 'ALL') => {
    setStatusFilter(value);
    setPage(0);
  };

  const handleQuickChange = async (id: number, status: OrderStatus) => {
    try {
      await changeStatus(id, status);
      setToast(
        `Pedido #${id} agora está como ${STATUS_META[status].label.toLowerCase()}.`,
      );
    } catch (err) {
      setToast(
        err instanceof Error
          ? err.message
          : 'Não foi possível atualizar o status.',
      );
    }
  };

  const handleDialogConfirm = async (id: number, status: OrderStatus) => {
    await changeStatus(id, status);
    setToast(
      `Pedido #${id} agora está como ${STATUS_META[status].label.toLowerCase()}.`,
    );
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Container
        maxWidth={false}
        sx={{ py: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>
        <Box
          sx={{
            display: 'grid',
            gap: 2,
            gridTemplateColumns: {
              xs: 'repeat(2, 1fr)',
              sm: 'repeat(3, 1fr)',
              lg: 'repeat(5, 1fr)',
            },
          }}>
          {ORDER_STATUSES.map((status) => {
            const meta = STATUS_META[status];
            const active = statusFilter === status;
            return (
              <Card
                key={status}
                variant="outlined"
                sx={{
                  borderColor: active ? meta.color : 'divider',
                  boxShadow: active
                    ? `0 0 0 1px ${meta.color}`
                    : '0 1px 3px rgba(15, 23, 42, 0.08)',
                }}>
                <CardActionArea
                  onClick={() => applyStatusFilter(active ? 'ALL' : status)}
                  sx={{ p: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Box
                      sx={{
                        width: 10,
                        height: 10,
                        borderRadius: '50%',
                        bgcolor: meta.color,
                      }}
                    />
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{ fontWeight: 600 }}>
                      {meta.label}
                    </Typography>
                  </Box>
                  <Typography
                    sx={{
                      mt: 1,
                      fontSize: 32,
                      lineHeight: '40px',
                      fontWeight: 700,
                    }}>
                    {counts[status]}
                  </Typography>
                  <Typography
                    variant="caption"
                    sx={{ color: meta.color, fontWeight: 600 }}>
                    {meta.hint}
                  </Typography>
                </CardActionArea>
              </Card>
            );
          })}
        </Box>

        <Paper
          variant="outlined"
          sx={{
            p: 2,
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 2,
          }}>
          <TextField
            size="small"
            placeholder="Buscar por número ou cliente"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            sx={{ width: { xs: '100%', sm: 320 } }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              },
            }}
          />
          <TextField
            select
            size="small"
            label="Status"
            value={statusFilter}
            onChange={(e) =>
              applyStatusFilter(e.target.value as OrderStatus | 'ALL')
            }
            sx={{ width: { xs: '100%', sm: 220 } }}>
            <MenuItem value="ALL">Todos</MenuItem>
            {ORDER_STATUSES.map((s) => (
              <MenuItem key={s} value={s}>
                {STATUS_META[s].label}
              </MenuItem>
            ))}
          </TextField>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant="outlined"
              color="inherit"
              startIcon={<RefreshIcon />}
              onClick={reload}
              disabled={loading}
              sx={{ borderColor: '#CBD5E1', color: '#334155' }}>
              Atualizar
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/pedidos/novo')}>
              Novo pedido
            </Button>
          </Box>
        </Paper>

        <Paper
          variant="outlined"
          sx={{
            overflow: 'hidden',
            boxShadow: '0 1px 3px rgba(15, 23, 42, 0.08)',
          }}>
          {loading && <LinearProgress />}
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Pedido</TableCell>
                  <TableCell>Horário</TableCell>
                  <TableCell>Cliente</TableCell>
                  <TableCell>Itens</TableCell>
                  <TableCell>Endereço de entrega</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="center">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {visible.map((order) => (
                  <OrderRow
                    key={order.id}
                    order={order}
                    onEditStatus={setDialogOrder}
                    onQuickChange={handleQuickChange}
                  />
                ))}
                {!loading && visible.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                      <Typography color="text.secondary" sx={{ mb: 1 }}>
                        {orders.length === 0
                          ? 'Nenhum pedido ainda.'
                          : 'Nenhum pedido encontrado com esses filtros.'}
                      </Typography>
                      {orders.length === 0 && (
                        <Button
                          variant="contained"
                          startIcon={<AddIcon />}
                          onClick={() => navigate('/pedidos/novo')}>
                          Criar primeiro pedido
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={filtered.length}
            page={page}
            onPageChange={(_e, p) => setPage(p)}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(Number(e.target.value));
              setPage(0);
            }}
            rowsPerPageOptions={[5, 10, 25, 50]}
            labelRowsPerPage="Linhas por página:"
            labelDisplayedRows={({ from, to, count }) =>
              `${from}-${to} de ${count}`
            }
          />
        </Paper>
      </Container>

      <StatusDialog
        order={dialogOrder}
        onClose={() => setDialogOrder(null)}
        onConfirm={handleDialogConfirm}
      />

      <Snackbar
        open={!!toast}
        autoHideDuration={3500}
        onClose={() => setToast('')}
        message={toast}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      />
    </Box>
  );
};
