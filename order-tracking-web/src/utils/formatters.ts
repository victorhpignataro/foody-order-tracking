const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export function formatCurrency(value: number) {
  return currencyFormatter.format(Number.isFinite(value) ? value : 0);
}

export function formatCurrencyInput(value: number | undefined | null) {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '';
  }

  return formatCurrency(value);
}

export function parseCurrencyInput(value: string) {
  const digits = value.replace(/\D/g, '');

  if (!digits) return undefined;

  return Number(digits) / 100;
}

export function timeAgo(iso: string) {
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
