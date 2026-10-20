import { Chip } from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import { STATUS_META, type OrderStatus } from '@models/order';

export const StatusChip = ({ status }: { status: OrderStatus }) => {
  const meta = STATUS_META[status];
  return (
    <Chip
      size="small"
      label={meta?.label}
      icon={<FiberManualRecordIcon sx={{ fontSize: '10px !important' }} />}
      sx={{
        bgcolor: meta?.bg,
        color: meta?.color,
        fontWeight: 600,
        '& .MuiChip-icon': { color: 'inherit' },
      }}
    />
  );
};
