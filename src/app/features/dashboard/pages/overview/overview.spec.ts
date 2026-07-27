import { convertToParamMap } from '@angular/router';

import { parseKpiQuery, validateKpiFilter } from './overview';

describe('KPI dashboard filters', () => {
  it('uses backend defaults when the URL has no KPI parameters', () => {
    expect(parseKpiQuery(convertToParamMap({}))).toEqual({
      model: { from: '', to: '', bucket: 'DAY' },
      params: undefined,
    });
  });

  it('maps bookmarkable URL parameters into the generated API request', () => {
    const parsed = parseKpiQuery(
      convertToParamMap({ from: '2026-07-01', to: '2026-07-31', bucket: 'week' }),
    );

    expect(parsed).toEqual({
      model: { from: '2026-07-01', to: '2026-07-31', bucket: 'WEEK' },
      params: { from: '2026-07-01', to: '2026-07-31', bucket: 'WEEK' },
    });
  });

  it('rejects invalid buckets and incomplete date ranges before calling the API', () => {
    expect(parseKpiQuery(convertToParamMap({ bucket: 'quarter' })).error).toEqual(
      expect.objectContaining({ kind: 'validation' }),
    );
    expect(parseKpiQuery(convertToParamMap({ from: '2026-07-01', bucket: 'DAY' })).error).toEqual(
      expect.objectContaining({ kind: 'validation' }),
    );
  });

  it('validates order and the inclusive 366-day maximum', () => {
    expect(validateKpiFilter({ from: '2026-07-31', to: '2026-07-01', bucket: 'DAY' })).toContain(
      'start date',
    );
    expect(validateKpiFilter({ from: '2024-01-01', to: '2025-01-01', bucket: 'MONTH' })).toContain(
      '366 days',
    );
    expect(validateKpiFilter({ from: '2024-01-01', to: '2024-12-31', bucket: 'MONTH' })).toBeNull();
  });
});
