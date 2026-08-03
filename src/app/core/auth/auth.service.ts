import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, tap } from 'rxjs';

import { appPaths } from '@core/routing/app-paths';
import { AuthenticationService } from '@generated-api/modules/authentication/Authentication.service';
import type {
  AuthCompanyResponse,
  LoginRequest,
  LoginResponse,
  SelectCompanyResponse,
  SelectCompanyResponseRole,
} from '@generated-api/modules/authentication/dtos';

export interface ActiveCompany {
  id: string;
  name: string;
  companyCode?: number;
}

export interface AuthSession {
  accessToken: string;
  tokenType: string;
  globalAccessToken: string;
  globalTokenType: string;
  globalExpiresAt?: number;
  expiresIn?: number;
  expiresAt?: number;
  user?: LoginResponse['user'];
  role?: SelectCompanyResponseRole;
  availableCompanies: AuthCompanyResponse[];
  activeCompany?: ActiveCompany;
}

const AUTH_SESSION_STORAGE_KEY = 'erp.auth.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthenticationService);
  private readonly router = inject(Router);
  private readonly sessionState = signal<AuthSession | null>(this.readStoredSession());

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => {
    const session = this.sessionState();
    return Boolean(session?.accessToken) && !this.isSessionExpired(session);
  });
  readonly hasActiveCompany = computed(() => Boolean(this.sessionState()?.activeCompany?.id));
  readonly role = computed(() => this.sessionState()?.role);
  readonly isDriver = computed(() => this.role() === 'DRIVER');
  readonly landingPath = computed(() =>
    this.isDriver() ? appPaths.driverShipments : appPaths.dashboard,
  );
  readonly availableCompanies = computed(() => this.sessionState()?.availableCompanies ?? []);
  readonly authorizationHeader = computed(() => {
    const session = this.sessionState();
    return session && !this.isSessionExpired(session)
      ? `${session.tokenType} ${session.accessToken}`
      : null;
  });
  readonly globalAuthorizationHeader = computed(() => {
    const session = this.sessionState();
    return session && !this.isGlobalTokenExpired(session)
      ? `${session.globalTokenType} ${session.globalAccessToken}`
      : null;
  });

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

  logout(): void {
    this.setSession(null);
    void this.router.navigateByUrl('/login');
  }

  private setSession(session: AuthSession | null): void {
    this.sessionState.set(session);

    if (session) {
      localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
      return;
    }

    localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
  }

  private readStoredSession(): AuthSession | null {
    const storedSession = localStorage.getItem(AUTH_SESSION_STORAGE_KEY);

    if (!storedSession) {
      return null;
    }

    try {
      const session = JSON.parse(storedSession) as Partial<AuthSession>;
      const restoredSession =
        session.accessToken && session.tokenType
          ? {
              accessToken: session.accessToken,
              tokenType: session.tokenType,
              globalAccessToken: this.stringValue(session.globalAccessToken) ?? session.accessToken,
              globalTokenType: this.stringValue(session.globalTokenType) ?? session.tokenType,
              globalExpiresAt: session.globalExpiresAt ?? session.expiresAt,
              expiresIn: session.expiresIn,
              expiresAt: session.expiresAt,
              user: session.user,
              role: session.role,
              availableCompanies: Array.isArray(session.availableCompanies)
                ? session.availableCompanies
                : [],
              activeCompany: this.toActiveCompany(session.activeCompany),
            }
          : null;

      if (this.isSessionExpired(restoredSession)) {
        localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
        return null;
      }

      return restoredSession;
    } catch {
      localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
      return null;
    }
  }

  private toSession(response: LoginResponse): AuthSession {
    if (!response.accessToken) {
      throw new Error('Login response did not include an access token.');
    }

    const globalTokenType = response.tokenType ?? 'Bearer';
    const globalExpiresAt = response.expiresIn ? Date.now() + response.expiresIn * 1000 : undefined;
    const baseSession: AuthSession = {
      accessToken: response.accessToken,
      tokenType: globalTokenType,
      globalAccessToken: response.accessToken,
      globalTokenType,
      globalExpiresAt,
      expiresIn: response.expiresIn,
      expiresAt: globalExpiresAt,
      user: response.user,
      availableCompanies: response.availableCompanies ?? [],
    };

    if (!response.scopedToken) {
      return baseSession;
    }

    return this.sessionFromScopedToken(baseSession, response.scopedToken);
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

    if (!response.role) {
      throw new Error('Company selection response did not include a role.');
    }

    const tokenType = response.tokenType ?? currentSession.tokenType;
    const expiresIn = response.expiresIn ?? currentSession.expiresIn;
    const expiresAt = expiresIn ? Date.now() + expiresIn * 1000 : currentSession.expiresAt;

    return {
      accessToken: response.accessToken,
      tokenType,
      globalAccessToken: currentSession.globalAccessToken,
      globalTokenType: currentSession.globalTokenType,
      globalExpiresAt: currentSession.globalExpiresAt,
      expiresIn,
      expiresAt,
      user: currentSession.user,
      role: response.role,
      availableCompanies: currentSession.availableCompanies,
      activeCompany:
        (selectedCompany?.id === response.activeCompanyId ? selectedCompany : undefined) ??
        this.companyFromAvailableCompanies(
          response.activeCompanyId,
          currentSession.availableCompanies,
        ),
    };
  }

  private sessionFromScopedToken(
    baseSession: AuthSession,
    scopedToken: SelectCompanyResponse,
  ): AuthSession {
    if (!scopedToken.accessToken) {
      throw new Error('Scoped login response did not include an access token.');
    }

    if (!scopedToken.activeCompanyId) {
      throw new Error('Scoped login response did not include an active company.');
    }

    if (!scopedToken.role) {
      throw new Error('Scoped login response did not include a role.');
    }

    const expiresIn = scopedToken.expiresIn ?? baseSession.expiresIn;

    return {
      ...baseSession,
      accessToken: scopedToken.accessToken,
      tokenType: scopedToken.tokenType ?? baseSession.tokenType,
      expiresIn,
      expiresAt: expiresIn ? Date.now() + expiresIn * 1000 : baseSession.expiresAt,
      role: scopedToken.role,
      activeCompany: this.companyFromAvailableCompanies(
        scopedToken.activeCompanyId,
        baseSession.availableCompanies,
      ),
    };
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

  private isSessionExpired(session: AuthSession | null): boolean {
    return Boolean(session?.expiresAt && session.expiresAt <= Date.now());
  }

  private isGlobalTokenExpired(session: AuthSession | null): boolean {
    return Boolean(session?.globalExpiresAt && session.globalExpiresAt <= Date.now());
  }
}
