import { ordersApi } from '@/api/order';
import { useSnackbar } from '@/context/snackbar-context/context';
import {
  NEXT_STATUS,
  ORDER_STATUSES,
  STATUS_META,
  type Order,
  type OrderStatus,
} from '@models/order';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Radio,
  RadioGroup,
  Typography,
} from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
interface Props {
  order: Order | null;
  onClose: () => void;
}

export const StatusDialog = ({ order, onClose }: Props) => {
  const { toastMessage } = useSnackbar();
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<OrderStatus | ''>('');

  const allowed = order?.status ? NEXT_STATUS[order.status] : [];

  const handleClose = () => {
    onClose();
    setSelected('');
  };

  const handleConfirm = async () => {
    if (!order || !selected) return;
    mutateAsync({ id: order?.id, newStatus: selected });
  };

  const { mutateAsync, isPending } = useMutation({
    mutationFn: async ({
      id,
      newStatus,
    }: {
      id: Order['id'];
      newStatus: OrderStatus;
    }) => {
      await ordersApi.changeStatus(id, newStatus);
      queryClient.invalidateQueries({
        queryKey: ['orders'],
        exact: false,
      });
    },
    onSuccess: () => {
      toastMessage(`Status do pedido #${order?.id} alterado com sucesso`);
      handleClose();
    },
    onError: () => {
      toastMessage(`Não foi possível atualizar o status.`, 'error');
      handleClose();
    },
  });

  return (
    <Dialog open={!!order} onClose={handleClose} fullWidth maxWidth="xs">
      <DialogTitle sx={{ fontWeight: 600 }}>
        Alterar status do pedido #{order?.id}
      </DialogTitle>
      <DialogContent>
        {order?.status && (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{ mb: 2, display: 'inline-flex', gap: 0.5 }}>
            Mover de
            <Box
              component="span"
              sx={{
                color: STATUS_META[order.status].color,
              }}>
              {STATUS_META[order.status].label}
            </Box>
            para
          </Typography>
        )}

        <RadioGroup
          value={selected}
          onChange={(e) => setSelected(e.target.value as OrderStatus)}>
          {ORDER_STATUSES.map((status) => {
            const meta = STATUS_META[status];
            const disabled =
              status !== order?.status && !allowed?.includes(status);
            const checked = selected === status;
            return (
              <Box
                key={status}
                sx={{
                  mb: 1,
                  px: 1.5,
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: checked ? meta.color : 'transparent',
                  bgcolor: checked ? meta.bg : 'transparent',
                }}>
                <FormControlLabel
                  value={status}
                  disabled={disabled}
                  control={
                    <Radio sx={{ '&.Mui-checked': { color: meta.color } }} />
                  }
                  label={
                    <Typography
                      variant="subtitle2"
                      sx={{ color: disabled ? 'text.disabled' : meta.color }}>
                      {meta.label}
                    </Typography>
                  }
                  sx={{ width: '100%', m: 0 }}
                />
              </Box>
            );
          })}
        </RadioGroup>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={handleClose} color="inherit">
          Cancelar
        </Button>
        <Button
          variant="contained"
          onClick={handleConfirm}
          loading={isPending}
          disabled={isPending || !selected || selected === order?.status}>
          Salvar status
        </Button>
      </DialogActions>
    </Dialog>
  );
};
