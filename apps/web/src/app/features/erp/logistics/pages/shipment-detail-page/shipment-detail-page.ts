import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import type {
  DriverResponse,
  ShipmentResponse,
  VehicleResponse,
} from '@generated-api/modules/logistics/dtos';
import { formatTimestamp } from '../../utils/timestamp-format';

interface DetailField {
  label: string;
  value: string;
}

@Component({
  selector: 'app-shipment-detail-page',
  imports: [
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzDescriptionsModule,
    NzIconModule,
    NzSpinModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './shipment-detail-page.html',
  styleUrl: './shipment-detail-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShipmentDetailPage {
  private readonly logistics = inject(LogisticsService);
  private readonly route = inject(ActivatedRoute);

  readonly shipment = signal<ShipmentResponse | null>(null);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly shipmentFields = computed<DetailField[]>(() => {
    const shipment = this.shipment();
    if (!shipment) {
      return [];
    }

    return [
      { label: 'Shipment number', value: this.text(shipment.formattedShipmentNumber) },
      { label: 'Status', value: this.text(shipment.status) },
      { label: 'Route', value: this.text(shipment.routeName) },
      { label: 'Sales order ID', value: this.text(shipment.salesOrderId) },
      { label: 'Seller company ID', value: this.text(shipment.sellerCompanyId) },
      { label: 'Buyer company ID', value: this.text(shipment.buyerCompanyId) },
      { label: 'Customer ID', value: this.text(shipment.customerId) },
      { label: 'Origin warehouse ID', value: this.text(shipment.originWarehouseId) },
      { label: 'Destination', value: this.text(shipment.destinationDescription) },
      { label: 'Driver', value: this.driverLabel(shipment.driver) },
      { label: 'Vehicle', value: this.vehicleLabel(shipment.vehicle) },
      { label: 'Loaded at', value: this.formatTimestamp(shipment.loadedAt) },
      { label: 'Dispatched at', value: this.formatTimestamp(shipment.dispatchedAt) },
      { label: 'Delivered at', value: this.formatTimestamp(shipment.deliveredAt) },
      { label: 'Cancelled at', value: this.formatTimestamp(shipment.cancelledAt) },
    ];
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id');
      if (!id) {
        this.errorMessage.set('Shipment ID is missing.');
        return;
      }
      this.loadShipment(id);
    });
  }

  text(value: unknown): string {
    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }
    return '-';
  }

  formatTimestamp(value: unknown): string {
    return formatTimestamp(value);
  }

  driverLabel(driver: DriverResponse | undefined): string {
    if (!driver) {
      return '-';
    }
    return driver.licenseNumber || driver.id || '-';
  }

  vehicleLabel(vehicle: VehicleResponse | undefined): string {
    if (!vehicle) {
      return '-';
    }
    return vehicle.plate || vehicle.description || vehicle.id || '-';
  }

  private loadShipment(id: string): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.logistics
      .getShipmentById(id)
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(),
      )
      .subscribe({
        next: (shipment) => this.shipment.set(shipment),
        error: () => {
          this.shipment.set(null);
          this.errorMessage.set('Unable to load shipment detail.');
        },
      });
  }
}
