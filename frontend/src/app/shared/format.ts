const RELATIVE = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });

const DIVISIONS: { amount: number; unit: Intl.RelativeTimeFormatUnit }[] = [
  { amount: 60, unit: 'seconds' },
  { amount: 60, unit: 'minutes' },
  { amount: 24, unit: 'hours' },
  { amount: 7, unit: 'days' },
  { amount: 4.34524, unit: 'weeks' },
  { amount: 12, unit: 'months' },
  { amount: Number.POSITIVE_INFINITY, unit: 'years' },
];

export function relativeTime(iso: string | null): string {
  if (!iso) {
    return 'never';
  }
  let delta = (new Date(iso).getTime() - Date.now()) / 1000;
  for (const division of DIVISIONS) {
    if (Math.abs(delta) < division.amount) {
      return RELATIVE.format(Math.round(delta), division.unit);
    }
    delta /= division.amount;
  }
  return 'never';
}

export function metric(value: number | null | undefined, unit = '', digits = 0): string {
  if (value === null || value === undefined) {
    return '—';
  }
  return `${value.toFixed(digits)}${unit}`;
}
