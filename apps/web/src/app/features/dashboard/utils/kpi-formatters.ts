const numberFormatter = new Intl.NumberFormat('pt-BR', {
  maximumFractionDigits: 2,
});

const percentFormatter = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const dateFormatter = new Intl.DateTimeFormat('en-US', {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
  timeZone: 'UTC',
});

const shortDateFormatter = new Intl.DateTimeFormat('en-US', {
  month: 'short',
  day: 'numeric',
  timeZone: 'UTC',
});

const timestampFormatter = new Intl.DateTimeFormat('en-US', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'UTC',
});

export function formatNumber(value: number | null | undefined): string {
  return value == null ? '—' : numberFormatter.format(value);
}

export function formatPercent(value: number | null | undefined): string {
  return value == null ? '—' : `${percentFormatter.format(value)}%`;
}

export function formatHours(value: number | null | undefined): string {
  return value == null ? '—' : `${numberFormatter.format(value)} h`;
}

export function formatMoney(value: number | null | undefined, currencyCode: string): string {
  if (value == null) {
    return '—';
  }

  try {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: currencyCode,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);
  } catch {
    return `${currencyCode} ${numberFormatter.format(value)}`;
  }
}

export function formatDate(value: string | undefined): string {
  const date = isoDate(value);
  return date ? dateFormatter.format(date) : '—';
}

export function formatShortDate(value: string | undefined): string {
  const date = isoDate(value);
  return date ? shortDateFormatter.format(date) : '—';
}

export function formatDateRange(from: string | undefined, to: string | undefined): string {
  if (!from || !to) {
    return 'Period unavailable';
  }
  return from === to ? formatDate(from) : `${formatDate(from)} – ${formatDate(to)}`;
}

export function formatBucketLabel(from: string | undefined, to: string | undefined): string {
  if (!from || !to) {
    return 'Unknown period';
  }
  return from === to ? formatShortDate(from) : `${formatShortDate(from)} – ${formatShortDate(to)}`;
}

export function formatTimestamp(value: string | undefined): string {
  if (!value) {
    return 'Unavailable';
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? 'Unavailable' : `${timestampFormatter.format(date)} UTC`;
}

function isoDate(value: string | undefined): Date | null {
  if (!value || !isIsoDate(value)) {
    return null;
  }
  const date = new Date(`${value}T00:00:00Z`);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function isIsoDate(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false;
  }
  const date = new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value;
}

export function inclusiveDays(from: string, to: string): number {
  const fromTime = Date.parse(`${from}T00:00:00Z`);
  const toTime = Date.parse(`${to}T00:00:00Z`);
  return Math.floor((toTime - fromTime) / 86_400_000) + 1;
}
