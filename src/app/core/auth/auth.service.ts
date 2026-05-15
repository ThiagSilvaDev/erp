import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, tap } from 'rxjs';

import { AuthenticationService } from '../../api/modules/authentication/authentication.service';
import type { LoginRequest, LoginResponse } from '../../api/modules/authentication/dtos';

const sessionKey = 'erp.session';

export interface AuthSession {
  accessToken: string;
  tokenType: string;
  expiresIn?: number;
  user?: LoginResponse['user'];
}

function readSession(): AuthSession | null {
  const raw = localStorage.getItem(sessionKey);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<AuthSession>;
    return parsed.accessToken
      ? {
          accessToken: parsed.accessToken,
          tokenType: parsed.tokenType ?? 'Bearer',
          expiresIn: parsed.expiresIn,
          user: parsed.user,
        }
      : null;
  } catch {
    localStorage.removeItem(sessionKey);
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly api = inject(AuthenticationService);
  private readonly router = inject(Router);
  private readonly sessionState = signal<AuthSession | null>(readSession());

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.sessionState()?.accessToken));
  readonly authorizationHeader = computed(() => {
    const session = this.sessionState();
    return session ? `${session.tokenType} ${session.accessToken}` : null;
  });

  login(credentials: LoginRequest) {
    return this.api.authenticateUserAndIssueToken(credentials).pipe(
      map((response) => this.toSession(response)),
      tap((session) => {
        localStorage.setItem(sessionKey, JSON.stringify(session));
        this.sessionState.set(session);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(sessionKey);
    this.sessionState.set(null);
    void this.router.navigateByUrl('/login');
  }

  private toSession(response: LoginResponse): AuthSession {
    if (!response.accessToken) {
      throw new Error('Login response did not include an access token.');
    }

    return {
      accessToken: response.accessToken,
      tokenType: response.tokenType ?? 'Bearer',
      expiresIn: response.expiresIn,
      user: response.user,
    };
  }
}
