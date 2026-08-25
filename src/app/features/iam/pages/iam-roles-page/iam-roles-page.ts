import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { AuthService } from '@core/auth/auth.service';
import { PERMISSIONS } from '@core/auth/permissions';
import { appPaths } from '@core/routing/app-paths';
import { IamService } from '@generated-api/modules/iam/Iam.service';
import type { TenantRoleResponse } from '@generated-api/modules/iam/dtos';
import { TenantRoleResponseType } from '@generated-api/modules/iam/dtos';
import {
  domainOf,
  isReadPermission,
  isWritePermission,
  prettifyModule,
} from '../../utils/permissions';

interface RolePermissionSummary {
  readModules: string[];
  writeModules: string[];
  extraCodes: string[];
}

@Component({
  selector: 'app-iam-roles-page',
  imports: [
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzIconModule,
    NzModalModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './iam-roles-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IamRolesPage {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly iam = inject(IamService);
  private readonly messages = inject(NzMessageService);
  private readonly router = inject(Router);

  readonly roles = signal<TenantRoleResponse[]>([]);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly pendingDeleteId = signal<string | null>(null);

  private readonly summariesByRoleId = computed(() => {
    const map = new Map<string, RolePermissionSummary>();
    for (const role of this.roles()) {
      map.set(role.id ?? '', toPermissionSummary(role));
    }
    return map;
  });

  readonly canWrite = computed(() => this.auth.hasPermission(PERMISSIONS.roleWrite));

  constructor() {
    this.loadRoles();
  }

  openCreate(): void {
    void this.router.navigateByUrl(appPaths.iamRoleCreate);
  }

  openEdit(role: TenantRoleResponse): void {
    if (!role.id) {
      return;
    }
    void this.router.navigateByUrl(appPaths.iamRoleEdit(role.id));
  }

  isBuiltIn(role: TenantRoleResponse): boolean {
    return role.type === TenantRoleResponseType.BUILT_IN;
  }

  summaryFor(role: TenantRoleResponse): RolePermissionSummary {
    return this.summariesByRoleId().get(role.id ?? '') ?? {
      readModules: [],
      writeModules: [],
      extraCodes: [],
    };
  }

  confirmDelete(role: TenantRoleResponse): void {
    if (!role.id || this.isBuiltIn(role)) {
      return;
    }
    this.pendingDeleteId.set(role.id);
  }

  cancelDelete(): void {
    this.pendingDeleteId.set(null);
  }

  deleteRole(): void {
    const id = this.pendingDeleteId();
    if (!id) {
      return;
    }

    this.iam
      .deleteRole(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.messages.success('Role deleted.');
          this.pendingDeleteId.set(null);
          this.loadRoles();
        },
        error: () => {
          this.messages.error('Unable to delete role.');
          this.pendingDeleteId.set(null);
        },
      });
  }

  private loadRoles(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.iam
      .listRoles()
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (roles) => this.roles.set(roles ?? []),
        error: () => {
          this.roles.set([]);
          this.errorMessage.set('Unable to load roles.');
        },
      });
  }
}

function toPermissionSummary(role: TenantRoleResponse): RolePermissionSummary {
  const permissions = role.permissions ?? [];
  const readModules = new Set<string>();
  const writeModules = new Set<string>();
  const extraCodes = new Set<string>();

  for (const permission of permissions) {
    if (isReadPermission(permission)) {
      readModules.add(prettifyModule(domainOf(permission)));
    } else if (isWritePermission(permission)) {
      writeModules.add(prettifyModule(domainOf(permission)));
    } else {
      extraCodes.add(permission);
    }
  }

  return {
    readModules: [...readModules].sort((a, b) => a.localeCompare(b)),
    writeModules: [...writeModules].sort((a, b) => a.localeCompare(b)),
    extraCodes: [...extraCodes].sort((a, b) => a.localeCompare(b)),
  };
}