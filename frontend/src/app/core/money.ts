export function euro(value?: number | string | null): string {
  return new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(Number(value || 0));
}

export function day(value?: string | null): string {
  if (!value) return '—';
  return new Intl.DateTimeFormat('it-IT').format(new Date(value));
}

export function statusChip(status?: string): string {
  switch (status) {
    case 'DRAFT': return 'chip warn';
    case 'CONFIRMED': return 'chip info';
    case 'ACCEPTED':
    case 'PAID': return 'chip ok';
    case 'REJECTED':
    case 'VOIDED': return 'chip bad';
    default: return 'chip';
  }
}

export function statusLabel(status?: string): string {
  const map: Record<string, string> = {
    DRAFT: 'Bozza',
    CONFIRMED: 'Confermato',
    SUBMITTED: 'Inviato SDI',
    ACCEPTED: 'Accettato',
    REJECTED: 'Scartato',
    PARTIALLY_PAID: 'Parziale',
    PAID: 'Pagato',
    VOIDED: 'Stornato',
    READY: 'Pronta',
    RECEIVED: 'Ricevuta',
    NOT_APPLICABLE: '—'
  };
  return map[status || ''] || status || '—';
}
