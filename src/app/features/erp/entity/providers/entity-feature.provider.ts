import { Provider } from '@angular/core';

import { EntityOptionsFacade } from '../data-access/entity-options.facade';
import { EntityRegistryService } from '../data-access/entity-registry.service';
import { EntityFeatureProviderConfig } from '../models/entity-view-model';
import { ENTITY_CONFIGS, ENTITY_OPTION_LOADERS } from '../tokens/entity.tokens';

export function provideEntityFeature(feature: EntityFeatureProviderConfig): Provider[] {
  return [
    {
      provide: ENTITY_CONFIGS,
      multi: true,
      ...(typeof feature.config === 'function'
        ? { useFactory: feature.config }
        : { useValue: feature.config }),
    },
    ...(feature.optionLoaders ?? []).map((loader) => ({
      provide: ENTITY_OPTION_LOADERS,
      multi: true,
      useClass: loader,
    })),
    EntityRegistryService,
    EntityOptionsFacade,
  ];
}
