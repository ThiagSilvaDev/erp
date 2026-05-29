import { InjectionToken } from '@angular/core';

import { EntityConfig, EntityOptionLoader } from '../models/entity-view-model';

export const ENTITY_CONFIGS = new InjectionToken<readonly EntityConfig[]>('ENTITY_CONFIGS');
export const ENTITY_OPTION_LOADERS = new InjectionToken<readonly EntityOptionLoader[]>(
  'ENTITY_OPTION_LOADERS',
);
