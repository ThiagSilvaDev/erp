import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';

import { AuthStateService } from '../../../../core/auth/auth.service';
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
  private readonly auth = inject(AuthStateService);
  private readonly destroyRef = inject(DestroyRef);
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
        next: () => void this.router.navigateByUrl('/dashboard'),
        error: () => {
          this.errorMessage.set('Unable to sign in with these credentials.');
          this.isSubmitting.set(false);
        },
      });
  }
}
