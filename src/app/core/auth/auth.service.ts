import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, tap, throwError } from 'rxjs';

import { appPaths } from '@core/routing/app-paths';
import { AuthenticationService } from '@generated-api/modules/authentication/Authentication.service';
import type {
  AuthCompanyResponse,
  AuthRoleResponse,
  LoginRequest,
  LoginResponse,
  SelectCompanyResponse,
  SelectPlatformResponse,
} from '@generated-api/modules/authentication/dtos';
import { decodeAuthorities } from './jwt';

export interface ActiveCompany {
  id: string;
  name: string;
  companyCode?: number;
}

export type TokenUse = 'global' | 'scoped' | 'platform';

export interface AuthSession {
  user?: LoginResponse['user'];
  availableCompanies: AuthCompanyResponse[];
  activeCompany?: ActiveCompany;

  globalAccessToken: string;
  globalTokenType: string;
  globalExpiresAt?: number;

  scopedAccessToken?: string;
  scopedTokenType?: string;
  scopedExpiresAt?: number;
  scopedRoles: AuthRoleResponse[];
  scopedPermissions: string[];
  scopedActiveCompanyId?: string;

  platformAccessToken?: string;
  platformTokenType?: string;
  platformExpiresAt?: number;
  platformRole?: string;
  platformPermissions: string[];
}

