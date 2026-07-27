import { HttpErrorResponse } from '@angular/common/http';

import { KpiLoadError } from '../models/executive-kpi-view-model';

export function toKpiLoadError(error: unknown): KpiLoadError {
  if (error instanceof HttpErrorResponse && error.status === 403) {
    return {
      kind: 'forbidden',
      message: 'You do not have permission to view executive KPIs.',
      details: ['Ask an administrator for the kpi:read permission.'],
    };
  }

  if (error instanceof HttpErrorResponse && (error.status === 400 || error.status === 422)) {
    const problem = objectValue(error.error);
    const details = invalidParameters(problem?.['invalid_params']);
    const detail = stringValue(problem?.['detail']);

    return {
      kind: 'validation',
      message: detail ?? 'The selected KPI period is invalid.',
      details,
    };
  }

  return {
    kind: 'generic',
    message: 'Unable to load executive KPIs.',
    details: ['Try again in a moment.'],
  };
}

function invalidParameters(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }

  return value.flatMap((item) => {
    const parameter = objectValue(item);
    if (!parameter) {
      return [];
    }
    const name =
      stringValue(parameter['name']) ??
      stringValue(parameter['field']) ??
      stringValue(parameter['parameter']);
    const reason = stringValue(parameter['reason']) ?? stringValue(parameter['message']);

    if (name && reason) {
      return [`${name}: ${reason}`];
    }
    return reason ? [reason] : [];
  });
}

function objectValue(value: unknown): Record<string, unknown> | null {
  return value && typeof value === 'object' ? (value as Record<string, unknown>) : null;
}

function stringValue(value: unknown): string | null {
  return typeof value === 'string' && value.trim() ? value : null;
}
