import { EntityPageData, EntityRecord } from '../models/entity-view-model';

interface PageLike {
  content?: object[];
  page?: {
    totalElements?: number;
  };
  totalElements?: number;
}

export function text(value: unknown): string {
  if (typeof value === 'string' || typeof value === 'number') {
    return String(value);
  }
  return '';
}

export function optionalNumber(value: unknown): number | undefined {
  const normalized = text(value).trim();
  return normalized ? Number(normalized) : undefined;
}

export function getValue(record: EntityRecord, key: string): string {
  const value = key.split('.').reduce<unknown>((current, part) => {
    if (current && typeof current === 'object' && part in current) {
      return (current as Record<string, unknown>)[part];
    }
    return undefined;
  }, record);

  return text(value) || '-';
}

export function pageData(response: PageLike): EntityPageData {
  return {
    content: response.content ?? [],
    totalElements: response.totalElements ?? response.page?.totalElements ?? 0,
  };
}
