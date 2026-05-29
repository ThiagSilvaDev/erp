import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';

import {
  EntityOptionLoader,
  EntityOptionsKey,
  EntitySelectOption,
  EntitySelectOptions,
} from '../models/entity-view-model';
import { ENTITY_OPTION_LOADERS } from '../tokens/entity.tokens';

@Injectable()
export class EntityOptionsFacade {
  private readonly loaders = inject(ENTITY_OPTION_LOADERS, {
    optional: true,
  });

  loadOptions(keys: readonly EntityOptionsKey[]) {
    const uniqueKeys = [...new Set(keys)];

    if (!uniqueKeys.length) {
      return of({} satisfies EntitySelectOptions);
    }

    const loaderMap = new Map<EntityOptionsKey, EntityOptionLoader>(
      (this.loaders ?? []).map((loader) => [loader.key, loader]),
    );
    const requests = uniqueKeys.reduce<
      Partial<Record<EntityOptionsKey, Observable<EntitySelectOption[]>>>
    >((current, key) => {
      const loader = loaderMap.get(key);

      if (!loader) {
        throw new Error(`Option loader for '${key}' not found.`);
      }

      return {
        ...current,
        [key]: loader.load().pipe(catchError(() => of([]))),
      };
    }, {});

    return forkJoin(requests).pipe(map((options): EntitySelectOptions => options));
  }
}
