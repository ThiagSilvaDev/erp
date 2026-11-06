import { Injectable, inject } from '@angular/core';

import { EntityConfig, EntityKey } from '../models/entity-view-model';
import { ENTITY_CONFIGS } from '../tokens/entity.tokens';

@Injectable()
export class EntityRegistryService {
  private readonly configs = inject(ENTITY_CONFIGS);
  private readonly configMap = new Map<EntityKey, EntityConfig>(
    this.configs.map((config) => [config.key, config]),
  );

  getConfig(key: EntityKey): EntityConfig {
    const config = this.configMap.get(key);

    if (!config) {
      throw new Error(`Configuration for entity '${key}' not found.`);
    }

    return config;
  }
}