const AUTH_SESSION_STORAGE_KEY = 'erp.auth.session';
const PLATFORM_AUTHORITY_PREFIX = 'platform:';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthenticationService);
  private readonly router = inject(Router);
  private readonly sessionState = signal<AuthSession | null>(this.readStoredSession());

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => {
    const session = this.sessionState();
    return Boolean(session?.globalAccessToken) && !this.isGlobalExpired(session);
  });
  readonly hasActiveCompany = computed(() =>
    Boolean(this.sessionState()?.scopedActiveCompanyId && this.scopedAuthorizationHeader()),
  );
  readonly availableCompanies = computed(() => this.sessionState()?.availableCompanies ?? []);
  readonly activeCompany = computed(() => this.sessionState()?.activeCompany);

  readonly roles = computed<AuthRoleResponse[]>(() => this.sessionState()?.scopedRoles ?? []);
  readonly roleCodes = computed<Set<string>>(() => buildRoleCodeSet(this.roles()));
  readonly effectivePermissions = computed<string[]>(() => this.sessionState()?.scopedPermissions ?? []);

  readonly landingPath = computed(() =>
    this.hasRoleCode('DRIVER') ? appPaths.driverShipments : appPaths.dashboard,
  );

  readonly canActAsPlatformAdmin = computed(() => {
    const authorities = decodeAuthorities(this.sessionState()?.globalAccessToken);
    return authorities.some((authority) => authority.startsWith(PLATFORM_AUTHORITY_PREFIX));
  });

  readonly authorizationHeader = computed(() => this.scopedAuthorizationHeader());
  readonly scopedAuthorizationHeader = computed(() => {
    const session = this.sessionState();
    return session?.scopedAccessToken && !this.isScopedExpired(session)
      ? `${session.scopedTokenType ?? 'Bearer'} ${session.scopedAccessToken}`
      : null;
  });
  readonly globalAuthorizationHeader = computed(() => {
    const session = this.sessionState();
    return session && !this.isGlobalExpired(session)
      ? `${session.globalTokenType} ${session.globalAccessToken}`
      : null;
  });
  readonly platformAuthorizationHeader = computed(() => {
    const session = this.sessionState();
    return session && !this.isPlatformExpired(session) && session.platformAccessToken
      ? `${session.platformTokenType ?? 'Bearer'} ${session.platformAccessToken}`
      : null;
  });

  hasPermission(permission: string): boolean {
    return this.effectivePermissions().includes(permission);
  }

  hasAnyPermission(permissions: readonly string[]): boolean {
    const held = this.effectivePermissions();
    return permissions.some((permission) => held.includes(permission));
  }

  hasAllPermissions(permissions: readonly string[]): boolean {
    const held = this.effectivePermissions();
    return permissions.every((permission) => held.includes(permission));
  }

  hasRoleCode(code: string): boolean {
    return this.roleCodes().has(code);
  }

  login(credentials: LoginRequest) {
    return this.api.authenticateUserAndIssueToken(credentials).pipe(
      map((response) => this.toSession(response)),
      tap((session) => this.setSession(session)),
    );
  }

  selectCompany(companyId: string, activeCompany?: ActiveCompany) {
    return this.api.selectActiveCompanyAndIssueScopedToken({ companyId }).pipe(
      map((response) => this.toScopedSession(response, activeCompany)),
      tap((session) => this.setSession(session)),
    );
  }

  selectPlatform() {
    return this.api.selectPlatform().pipe(
      tap((response) => this.applyPlatformToken(response)),
    );
  }

  logout(): void {
    this.setSession(null);
    void this.router.navigateByUrl(appPaths.login);
  }

  headerForUrl(url: string): string | null {
    if (url.startsWith('/api/v1/auth/login')) {
      return null;
    }

    if (url.startsWith('/api/v1/auth/')) {
      return this.globalAuthorizationHeader();
    }

    if (url.startsWith('/api/v1/platform/')) {
      return this.platformAuthorizationHeader();
    }

    return this.scopedAuthorizationHeader() ?? this.globalAuthorizationHeader();
  }

  rescope(url: string) {
    if (url.startsWith('/api/v1/platform/')) {
      return this.selectPlatform().pipe(map(() => undefined));
    }

    const session = this.sessionState();
    const companyId = session?.activeCompany?.id ?? session?.scopedActiveCompanyId;
    if (!companyId) {
      return throwError(() => new Error('No active company to re-scope against.'));
    }

    return this.selectCompany(companyId, session?.activeCompany).pipe(map(() => undefined));
  }

  private setSession(session: AuthSession | null): void {
    this.sessionState.set(session);

    if (session) {
      localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
      return;
    }

    localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
  }

  private toSession(response: LoginResponse): AuthSession {
    if (!response.accessToken) {
      throw new Error('Login response did not include an access token.');
    }

    const globalTokenType = response.tokenType ?? 'Bearer';
    const globalExpiresAt = response.expiresIn
      ? Date.now() + response.expiresIn * 1000
      : undefined;

    const baseSession: AuthSession = {
      user: response.user,
      availableCompanies: response.availableCompanies ?? [],
      globalAccessToken: response.accessToken,
      globalTokenType,
      globalExpiresAt,
      scopedRoles: [],
      scopedPermissions: [],
      platformPermissions: [],
    };

    const availableCompanies = baseSession.availableCompanies;

    if (response.scopedToken) {
      return this.applyScopedToken(baseSession, response.scopedToken, availableCompanies);
    }

    return baseSession;
  }

  private toScopedSession(
    response: SelectCompanyResponse,
    selectedCompany?: ActiveCompany,
  ): AuthSession {
    const currentSession = this.sessionState();
    if (!currentSession?.globalAccessToken) {
      throw new Error('A global login token is required to select a company.');
    }

    if (!response.accessToken) {
      throw new Error('Company selection response did not include an access token.');
    }

    if (!response.activeCompanyId) {
      throw new Error('Company selection response did not include an active company.');
    }

    const expiresAt = response.expiresIn
      ? Date.now() + response.expiresIn * 1000
      : currentSession.globalExpiresAt;

    return {
      ...currentSession,
      scopedAccessToken: response.accessToken,
      scopedTokenType: response.tokenType ?? currentSession.scopedTokenType ?? 'Bearer',
      scopedExpiresAt: expiresAt,
      scopedRoles: response.roles ?? [],
      scopedPermissions: response.effectivePermissions ?? [],
      scopedActiveCompanyId: response.activeCompanyId,
      activeCompany:
        (selectedCompany?.id === response.activeCompanyId ? selectedCompany : undefined) ??
        this.companyFromAvailableCompanies(response.activeCompanyId, currentSession.availableCompanies),
    };
  }

  private applyScopedToken(
    baseSession: AuthSession,
    scopedToken: SelectCompanyResponse,
    availableCompanies: AuthCompanyResponse[],
  ): AuthSession {
    if (!scopedToken.accessToken) {
      throw new Error('Scoped login response did not include an access token.');
    }

    if (!scopedToken.activeCompanyId) {
      throw new Error('Scoped login response did not include an active company.');
    }

    const expiresAt = scopedToken.expiresIn
      ? Date.now() + scopedToken.expiresIn * 1000
      : baseSession.globalExpiresAt;

    return {
      ...baseSession,
      scopedAccessToken: scopedToken.accessToken,
      scopedTokenType: scopedToken.tokenType ?? baseSession.globalTokenType,
      scopedExpiresAt: expiresAt,
      scopedRoles: scopedToken.roles ?? [],
      scopedPermissions: scopedToken.effectivePermissions ?? [],
      scopedActiveCompanyId: scopedToken.activeCompanyId,
      activeCompany: this.companyFromAvailableCompanies(scopedToken.activeCompanyId, availableCompanies),
    };
  }

  private applyPlatformToken(response: SelectPlatformResponse): void {
    const currentSession = this.sessionState();
    if (!currentSession?.globalAccessToken) {
      throw new Error('A global login token is required to enter platform admin mode.');
    }

    if (!response.accessToken) {
      throw new Error('Platform selection response did not include an access token.');
    }

    const platformExpiresAt = response.expiresIn
      ? Date.now() + response.expiresIn * 1000
      : currentSession.globalExpiresAt;

    this.setSession({
      ...currentSession,
      platformAccessToken: response.accessToken,
      platformTokenType: response.tokenType ?? 'Bearer',
      platformExpiresAt,
      platformRole: response.role,
      platformPermissions: response.effectivePermissions ?? [],
    });
  }

  private companyFromAvailableCompanies(
    companyId: string,
    availableCompanies: AuthCompanyResponse[],
  ): ActiveCompany {
    const company = availableCompanies.find((candidate) => candidate.companyId === companyId);

    return {
      id: companyId,
      name: company?.tradeName || company?.cnpj || 'Selected company',
    };
  }

  private readStoredSession(): AuthSession | null {
    const storedSession = localStorage.getItem(AUTH_SESSION_STORAGE_KEY);

    if (!storedSession) {
      return null;
    }

    try {
      const session = JSON.parse(storedSession) as Partial<AuthSession>;
      const restored = this.restoreSession(session);

      if (this.isGlobalExpired(restored)) {
        localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
        return null;
      }

      return restored;
    } catch {
      localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
      return null;
    }
  }

  private restoreSession(session: Partial<AuthSession>): AuthSession | null {
    if (!session.globalAccessToken || !session.globalTokenType) {
      return null;
    }

    return {
      user: session.user,
      availableCompanies: Array.isArray(session.availableCompanies) ? session.availableCompanies : [],
      activeCompany: this.toActiveCompany(session.activeCompany),
      globalAccessToken: session.globalAccessToken,
      globalTokenType: session.globalTokenType,
      globalExpiresAt: session.globalExpiresAt,
      scopedAccessToken: session.scopedAccessToken,
      scopedTokenType: session.scopedTokenType,
      scopedExpiresAt: session.scopedExpiresAt,
      scopedRoles: Array.isArray(session.scopedRoles) ? session.scopedRoles : [],
      scopedPermissions: Array.isArray(session.scopedPermissions) ? session.scopedPermissions : [],
      scopedActiveCompanyId: session.scopedActiveCompanyId,
      platformAccessToken: session.platformAccessToken,
      platformTokenType: session.platformTokenType,
      platformExpiresAt: session.platformExpiresAt,
      platformRole: session.platformRole,
      platformPermissions: Array.isArray(session.platformPermissions)
        ? session.platformPermissions
        : [],
    };
  }

  private toActiveCompany(value: unknown): ActiveCompany | undefined {
    if (!value || typeof value !== 'object') {
      return undefined;
    }

    const record = value as Record<string, unknown>;
    const id = typeof record['id'] === 'string' ? record['id'] : undefined;
    const name =
      this.stringValue(record['name']) ??
      this.stringValue(record['tradeName']) ??
      this.stringValue(record['legalName']);

    if (!id || !name) {
      return undefined;
    }

    return {
      id,
      name,
      companyCode: typeof record['companyCode'] === 'number' ? record['companyCode'] : undefined,
    };
  }

  private stringValue(value: unknown): string | undefined {
    return typeof value === 'string' && value.trim() ? value : undefined;
  }

  private isGlobalExpired(session: AuthSession | null): boolean {
    return Boolean(session?.globalExpiresAt && session.globalExpiresAt <= Date.now());
  }

  private isScopedExpired(session: AuthSession | null): boolean {
    return Boolean(session?.scopedExpiresAt && session.scopedExpiresAt <= Date.now());
  }

  private isPlatformExpired(session: AuthSession | null): boolean {
    return Boolean(session?.platformExpiresAt && session.platformExpiresAt <= Date.now());
  }
}

function buildRoleCodeSet(roles: readonly AuthRoleResponse[]): Set<string> {
  return new Set(roles.map((role) => role.code).filter((code): code is string => Boolean(code)));
}