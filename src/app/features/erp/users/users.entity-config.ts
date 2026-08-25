import { inject } from '@angular/core';
import { map } from 'rxjs';

import { UsersService } from '@generated-api/modules/users/Users.service';
import { activeStatusOptions } from '../entity/config/entity-field-options';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData } from '../entity/utils/entity-utils';

export function usersEntityConfig(): EntityConfig {
  const users = inject(UsersService);
  return {
    key: 'users',
    title: 'Users',
    description: 'Application users and tenant memberships.',
    createMode: 'page',
    columns: [
      { key: 'displayName', label: 'Name' },
      { key: 'email', label: 'Email' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'displayName', label: 'Display name' },
      { key: 'email', label: 'Email' },
      { key: 'phone', label: 'Phone' },
      { key: 'birthDate', label: 'Birth date' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [
      { key: 'name', label: 'Name' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'status', label: 'Status', type: 'select', options: activeStatusOptions },
    ],
    createFields: [],
    list: (criteria, page, size) => users.listUsers({ ...criteria, page, size }).pipe(map(pageData)),
    getById: (id) => users.getUserById(id),
  };
}

export const provideUsersEntityConfig = () =>
  provideEntityFeature({ config: usersEntityConfig });