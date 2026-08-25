import { KNOWN_PERMISSIONS } from '@core/auth/permissions';
import type { TenantRoleResponse } from '@generated-api/modules/iam/dtos';

export interface PermissionGroup {
  label: string;
  options: string[];
}

const MODULE_LABELS: Record<string, string> = {
  iam: 'IAM',
  kpis: 'KPIs',
  pdv: 'PDV',
  erp: 'ERP',
};

export function domainOf(code: string): string {
  return code.split(':')[0] ?? '';
}

export function isReadPermission(code: string): boolean {
  return code.endsWith(':read');
}

export function isWritePermission(code: string): boolean {
  return code.endsWith(':write');
}

export function readCounterpart(code: string): string {
  return `${domainOf(code)}:read`;
}

export function writeCounterpart(code: string): string {
  return `${domainOf(code)}:write`;
}

export function prettifyModule(domain: string): string {
  const normalized = (MODULE_LABELS[domain] ?? domain.replace(/-/g, ' ')).trim();
  return normalized.charAt(0).toUpperCase() + normalized.slice(1);
}

export function sanitizePermissions(values: readonly string[]): string[] {
  return [...new Set(values.map((value) => value.trim()).filter(Boolean))].sort((a, b) =>
    a.localeCompare(b),
  );
}

export function collectKnownPermissions(roles: readonly TenantRoleResponse[]): Set<string> {
  const codes = new Set<string>(KNOWN_PERMISSIONS);
  for (const role of roles) {
    for (const permission of role.permissions ?? []) {
      codes.add(permission);
    }
  }
  return codes;
}

export function buildPermissionGroups(codes: Iterable<string>): PermissionGroup[] {
  const groups = new Map<string, Set<string>>();
  for (const code of codes) {
    const domain = domainOf(code) || code;
    groups.set(domain, (groups.get(domain) ?? new Set<string>()).add(code));
  }

  return [...groups.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([domain, options]) => ({
      label: prettifyModule(domain),
      options: [...options].sort((a, b) => a.localeCompare(b)),
    }));
}