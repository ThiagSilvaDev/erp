export const PERMISSIONS = {
  userWrite: 'user:write',
  roleRead: 'iam:role:read',
  roleWrite: 'iam:role:write',
  roleAssign: 'iam:role:assign',
  membershipRead: 'iam:membership:read',
  membershipWrite: 'iam:membership:write',
  ownershipTransfer: 'iam:ownership:transfer',
} as const;

export type Permission = (typeof PERMISSIONS)[keyof typeof PERMISSIONS];

export const ROLE_CODES = {
  owner: 'OWNER',
  admin: 'ADMIN',
  manager: 'MANAGER',
  operator: 'OPERATOR',
  driver: 'DRIVER',
} as const;

/**
 * Seed catalog of permission codes used to pre-populate the role editor.
 * The API exposes no permissions-catalog endpoint, so suggestions are
 * derived from this list plus the permissions observed on existing roles
 * and memberships. Custom codes can still be typed freely.
 */
export const KNOWN_PERMISSIONS: readonly string[] = [
  'user:read',
  'user:write',
  'iam:role:read',
  'iam:role:write',
  'iam:role:assign',
  'iam:membership:read',
  'iam:membership:write',
  'iam:ownership:transfer',
  'catalog:read',
  'catalog:write',
  'customers:read',
  'customers:write',
  'suppliers:read',
  'suppliers:write',
  'sales:read',
  'sales:write',
  'inventory:read',
  'inventory:write',
  'logistics:read',
  'logistics:write',
  'financial:read',
  'financial:write',
  'kpis:read',
  'platform:company:read',
  'platform:company:write',
];