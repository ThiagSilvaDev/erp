import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EMPTY, Observable, finalize, reduce, expand } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { AuthService } from '@core/auth/auth.service';
import { PERMISSIONS } from '@core/auth/permissions';
import { IamService } from '@generated-api/modules/iam/Iam.service';
import type { TenantMembershipResponse, TenantRoleResponse } from '@generated-api/modules/iam/dtos';
import { UsersService } from '@generated-api/modules/users/Users.service';
import type { PagedModelUserResponse, UserResponse } from '@generated-api/modules/users/dtos';

interface RoleOption {
  label: string;
  value: string;
}

interface UserOption {
  label: string;
  value: string;
}

const USER_PAGE_SIZE = 100;

@Component({
  selector: 'app-iam-memberships-page',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzIconModule,
    NzModalModule,
    NzSelectModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './iam-memberships-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IamMembershipsPage {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly iam = inject(IamService);
  private readonly messages = inject(NzMessageService);
  private readonly usersApi = inject(UsersService);

  readonly memberships = signal<TenantMembershipResponse[]>([]);
  readonly roleOptions = signal<RoleOption[]>([]);
  readonly userOptions = signal<UserOption[]>([]);
  readonly usersById = signal(new Map<string, UserResponse>());

  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly isCreateOpen = signal(false);
  readonly isReplaceOpen = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly actingOnId = signal<string | null>(null);
  readonly replaceMembershipId = signal<string | null>(null);

  readonly canRead = computed(() => this.auth.hasPermission(PERMISSIONS.membershipRead));
  readonly canWrite = computed(() => this.auth.hasPermission(PERMISSIONS.membershipWrite));
  readonly canAssign = computed(() => this.auth.hasPermission(PERMISSIONS.roleAssign));
  readonly canManageRoles = computed(() => this.canWrite() && this.canAssign());

  readonly availableUserOptions = computed(() => {
    const memberIds = new Set(
      this.memberships()
        .map((membership) => membership.userId)
        .filter((id): id is string => Boolean(id)),
    );
    return this.userOptions().filter((option) => !memberIds.has(option.value));
  });

  readonly createForm = this.formBuilder.group({
    userId: ['', [Validators.required]],
    roleIds: [[] as string[], [Validators.required]],
  });

  readonly replaceForm = this.formBuilder.group({
    roleIds: [[] as string[], [Validators.required]],
  });

  constructor() {
    this.loadAll();
  }

  openCreate(): void {
    this.createForm.reset({ userId: '', roleIds: [] });
    this.isCreateOpen.set(true);
  }

  closeCreate(): void {
    this.isCreateOpen.set(false);
  }

  openReplace(membership: TenantMembershipResponse): void {
    if (!membership.id) {
      return;
    }
    this.replaceMembershipId.set(membership.id);
    this.replaceForm.reset({
      roleIds: (membership.roles ?? [])
        .map((role) => role.id)
        .filter((id): id is string => Boolean(id)),
    });
    this.isReplaceOpen.set(true);
  }

  closeReplace(): void {
    this.isReplaceOpen.set(false);
    this.replaceMembershipId.set(null);
  }

  submitCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const value = this.createForm.getRawValue();
    if (!value.roleIds.length) {
      this.createForm.controls.roleIds.setErrors({ required: true });
      this.createForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.iam
      .createMembership({ userId: value.userId, roleIds: value.roleIds })
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success('Membership created.');
          this.isCreateOpen.set(false);
          this.loadMemberships();
        },
        error: (error) => this.messages.error(this.messageFrom(error, 'Unable to create membership.')),
      });
  }

  submitReplace(): void {
    const id = this.replaceMembershipId();
    if (!id || this.replaceForm.invalid) {
      this.replaceForm.markAllAsTouched();
      return;
    }

    const value = this.replaceForm.getRawValue();
    if (!value.roleIds.length) {
      this.replaceForm.controls.roleIds.setErrors({ required: true });
      this.replaceForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.iam
      .replaceMembershipRoles(id, { roleIds: value.roleIds })
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success('Membership roles updated.');
          this.closeReplace();
          this.loadMemberships();
        },
        error: (error) => this.messages.error(this.messageFrom(error, 'Unable to update membership roles.')),
      });
  }

  canActivate(membership: TenantMembershipResponse): boolean {
    return membership.status !== 'ACTIVE' && Boolean(membership.id);
  }

  canDeactivate(membership: TenantMembershipResponse): boolean {
    return membership.status === 'ACTIVE' && Boolean(membership.id);
  }

  activate(membership: TenantMembershipResponse): void {
    this.toggleMembership(membership, true);
  }

  deactivate(membership: TenantMembershipResponse): void {
    this.toggleMembership(membership, false);
  }

  private toggleMembership(membership: TenantMembershipResponse, activate: boolean): void {
    const id = membership.id;
    if (!id) {
      return;
    }
    this.actingOnId.set(id);
    const request$ = activate
      ? this.iam.activateMembership(id)
      : this.iam.deactivateMembership(id);
    request$
      .pipe(
        finalize(() => this.actingOnId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success(activate ? 'Membership activated.' : 'Membership deactivated.');
          this.loadMemberships();
        },
        error: (error) =>
          this.messages.error(
            this.messageFrom(error, activate ? 'Unable to activate membership.' : 'Unable to deactivate membership.'),
          ),
      });
  }

  userLabel(membership: TenantMembershipResponse): string {
    const user = membership.userId ? this.usersById().get(membership.userId) : undefined;
    return user?.displayName || user?.email || membership.userId || '—';
  }

  roleCodes(membership: TenantMembershipResponse): string {
    return (membership.roles ?? [])
      .map((role) => role.code || role.name || role.id || '')
      .filter(Boolean)
      .join(', ');
  }

  isActingOn(membership: TenantMembershipResponse): boolean {
    return this.actingOnId() === membership.id;
  }

  private loadAll(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.loadRoles();
    this.loadUsers();
    this.loadMemberships();
  }

  private loadRoles(): void {
    this.iam
      .listRoles()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (roles) => this.roleOptions.set(this.toRoleOptions(roles)),
        error: () => this.roleOptions.set([]),
      });
  }

  private loadMemberships(): void {
    this.iam
      .listMemberships()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (memberships) => {
          this.memberships.set(memberships ?? []);
          this.isLoading.set(false);
        },
        error: () => {
          this.memberships.set([]);
          this.isLoading.set(false);
          this.errorMessage.set('Unable to load memberships.');
        },
      });
  }

  private loadUsers(): void {
    this.listAllUsers()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (users) => {
          this.userOptions.set(
            users
              .filter((user) => Boolean(user.id))
              .map((user) => ({
                value: user.id as string,
                label: user.displayName || user.email || (user.id as string),
              })),
          );
          this.usersById.set(new Map(users.flatMap((user) => (user.id ? [[user.id, user]] : []))));
        },
        error: () => {
          this.userOptions.set([]);
          this.usersById.set(new Map());
        },
      });
  }

  private listAllUsers(): Observable<UserResponse[]> {
    return this.usersApi
      .listUsers({ page: 0, size: USER_PAGE_SIZE })
      .pipe(
        expand((page: PagedModelUserResponse) => {
          const pageNumber = page.page?.number ?? 0;
          const totalPages = page.page?.totalPages ?? 1;
          if (pageNumber + 1 >= totalPages) {
            return EMPTY;
          }
          return this.usersApi.listUsers({ page: pageNumber + 1, size: USER_PAGE_SIZE });
        }),
        reduce(
          (users, page: PagedModelUserResponse) => [...users, ...(page.content ?? [])],
          [] as UserResponse[],
        ),
      );
  }

  private toRoleOptions(roles: TenantRoleResponse[]): RoleOption[] {
    return roles
      .filter((role) => role.status !== 'INACTIVE' && role.id)
      .map((role) => ({
        value: role.id as string,
        label: role.name || role.code || (role.id as string),
      }));
  }

  private messageFrom(error: unknown, fallback: string): string {
    const extracted = extractServerMessage(error);
    return extracted ? extracted : fallback;
  }
}

function extractServerMessage(error: unknown): string | null {
  if (!(error instanceof HttpErrorResponse)) {
    return null;
  }

  const body = error.error;
  if (typeof body === 'string' && body.trim()) {
    return body.trim();
  }

  if (body && typeof body === 'object') {
    const record = body as Record<string, unknown>;
    const candidates = [record['message'], record['error'], record['detail'], record['title']];
    for (const candidate of candidates) {
      if (typeof candidate === 'string' && candidate.trim()) {
        return candidate.trim();
      }
    }
  }

  return null;
}