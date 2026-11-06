import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { EntityConfig } from '../models/entity-view-model';
import { ENTITY_CONFIGS } from '../tokens/entity.tokens';
import { EntityRegistryService } from './entity-registry.service';

const companiesConfig: EntityConfig = {
  key: 'companies',
  title: 'Companies',
  description: 'Legal entities.',
  columns: [],
  detailFields: [],
  searchFields: [],
  createFields: [],
  list: () => of({ content: [], totalElements: 0 }),
  getById: (id) => of({ id }),
};

describe('EntityRegistryService', () => {
  it('returns registered entity configs by key', () => {
    TestBed.configureTestingModule({
      providers: [
        EntityRegistryService,
        { provide: ENTITY_CONFIGS, multi: true, useValue: companiesConfig },
      ],
    });

    const registry = TestBed.inject(EntityRegistryService);

    expect(registry.getConfig('companies')).toBe(companiesConfig);
  });

  it('throws a clear error for missing entity configs', () => {
    TestBed.configureTestingModule({
      providers: [
        EntityRegistryService,
        { provide: ENTITY_CONFIGS, multi: true, useValue: companiesConfig },
      ],
    });

    const registry = TestBed.inject(EntityRegistryService);

    expect(() => registry.getConfig('sales-orders')).toThrow(
      "Configuration for entity 'sales-orders' not found.",
    );
  });
});
