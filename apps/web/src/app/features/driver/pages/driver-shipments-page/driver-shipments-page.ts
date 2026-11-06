import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form, submit } from '@angular/forms/signals';
import { Observable, finalize, firstValueFrom } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import type {
  CreateTrackingEventRequest,
  CurrentDriverResponse,
  ShipmentResponse,
} from '@generated-api/modules/logistics/dtos';
import { ShipmentResponseStatus } from '@generated-api/modules/logistics/dtos';
import { formatTimestamp } from '@features/erp/logistics/utils/timestamp-format';

type DriverShipmentAction = 'load' | 'dispatch' | 'tracking' | 'deliver';

interface DetailField {
  label: string;
  value: string;
}

const PAGE_SIZE = 10;
const EMPTY_EVENT = {
  occurredAt: '',
  location: '',
  notes: '',
};

@Component({
  selector: 'app-driver-shipments-page',
  imports: [
    FormField,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDescriptionsModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzModalModule,
    NzPaginationModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './driver-shipments-page.html',
  styleUrl: './driver-shipments-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DriverShipmentsPage {
  private readonly destroyRef = inject(DestroyRef);
  private readonly logistics = inject(LogisticsService);
  private readonly messages = inject(NzMessageService);

  readonly driver = signal<CurrentDriverResponse | null>(null);
  readonly driverLoading = signal(false);
  readonly driverError = signal<string | null>(null);

  readonly shipments = signal<ShipmentResponse[]>([]);
  readonly shipmentsLoading = signal(false);
  readonly shipmentsError = signal<string | null>(null);
  readonly pageIndex = signal(1);
  readonly total = signal(0);
  readonly pageSize = PAGE_SIZE;

  readonly selectedShipment = signal<ShipmentResponse | null>(null);
  readonly isDetailOpen = signal(false);
  readonly actionShipment = signal<ShipmentResponse | null>(null);
  readonly action = signal<DriverShipmentAction>('tracking');
  readonly isActionOpen = signal(false);
  readonly isMutating = signal(false);

  readonly eventModel = signal({ ...EMPTY_EVENT });
  readonly eventForm = form(this.eventModel);

  readonly driverName = computed(
    () => this.driver()?.displayName || this.driver()?.email || 'Driver',
  );
  readonly shipmentFields = computed<DetailField[]>(() => {
    const shipment = this.selectedShipment();
    if (!shipment) {
      return [];
    }

    return [
      { label: 'Shipment', value: this.shipmentNumber(shipment) },
      { label: 'Status', value: this.text(shipment.status) },
      { label: 'Route', value: this.text(shipment.routeName) },
      { label: 'Destination', value: this.text(shipment.destinationDescription) },
      { label: 'Vehicle', value: this.vehicleLabel(shipment) },
      { label: 'Loaded at', value: this.timestamp(shipment.loadedAt) },
      { label: 'Dispatched at', value: this.timestamp(shipment.dispatchedAt) },
      { label: 'Delivered at', value: this.timestamp(shipment.deliveredAt) },
    ];
  });

  constructor() {
    this.loadDriver();
    this.loadShipments();
  }

  loadDriver(): void {
    this.driverLoading.set(true);
    this.driverError.set(null);
    this.logistics
      .getCurrentDriver()
      .pipe(
        finalize(() => this.driverLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (driver) => this.driver.set(driver),
        error: () => {
          this.driver.set(null);
          this.driverError.set('Unable to load your driver profile.');
        },
      });
  }

  loadShipments(): void {
    this.shipmentsLoading.set(true);
    this.shipmentsError.set(null);
    this.logistics
      .listCurrentDriverShipments({
        page: this.pageIndex() - 1,
        size: PAGE_SIZE,
      })
      .pipe(
        finalize(() => this.shipmentsLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          const shipments = page.content ?? [];
          this.shipments.set(shipments);
          this.total.set(page.page?.totalElements ?? 0);
          this.reconcileSelectedShipment(shipments);
        },
        error: () => {
          this.shipments.set([]);
          this.total.set(0);
          this.shipmentsError.set('Unable to load your shipments.');
        },
      });
  }

  changePage(page: number): void {
    this.pageIndex.set(page);
    this.closeDetail();
    this.loadShipments();
  }

  openDetail(shipment: ShipmentResponse): void {
    this.selectedShipment.set(shipment);
    this.isDetailOpen.set(true);
  }

  closeDetail(): void {
    this.isDetailOpen.set(false);
    this.selectedShipment.set(null);
  }

  openAction(shipment: ShipmentResponse, action: DriverShipmentAction): void {
    if (!shipment.id || !this.isActionAllowed(shipment, action)) {
      return;
    }

    this.actionShipment.set(shipment);
    this.action.set(action);
    this.eventModel.set({ ...EMPTY_EVENT });
    this.isActionOpen.set(true);
  }

  closeAction(): void {
    if (this.isMutating()) {
      return;
    }

    this.isActionOpen.set(false);
    this.actionShipment.set(null);
  }

  submitAction(): void {
    void submit(this.eventForm, async () => {
      const shipment = this.actionShipment();
      const action = this.action();
      if (!shipment?.id || !this.isActionAllowed(shipment, action)) {
        return;
      }

      this.isMutating.set(true);
      try {
        const updated = await firstValueFrom(
          this.actionRequest(shipment.id, action, this.eventPayload()).pipe(
            takeUntilDestroyed(this.destroyRef),
          ),
        );
        this.applyMutationResult(updated);
        this.messages.success(`${this.actionLabel(action)} completed.`);
        this.isActionOpen.set(false);
        this.actionShipment.set(null);
        this.eventModel.set({ ...EMPTY_EVENT });
        this.loadShipments();
      } catch {
        this.messages.error(`Unable to ${this.actionLabel(action).toLowerCase()} shipment.`);
      } finally {
        this.isMutating.set(false);
      }
    });
  }

  canLoad(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentResponseStatus.DISPATCH_ASSIGNED;
  }

  canDispatch(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentResponseStatus.LOADED;
  }

  canTrack(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentResponseStatus.IN_TRANSIT;
  }

  canDeliver(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentResponseStatus.IN_TRANSIT;
  }

  actionLabel(action = this.action()): string {
    switch (action) {
      case 'load':
        return 'Load';
      case 'dispatch':
        return 'Dispatch';
      case 'deliver':
        return 'Deliver';
      default:
        return 'Track';
    }
  }

  shipmentNumber(shipment: ShipmentResponse): string {
    return this.text(shipment.formattedShipmentNumber ?? shipment.shipmentNumber);
  }

  vehicleLabel(shipment: ShipmentResponse): string {
    return shipment.vehicle?.plate || shipment.vehicle?.description || '-';
  }

  text(value: unknown): string {
    return typeof value === 'string' || typeof value === 'number' ? String(value) : '-';
  }

  timestamp(value: unknown): string {
    return formatTimestamp(value);
  }

  statusColor(status: ShipmentResponse['status']): string {
    switch (status) {
      case ShipmentResponseStatus.DISPATCH_ASSIGNED:
        return 'processing';
      case ShipmentResponseStatus.LOADED:
        return 'warning';
      case ShipmentResponseStatus.IN_TRANSIT:
        return 'blue';
      case ShipmentResponseStatus.DELIVERED:
        return 'success';
      case ShipmentResponseStatus.CANCELLED:
        return 'error';
      default:
        return 'default';
    }
  }

  private isActionAllowed(shipment: ShipmentResponse, action: DriverShipmentAction): boolean {
    switch (action) {
      case 'load':
        return this.canLoad(shipment);
      case 'dispatch':
        return this.canDispatch(shipment);
      case 'deliver':
        return this.canDeliver(shipment);
      default:
        return this.canTrack(shipment);
    }
  }

  private actionRequest(
    shipmentId: string,
    action: DriverShipmentAction,
    event: CreateTrackingEventRequest,
  ): Observable<ShipmentResponse> {
    switch (action) {
      case 'load':
        return this.logistics.loadCurrentDriverShipment(shipmentId, event);
      case 'dispatch':
        return this.logistics.dispatchCurrentDriverShipment(shipmentId, event);
      case 'deliver':
        return this.logistics.deliverCurrentDriverShipment(shipmentId, event);
      default:
        return this.logistics.addCurrentDriverShipmentTrackingEvent(shipmentId, event);
    }
  }

  private eventPayload(): CreateTrackingEventRequest {
    const event = this.eventModel();
    return {
      occurredAt: event.occurredAt || undefined,
      location: event.location.trim() || undefined,
      notes: event.notes.trim() || undefined,
    };
  }

  private applyMutationResult(updated: ShipmentResponse): void {
    this.shipments.update((shipments) =>
      shipments.map((shipment) => (shipment.id === updated.id ? updated : shipment)),
    );
    if (this.selectedShipment()?.id === updated.id) {
      this.selectedShipment.set(updated);
    }
  }

  private reconcileSelectedShipment(shipments: ShipmentResponse[]): void {
    const selectedId = this.selectedShipment()?.id;
    if (!selectedId) {
      return;
    }

    const refreshed = shipments.find((shipment) => shipment.id === selectedId);
    if (refreshed) {
      this.selectedShipment.set(refreshed);
    }
  }
}
