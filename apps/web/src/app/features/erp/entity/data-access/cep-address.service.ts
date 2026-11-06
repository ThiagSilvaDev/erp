import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';

export interface CepAddress {
  street: string;
  district: string;
  city: string;
  region: string;
  postalCode: string;
  country: string;
}

interface ViaCepResponse {
  cep?: string;
  logradouro?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
  erro?: boolean;
}

@Injectable({ providedIn: 'root' })
export class CepAddressService {
  private readonly http = inject(HttpClient);

  lookup(cep: string) {
    const normalized = this.normalizeCep(cep);

    return this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${normalized}/json/`).pipe(
      map((response) => {
        if (response.erro) {
          throw new Error('CEP not found.');
        }

        return {
          street: response.logradouro ?? '',
          district: response.bairro ?? '',
          city: response.localidade ?? '',
          region: response.uf ?? '',
          postalCode: response.cep ?? normalized,
          country: 'BR',
        } satisfies CepAddress;
      }),
    );
  }

  normalizeCep(cep: string): string {
    return cep.replace(/\D/g, '');
  }
}
