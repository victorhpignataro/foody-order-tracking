import { OrderRow } from '@/components/order-table/row';
import { AddOutlined } from '@mui/icons-material';
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
} from '@mui/material';

import { ordersApi } from '@/api/order';
import { useSnackbar } from '@/context/snackbar-context/context';
import { STATUS_META, type Order, type OrderStatus } from '@/models/order';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { useMemo, useState, type ChangeEvent } from 'react';
import { useDebounce } from 'use-debounce';
import { DrawerOrder } from '../order-drawer';
import { StatusDialog } from '../status-dialog';
import { RowError } from './row-error';
import { TableHeader } from './table-header';
import { TableLoader } from './table-loader';

export const OrderTable = () => {
  const { toastMessage } = useSnackbar();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [dialogOrder, setDialogOrder] = useState<Order | null>(null);

  const toggleOrderDrawer = (newOpen: boolean) => () => setDrawerOpen(newOpen);
  const handleCloseOrderDrawer = () => {
    setDrawerOpen(false);
  };

  const handleDialogConfirm = async (id: number, status: OrderStatus) => {
    await ordersApi.changeStatus(id, status);
    toastMessage(
      `Pedido #${id} agora está como ${STATUS_META[status].label.toLowerCase()}.`,
    );
  };

  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm] = useDebounce(searchTerm, 300);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['orders', debouncedSearchTerm, page, rowsPerPage],
    queryFn: () => ordersApi.list(debouncedSearchTerm, page, rowsPerPage),
    placeholderData: keepPreviousData,
  });

  const handleQuickChange = async (id: number, status: OrderStatus) => {
    try {
      await ordersApi.changeStatus(id, status);
      refetch();
      toastMessage(
        `Pedido #${id} agora está como ${STATUS_META[status].label.toLowerCase()}.`,
      );
    } catch (err) {
      toastMessage(
        err instanceof Error
          ? err.message
          : 'Não foi possível atualizar o status.',
      );
    }
  };

  const filteredOrders = useMemo(() => {
    const dataContent = data?.content;
    if (!dataContent) return [];
    return dataContent;
  }, [data]);

  const handleRefresh = () => {
    refetch();
  };

  const isEmpty = !isLoading && !isError && filteredOrders.length === 0;
  const isTrulyEmpty = !isLoading && !isError && (!data || data.length === 0);

  return (
    <>
      <TableHeader
        handleRefresh={handleRefresh}
        isLoading={isLoading}
        search={searchTerm}
        onSearchChange={(event: ChangeEvent<HTMLInputElement>) => {
          setSearchTerm(event.target.value);
        }}
        onClickNewOrder={toggleOrderDrawer(true)}></TableHeader>
      <Paper
        variant="outlined"
        sx={{
          position: 'relative',
          overflow: 'hidden',
        }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Pedido</TableCell>
                <TableCell>Horário</TableCell>
                <TableCell>Cliente</TableCell>
                <TableCell>Endereço de entrega</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="center">Ações</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredOrders.map((order) => (
                <OrderRow
                  key={order.id}
                  order={order}
                  onEditStatus={() => {
                    setDialogOrder(order);
                  }}
                  onQuickChange={handleQuickChange}
                />
              ))}
              {isEmpty && (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <Box
                      sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: 2,
                      }}>
                      <Typography color="text.secondary" sx={{ mb: 1 }}>
                        {isTrulyEmpty
                          ? 'Nenhum pedido cadastrado.'
                          : 'Nenhum pedido encontrado'}
                      </Typography>
                      {isTrulyEmpty && (
                        <Button
                          variant="contained"
                          startIcon={<AddOutlined />}
                          onClick={toggleOrderDrawer(true)}>
                          Criar primeiro pedido
                        </Button>
                      )}
                    </Box>
                  </TableCell>
                </TableRow>
              )}
              {isError && <RowError handleRefresh={handleRefresh}></RowError>}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={data?.totalElements}
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

        {(isLoading || isError) && <TableLoader isLoading={isLoading} />}
      </Paper>
      <DrawerOrder
        drawerOpen={drawerOpen}
        handleCloseDrawer={handleCloseOrderDrawer}
      />
      <StatusDialog
        order={dialogOrder}
        onClose={() => setDialogOrder(null)}
        onConfirm={handleDialogConfirm}
      />
    </>
  );
};
