import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzSwitchModule } from 'ng-zorro-antd/switch';

import { AuthService } from '@core/auth/auth.service';
import { KNOWN_PERMISSIONS, PERMISSIONS } from '@core/auth/permissions';
import { appPaths } from '@core/routing/app-paths';
import { IamService } from '@generated-api/modules/iam/Iam.service';
import {
  type PermissionGroup,
  buildPermissionGroups,
  isReadPermission,
  isWritePermission,
  readCounterpart,
  sanitizePermissions,
  writeCounterpart,
} from '../../utils/permissions';

@Component({
  selector: 'app-role-editor-page',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzSpinModule,
    NzSwitchModule,
  ],
  templateUrl: './role-editor-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleEditorPage {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly iam = inject(IamService);
  private readonly messages = inject(NzMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly roleId = signal<string | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly membershipPermissions = signal<string[]>([]);
  readonly filter = signal('');
  readonly selectedPermissions = signal<string[]>([]);

  readonly nameControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required],
  });
  readonly descriptionControl = new FormControl('', { nonNullable: true });

  readonly isEdit = computed(() => Boolean(this.roleId()));
  readonly canWrite = computed(() => this.auth.hasPermission(PERMISSIONS.roleWrite));
  readonly pageTitle = computed(() => (this.isEdit() ? 'Edit role' : 'New role'));
  readonly selectedCount = computed(() => this.selectedPermissions().length);

  readonly permissionGroups = computed<PermissionGroup[]>(() => {
    const codes = new Set<string>(KNOWN_PERMISSIONS);
    for (const permission of this.membershipPermissions()) {
      codes.add(permission);
    }
    for (const permission of this.selectedPermissions()) {
      codes.add(permission);
    }

    const filter = this.filter().trim().toLowerCase();
    return buildPermissionGroups(codes)
      .map((group) =>
        filter
          ? {
              label: group.label,
              options: group.options.filter((option) => option.toLowerCase().includes(filter)),
            }
          : group,
      )
      .filter((group) => group.options.length);
  });

  constructor() {
    const id = this.route.snapshot.paramMap.get('roleId');
    this.roleId.set(id);
    this.loadMembershipPermissions();
    if (id) {
      this.loadRole(id);
    }
  }

  isSelected(code: string): boolean {
    return this.selectedPermissions().includes(code);
  }

  togglePermission(code: string, checked: boolean): void {
    const next = new Set(this.selectedPermissions());

    if (checked) {
      next.add(code);
      if (isWritePermission(code)) {
        next.add(readCounterpart(code));
      }
    } else {
      next.delete(code);
      if (isReadPermission(code)) {
        next.delete(writeCounterpart(code));
      }
    }

    this.selectedPermissions.set(sanitizePermissions([...next]));
  }

  isGroupAllSelected(group: PermissionGroup): boolean {
    const selected = this.selectedPermissions();
    return group.options.length > 0 && group.options.every((option) => selected.includes(option));
  }

  setGroupSelected(group: PermissionGroup, checked: boolean): void {
    const next = new Set(this.selectedPermissions());

    for (const code of group.options) {
      if (checked) {
        next.add(code);
        if (isWritePermission(code)) {
          next.add(readCounterpart(code));
        }
      } else {
        next.delete(code);
        if (isReadPermission(code)) {
          next.delete(writeCounterpart(code));
        }
      }
    }

    this.selectedPermissions.set(sanitizePermissions([...next]));
  }

  selectedCountIn(group: PermissionGroup): number {
    const selected = this.selectedPermissions();
    return group.options.filter((option) => selected.includes(option)).length;
  }

  submit(): void {
    if (!this.canWrite()) {
      return;
    }

    if (this.nameControl.invalid) {
      this.nameControl.markAsTouched();
      return;
    }

    const name = this.nameControl.value.trim();
    const description = this.descriptionControl.value.trim() || undefined;
    const permissions = sanitizePermissions(this.selectedPermissions());
    this.isSaving.set(true);

    const request$ = this.roleId()
      ? this.iam.updateRole(this.roleId() as string, { name, description, permissions })
      : this.iam.createRole({ name, description, permissions });

    request$
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success(this.roleId() ? 'Role updated.' : 'Role created.');
          void this.router.navigateByUrl(appPaths.iamRoles);
        },
        error: () => this.messages.error('Unable to save role.'),
      });
  }

  cancel(): void {
    void this.router.navigateByUrl(appPaths.iamRoles);
  }

  private loadRole(id: string): void {
    this.isLoading.set(true);
    this.iam
      .listRoles()
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (roles) => {
          const role = (roles ?? []).find((candidate) => candidate.id === id);
          if (!role) {
            this.errorMessage.set('Role not found.');
            return;
          }
          this.nameControl.setValue(role.name ?? '');
          this.descriptionControl.setValue(role.description ?? '');
          this.selectedPermissions.set(sanitizePermissions(role.permissions ?? []));
        },
        error: () => this.errorMessage.set('Unable to load role.'),
      });
  }

  private loadMembershipPermissions(): void {
    this.iam
      .listMemberships()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (memberships) =>
          this.membershipPermissions.set(
            (memberships ?? []).flatMap((membership) => membership.effectivePermissions ?? []),
          ),
        error: () => this.membershipPermissions.set([]),
      });
  }
}