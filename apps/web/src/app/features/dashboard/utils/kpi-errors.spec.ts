import { HttpErrorResponse } from '@angular/common/http';

import { toKpiLoadError } from './kpi-errors';

describe('toKpiLoadError', () => {
  it('maps forbidden responses to the permission-specific state', () => {
    expect(toKpiLoadError(new HttpErrorResponse({ status: 403 }))).toEqual(
      expect.objectContaining({
        kind: 'forbidden',
        message: expect.stringContaining('permission'),
      }),
    );
  });

  it('extracts standard invalid_params entries from ProblemDetail', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        detail: 'Invalid KPI period',
        invalid_params: [
          { name: 'from', reason: 'must be provided with to' },
          { field: 'bucket', message: 'unsupported value' },
        ],
      },
    });

    expect(toKpiLoadError(error)).toEqual({
      kind: 'validation',
      message: 'Invalid KPI period',
      details: ['from: must be provided with to', 'bucket: unsupported value'],
    });
  });
});
