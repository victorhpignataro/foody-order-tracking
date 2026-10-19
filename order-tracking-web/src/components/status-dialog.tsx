import { useEffect, useState } from 'react';
import {
  Alert,
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
import {
  NEXT_STATUS,
  ORDER_STATUSES,
  STATUS_META,
  type Order,
  type OrderStatus,
} from '@models/order';

interface Props {
  order: Order | null;
  onClose: () => void;
  onConfirm: (id: number, status: OrderStatus) => Promise<void>;
}

export const StatusDialog = ({ order, onClose, onConfirm }: Props) => {
  const [selected, setSelected] = useState<OrderStatus | ''>('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setSelected(order?.status ?? '');
    setError('');
  }, [order]);

  if (!order) return null;

  const allowed = NEXT_STATUS[order.status];

  const handleConfirm = async () => {
    if (!selected) return;
    setSaving(true);
    setError('');
    try {
      await onConfirm(order.id, selected);
      onClose();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Não foi possível atualizar o status.',
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle sx={{ fontWeight: 600 }}>
        Alterar status do pedido #{order.id}
      </DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Só as etapas seguintes ficam disponíveis.
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <RadioGroup
          value={selected}
          onChange={(e) => setSelected(e.target.value as OrderStatus)}>
          {ORDER_STATUSES.map((status) => {
            const meta = STATUS_META[status];
            const disabled =
              status !== order.status && !allowed.includes(status);
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
        <Button onClick={onClose} color="inherit">
          Cancelar
        </Button>
        <Button
          variant="contained"
          onClick={handleConfirm}
          disabled={saving || !selected || selected === order.status}>
          {saving ? 'Salvando...' : 'Salvar status'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
