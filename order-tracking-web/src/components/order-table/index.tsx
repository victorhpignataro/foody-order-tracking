import { OrderRow } from '@/components/order-table/row/row';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
} from '@mui/material';

import { ordersApi } from '@/api/order';
import { useSnackbar } from '@/context/snackbar-context/context';
import { STATUS_META, type Order, type OrderStatus } from '@/models/order';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import React, { useMemo, useState } from 'react';
import { SpinnerLoader } from '../spinner-loader';
import { StatusDialog } from '../status-dialog';
import { TableHeader } from './header';
import { RowEmpty } from './row/row-empty';
import { RowError } from './row/row-error';

import { OrderDrawer } from '../order-drawer';

export const OrderTable: React.FC = () => {
  const { toastMessage } = useSnackbar();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [dialogOrder, setDialogOrder] = useState<Order | null>(null);

  const toggleOrderDrawer = (newOpen: boolean) => () => setDrawerOpen(newOpen);
  const handleCloseOrderDrawer = () => {
    setDrawerOpen(false);
  };

  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const {
    data: dataDB,
    isLoading,
    isError,
    refetch,
  } = useQuery({
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

  const dataTable = useMemo(() => {
    const dataContent = dataDB?.content;
    if (!dataContent) return [];
    return dataContent;
  }, [dataDB]);

  const handleRefresh = () => {
    refetch();
  };

  const isEmpty = !isLoading && !isError && dataTable.length === 0;
  const isTrulyEmpty =
    !isLoading && !isError && (!dataTable || dataTable.length === 0);

  return (
    <>
      <TableHeader
        handleRefresh={handleRefresh}
        isLoading={isLoading}
        onSearchChange={(debouncedSearchTerm: string) => {
          setDebouncedSearchTerm(debouncedSearchTerm);
        }}
        onClickNewOrder={toggleOrderDrawer(true)}
      />
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
              {dataTable.map((order) => (
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
                <RowEmpty
                  isTrulyEmpty={isTrulyEmpty}
                  toggleOrderDrawer={toggleOrderDrawer}
                />
              )}
              {isError && <RowError handleRefresh={handleRefresh} />}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={dataDB?.totalElements ?? 0}
          page={page}
          onPageChange={(_event, page) => setPage(page)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(event) => {
            setRowsPerPage(Number(event.target.value));
            setPage(0);
          }}
          rowsPerPageOptions={[5, 10, 25, 50]}
          labelRowsPerPage="Linhas por página:"
          labelDisplayedRows={({ from, to, count }) =>
            `${from}-${to} de ${count}`
          }
        />

        {(isLoading || isError) && <SpinnerLoader isLoading={isLoading} />}
      </Paper>
      <OrderDrawer
        drawerOpen={drawerOpen}
        onCloseDrawer={handleCloseOrderDrawer}
      />
      <StatusDialog order={dialogOrder} onClose={() => setDialogOrder(null)} />
    </>
  );
};
