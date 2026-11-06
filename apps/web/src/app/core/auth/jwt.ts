export type TokenUse = 'global' | 'scoped' | 'platform';

const TOKEN_USE_VALUES: readonly TokenUse[] = ['global', 'scoped', 'platform'];

export function decodeJwt(token: string | null | undefined): Record<string, unknown> | null {
  if (!token || typeof token !== 'string' || !token.includes('.')) {
    return null;
  }

  const payload = token.split('.')[1];
  if (!payload) {
    return null;
  }

  try {
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized + '='.repeat((4 - (normalized.length % 4)) % 4);
    const decoded = atob(padded);
    const json = decodeURIComponent(
      decoded
        .split('')
        .map((char) => '%' + char.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    );
    return JSON.parse(json) as Record<string, unknown>;
  } catch {
    return null;
  }
}

export function decodeTokenUse(token: string | null | undefined): TokenUse | null {
  const claims = decodeJwt(token);
  const value = claims?.['token_use'];
  return typeof value === 'string' && TOKEN_USE_VALUES.includes(value as TokenUse)
    ? (value as TokenUse)
    : null;
}

export function decodeAuthorities(token: string | null | undefined): string[] {
  const claims = decodeJwt(token);
  if (!claims) {
    return [];
  }

  const candidates: unknown[] = [
    claims['authorities'],
    claims['perms'],
    claims['permissions'],
    claims['effective_permissions'],
    claims['scope'],
  ];

  for (const candidate of candidates) {
    if (Array.isArray(candidate)) {
      const flattened = candidate.filter((entry): entry is string => typeof entry === 'string');
      if (flattened.length) {
        return flattened;
      }
    }

    if (typeof candidate === 'string' && candidate.trim()) {
      const spaceSeparated = candidate.split(/\s+/).filter(Boolean);
      if (spaceSeparated.length) {
        return spaceSeparated;
      }
    }
  }

  return [];
}