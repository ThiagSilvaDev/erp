import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, tap } from 'rxjs';

import { AuthenticationService } from '@generated-api/modules/authentication/Authentication.service';
import type {
  AuthCompanyResponse,
  LoginRequest,
  LoginResponse,
  SelectCompanyResponse as ApiSelectCompanyResponse,
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
  expiresIn?: number;
  expiresAt?: number;
  user?: LoginResponse['user'];
  role?: string;
  availableCompanies: AuthCompanyResponse[];
  activeCompany?: ActiveCompany;
}

const AUTH_SESSION_STORAGE_KEY = 'erp.auth.session';

interface SelectCompanyRequest {
  companyId: string;
}

interface SelectCompanyResponse extends ApiSelectCompanyResponse {
  activeCompany?: Partial<ActiveCompany>;
  company?: Partial<ActiveCompany>;
  companyId?: string;
  companyName?: string;
  tradeName?: string;
  legalName?: string;
  companyCode?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthenticationService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly sessionState = signal<AuthSession | null>(this.readStoredSession());

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => {
    const session = this.sessionState();
    return Boolean(session?.accessToken) && !this.isSessionExpired(session);
  });
  readonly hasActiveCompany = computed(() => Boolean(this.sessionState()?.activeCompany?.id));
  readonly availableCompanies = computed(() => this.sessionState()?.availableCompanies ?? []);
  readonly authorizationHeader = computed(() => {
    const session = this.sessionState();
    return session && !this.isSessionExpired(session)
      ? `${session.tokenType} ${session.accessToken}`
      : null;
  });
  readonly globalAuthorizationHeader = computed(() => {
    const session = this.sessionState();
    return session && !this.isSessionExpired(session)
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
    return this.http
      .post<SelectCompanyResponse>('/api/v1/auth/select-company', {
        companyId,
      } satisfies SelectCompanyRequest)
      .pipe(
        map((response) => this.toScopedSession(response, companyId, activeCompany)),
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

    return {
      accessToken: response.accessToken,
      tokenType: response.tokenType ?? 'Bearer',
      globalAccessToken: response.accessToken,
      globalTokenType: response.tokenType ?? 'Bearer',
      expiresIn: response.expiresIn,
      expiresAt: response.expiresIn ? Date.now() + response.expiresIn * 1000 : undefined,
      user: response.user,
      role: response.user?.role,
      availableCompanies: response.availableCompanies ?? [],
      activeCompany: this.activeCompanyFromResponse(response),
    };
  }

  private toScopedSession(
    response: SelectCompanyResponse,
    requestedCompanyId: string,
    selectedCompany?: ActiveCompany,
  ): AuthSession {
    const currentSession = this.sessionState();
    if (!currentSession?.globalAccessToken) {
      throw new Error('A global login token is required to select a company.');
    }

    if (!response.accessToken) {
      throw new Error('Company selection response did not include an access token.');
    }

    const tokenType = response.tokenType ?? currentSession.tokenType;
    const expiresIn = response.expiresIn ?? currentSession.expiresIn;
    const expiresAt = expiresIn ? Date.now() + expiresIn * 1000 : currentSession.expiresAt;

    return {
      accessToken: response.accessToken,
      tokenType,
      globalAccessToken: currentSession.globalAccessToken,
      globalTokenType: currentSession.globalTokenType,
      expiresIn,
      expiresAt,
      user: currentSession.user,
      role: response.role ?? currentSession.role,
      availableCompanies: currentSession.availableCompanies,
      activeCompany:
        this.activeCompanyFromResponse(response) ??
        selectedCompany ??
        this.toActiveCompany({
          id: requestedCompanyId,
          name:
            response.companyName ??
            response.tradeName ??
            response.legalName ??
            'Selected company',
          companyCode: response.companyCode,
        }),
    };
  }

  private activeCompanyFromResponse(response: SelectCompanyResponse): ActiveCompany | undefined {
    return (
      this.toActiveCompany(response.activeCompany) ??
      this.toActiveCompany(response.company) ??
      this.toActiveCompany({
        id: response.activeCompanyId ?? response.companyId,
        name: response.companyName ?? response.tradeName ?? response.legalName,
        companyCode: response.companyCode,
      })
    );
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
      companyCode:
        typeof record['companyCode'] === 'number' ? record['companyCode'] : undefined,
    };
  }

  private stringValue(value: unknown): string | undefined {
    return typeof value === 'string' && value.trim() ? value : undefined;
  }

  private isSessionExpired(session: AuthSession | null): boolean {
    return Boolean(session?.expiresAt && session.expiresAt <= Date.now());
  }
}
