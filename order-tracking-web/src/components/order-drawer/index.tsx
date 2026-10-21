import { ordersApi } from '@/api/order';
import { useSnackbar } from '@/context/snackbar-context/context';
import { DeleteOutlined } from '@mui/icons-material';
import AddIcon from '@mui/icons-material/Add';
import {
  Box,
  Button,
  Divider,
  Drawer,
  IconButton,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { FC } from 'react';
import { Controller, useFieldArray, useForm } from 'react-hook-form';

type OrderFormValues = {
  client: string;
  address: string;
  items: {
    name: string;
    quantity: number;
    unitPrice: number;
  }[];
};

type DrawerOrderProps = {
  drawerOpen: boolean;
  onCloseDrawer: () => void;
};

export const OrderDrawer: FC<DrawerOrderProps> = ({
  drawerOpen,
  onCloseDrawer,
}) => {
  const { toastMessage } = useSnackbar();
  const queryClient = useQueryClient();

  const {
    control,
    handleSubmit,
    reset: resetFormFields,
    clearErrors: clearFormErrors,
    formState: { errors },
  } = useForm<OrderFormValues>({
    defaultValues: {
      client: '',
      address: '',
      items: [{ name: '', quantity: undefined, unitPrice: undefined }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const handleCloseDrawer = () => {
    onCloseDrawer();
    clearFormErrors();
  };

  const { mutateAsync, isPending } = useMutation({
    mutationFn: async (data: OrderFormValues) => {
      await ordersApi.create(data);
      queryClient.invalidateQueries({
        queryKey: ['orders'],
        exact: false,
      });
    },
    onSuccess: () => {
      toastMessage('Pedido criado com sucesso');
      handleCloseDrawer();
      resetFormFields();
    },
  });

  const onSubmit = async (_data: OrderFormValues) => {
    mutateAsync(_data);
  };

  return (
    <Drawer anchor="right" open={drawerOpen} onClose={handleCloseDrawer}>
      <Box
        component="form"
        onSubmit={handleSubmit(onSubmit)}
        sx={{
          width: { xs: '100vw', sm: 420 },
          p: 2,
          pt: 1,
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
        }}>
        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 700, pb: 1 }}>
              Novo pedido
            </Typography>
            <Divider />
          </Box>
          <Controller
            name="client"
            control={control}
            rules={{
              required: 'Cliente é obrigatório',
              maxLength: {
                value: 256,
                message:
                  'O nome do cliente deve conter no máximo 256 caracteres',
              },
            }}
            render={({ field }) => (
              <TextField
                {...field}
                label="Cliente"
                size="small"
                fullWidth
                error={!!errors.client}
                helperText={errors.client?.message}
              />
            )}
          />

          <Controller
            name="address"
            control={control}
            rules={{
              required: 'Endereço é obrigatório',
              maxLength: {
                value: 256,
                message: 'O endereço conter no máximo 256 caracteres',
              },
            }}
            render={({ field }) => (
              <TextField
                {...field}
                label="Endereço de entrega"
                size="small"
                fullWidth
                error={!!errors.address}
                helperText={errors.address?.message}
              />
            )}
          />

          <Typography variant="subtitle2" sx={{ mt: 1 }}>
            Itens do pedido
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {fields.map((field, index) => (
              <Paper
                key={field.id}
                variant="outlined"
                sx={{
                  p: 1.5,
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 2,
                }}>
                <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
                  <Controller
                    name={`items.${index}.name`}
                    control={control}
                    rules={{
                      required: 'Obrigatório',
                      maxLength: {
                        value: 256,
                        message:
                          'O nome do item deve conter no máximo 256 caracteres',
                      },
                    }}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Nome do item"
                        size="small"
                        error={!!errors.items?.[index]?.name}
                        helperText={errors.items?.[index]?.name?.message}
                        sx={{ flex: 2 }}
                      />
                    )}
                  />
                  <Controller
                    name={`items.${index}.quantity`}
                    control={control}
                    rules={{
                      required: 'Obrigatório',
                      min: { value: 1, message: 'Mín. 1' },
                    }}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Qtd"
                        type="number"
                        size="small"
                        error={!!errors.items?.[index]?.quantity}
                        helperText={errors.items?.[index]?.quantity?.message}
                        onChange={(e) => field.onChange(Number(e.target.value))}
                        sx={{ width: 90 }}
                      />
                    )}
                  />
                  <IconButton
                    size="small"
                    color="error"
                    onClick={() => remove(index)}
                    disabled={fields.length === 1}
                    sx={{ mt: 0.5 }}>
                    <DeleteOutlined fontSize="small" />
                  </IconButton>
                </Box>
                <Controller
                  name={`items.${index}.unitPrice`}
                  control={control}
                  rules={{
                    required: 'Obrigatório',
                    min: { value: 0, message: 'Valor inválido' },
                  }}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Preço unitário (R$)"
                      type="number"
                      size="small"
                      error={!!errors.items?.[index]?.unitPrice}
                      helperText={errors.items?.[index]?.unitPrice?.message}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                    />
                  )}
                />
              </Paper>
            ))}
          </Box>

          <Button
            size="small"
            startIcon={<AddIcon />}
            onClick={() => append({ name: '', quantity: 1, unitPrice: 0 })}
            sx={{ alignSelf: 'flex-start' }}>
            Adicionar item
          </Button>
        </Box>

        <Divider sx={{ mt: 1 }} />

        <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
          <Button onClick={handleCloseDrawer} color="inherit">
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isPending}
            loading={isPending}>
            Criar pedido
          </Button>
        </Box>
      </Box>
    </Drawer>
  );
};
