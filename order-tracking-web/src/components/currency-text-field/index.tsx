import { formatCurrencyInput, parseCurrencyInput } from '@/utils/formatters';
import { TextField, type TextFieldProps } from '@mui/material';
import type { ChangeEvent, FC } from 'react';

type CurrencyTextFieldProps = Omit<
  TextFieldProps,
  'value' | 'onChange' | 'type'
> & {
  value: number | undefined;
  onChange: (value: number | undefined) => void;
};

export const CurrencyTextField: FC<CurrencyTextFieldProps> = ({
  value,
  onChange,
  ...props
}) => {
  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    onChange(parseCurrencyInput(event.target.value));
  };

  return (
    <TextField
      {...props}
      value={formatCurrencyInput(value)}
      onChange={handleChange}
      placeholder={props.placeholder ?? 'R$ 0,00'}
      slotProps={{
        ...props.slotProps,
        htmlInput: {
          inputMode: 'numeric',
          ...props.slotProps?.htmlInput,
        },
      }}
    />
  );
};
