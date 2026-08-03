import { convertToParamMap } from '@angular/router';

import { parseBalanceQuery, validateBalanceFilters } from './account-balances-page';

describe('AccountBalancesPage filters', () => {
  it('keeps the page idle with default BRL when query filters are absent', () => {
    expect(parseBalanceQuery(convertToParamMap({}))).toEqual({
      model: { from: '', to: '', currencyCode: 'BRL' },
      hasQuery: false,
      params: undefined,
      validationMessage: undefined,
    });
  });

  it('builds generated API params from a valid shareable query', () => {
    expect(
      parseBalanceQuery(
        convertToParamMap({ from: '2026-08-01', to: '2026-08-31', currencyCode: 'brl' }),
      ),
    ).toEqual({
      model: { from: '2026-08-01', to: '2026-08-31', currencyCode: 'BRL' },
      hasQuery: true,
      params: { from: '2026-08-01', to: '2026-08-31', currencyCode: 'BRL' },
      validationMessage: undefined,
    });
  });

  it('rejects reversed periods', () => {
    expect(
      validateBalanceFilters({ from: '2026-08-31', to: '2026-08-01', currencyCode: 'BRL' }),
    ).toBe('The from date must be on or before the to date.');
  });

  it('requires complete filters', () => {
    expect(validateBalanceFilters({ from: '2026-08-01', to: '', currencyCode: 'BRL' })).toBe(
      'From date, to date, and currency are required.',
    );
  });
});
