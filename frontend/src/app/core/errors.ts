export function apiError(err: unknown, fallback = 'Salvataggio non riuscito') {
  const e = err as { error?: { message?: string }; message?: string };
  return e?.error?.message || e?.message || fallback;
}
