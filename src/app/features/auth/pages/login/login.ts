import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '@core/auth/auth.service';
import { appPaths } from '@core/routing/app-paths';
import { LoginFormCard } from '../../components/login-form-card/login-form-card';
import { LoginForm } from '../../models/login-form';

@Component({
  selector: 'app-login',
  imports: [LoginFormCard],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(credentials: LoginForm): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.auth
      .login(credentials)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => void this.router.navigateByUrl(this.nextPath()),
        error: () => {
          this.errorMessage.set('Unable to sign in with these credentials.');
          this.isSubmitting.set(false);
        },
      });
  }

  private nextPath(): string {
    if (!this.auth.hasActiveCompany()) {
      return appPaths.selectCompany;
    }

    return this.route.snapshot.queryParamMap.get('returnUrl') ?? this.auth.landingPath();
  }
}
