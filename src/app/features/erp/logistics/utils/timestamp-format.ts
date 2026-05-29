export function formatTimestamp(value: unknown, fallback = '-'): string {
  if (typeof value !== 'string' && typeof value !== 'number') {
    return fallback;
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return fallback;
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}
