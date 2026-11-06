import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzSelectModule } from 'ng-zorro-antd/select';

import { AuthService } from '@core/auth/auth.service';
import { PERMISSIONS } from '@core/auth/permissions';
import { IamService } from '@generated-api/modules/iam/Iam.service';
import type { TenantRoleResponse } from '@generated-api/modules/iam/dtos';
import { UsersService } from '@generated-api/modules/users/Users.service';

interface RoleOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-user-create-page',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDatePickerModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
  ],
  templateUrl: './user-create-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserCreatePage {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly iam = inject(IamService);
  private readonly messages = inject(NzMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly users = inject(UsersService);

  readonly roleOptions = signal<RoleOption[]>([]);
  readonly isLoadingRoles = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly canCreate = computed(() =>
    this.auth.hasAllPermissions([PERMISSIONS.userWrite, PERMISSIONS.roleAssign]),
  );

  readonly form = this.formBuilder.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    birthDate: [null as Date | null],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    roleIds: [[] as string[], [Validators.required]],
  });

  constructor() {
    this.loadRoles();
  }

  private loadRoles(): void {
    this.isLoadingRoles.set(true);
    this.iam
      .listRoles()
      .pipe(
        finalize(() => this.isLoadingRoles.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (roles) => this.roleOptions.set(this.toRoleOptions(roles)),
        error: () => {
          this.errorMessage.set('Unable to load tenant roles.');
          this.messages.error('Unable to load tenant roles.');
        },
      });
  }

  private toRoleOptions(roles: TenantRoleResponse[]): RoleOption[] {
    return roles
      .filter((role) => role.status !== 'INACTIVE' && role.id)
      .map((role) => ({
        value: role.id as string,
        label: role.name || role.code || (role.id as string),
      }));
  }

  submit(): void {
    if (!this.canCreate()) {
      this.errorMessage.set('You need user:write and iam:role:assign permissions to create users.');
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (!value.roleIds.length) {
      this.form.controls.roleIds.setErrors({ required: true });
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    this.users
      .createUser({
        firstName: value.firstName.trim(),
        lastName: value.lastName.trim(),
        email: value.email.trim(),
        phone: value.phone.trim() || undefined,
        birthDate: value.birthDate ? this.toIsoDate(value.birthDate) : undefined,
        password: value.password,
        roleIds: value.roleIds,
      })
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success('User created.');
          void this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: () => this.errorMessage.set('Unable to create user.'),
      });
  }

  cancel(): void {
    void this.router.navigate(['../'], { relativeTo: this.route });
  }

  private toIsoDate(value: Date | string): string {
    const date = value instanceof Date ? value : new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toISOString().slice(0, 10);
  }
}