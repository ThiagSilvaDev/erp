import { inject } from '@angular/core';
import { map } from 'rxjs';

import { UsersService } from '@generated-api/modules/users/Users.service';
import { activeStatusOptions, roleOptions } from '../entity/config/entity-field-options';
import { CompaniesOptionLoader } from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData, text } from '../entity/utils/entity-utils';

export function usersEntityConfig(): EntityConfig {
  const users = inject(UsersService);
  return {
    key: 'users',
    title: 'Users',
    description: 'Application users and roles.',
    createMode: 'page',
    columns: [
      { key: 'displayName', label: 'Name' },
      { key: 'email', label: 'Email' },
      { key: 'role', label: 'Role' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'displayName', label: 'Display name' },
      { key: 'email', label: 'Email' },
      { key: 'phone', label: 'Phone' },
      { key: 'birthDate', label: 'Birth date' },
      { key: 'role', label: 'Role' },
      { key: 'status', label: 'Status' },
      { key: 'companyId', label: 'Company ID' },
    ],
    searchFields: [
      { key: 'name', label: 'Name' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'role', label: 'Role', type: 'select', options: roleOptions },
      { key: 'status', label: 'Status', type: 'select', options: activeStatusOptions },
      { key: 'companyId', label: 'Company', type: 'select', optionsKey: 'companies' },
    ],
    createFields: [
      { key: 'firstName', label: 'First name', required: true, section: 'Profile' },
      { key: 'lastName', label: 'Last name', required: true, section: 'Profile' },
      { key: 'email', label: 'Email', required: true, type: 'email', section: 'Profile' },
      { key: 'phone', label: 'Phone', section: 'Profile' },
      { key: 'birthDate', label: 'Birth date', type: 'date', section: 'Profile' },
      {
        key: 'role',
        label: 'Role',
        required: true,
        type: 'select',
        options: roleOptions,
        section: 'Access',
      },
      {
        key: 'companyId',
        label: 'Company',
        required: true,
        type: 'select',
        optionsKey: 'companies',
        section: 'Access',
      },
      { key: 'password', label: 'Password', required: true, type: 'password', section: 'Access' },
    ],
    list: (criteria, page, size) => users.listUsers({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) =>
      users.createUser({
        firstName: text(value['firstName']),
        lastName: text(value['lastName']),
        email: text(value['email']),
        phone: text(value['phone']) || undefined,
        birthDate: text(value['birthDate']) || undefined,
        role: text(value['role']) === 'MANAGER' ? 'MANAGER' : 'ADMIN',
        companyId: text(value['companyId']),
        password: text(value['password']),
      }),
    getById: (id) => users.getUserById(id),
  };
}

export const provideUsersEntityConfig = () =>
  provideEntityFeature({ config: usersEntityConfig, optionLoaders: [CompaniesOptionLoader] });
