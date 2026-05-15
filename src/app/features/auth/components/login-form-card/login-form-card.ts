import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';

import { LoginForm } from '../../models/login-form';

@Component({
  selector: 'app-login-form-card',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzInputModule,
  ],
  templateUrl: './login-form-card.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginFormCard {
  private readonly formBuilder = inject(NonNullableFormBuilder);

  readonly errorMessage = input<string | null>(null);
  readonly isSubmitting = input(false);
  readonly submitted = output<LoginForm>();

  readonly form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitted.emit(this.form.getRawValue());
  }
}
