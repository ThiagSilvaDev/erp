import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormField, email, form, minLength, required, submit } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  EMPTY,
  Observable,
  catchError,
  expand,
  finalize,
  firstValueFrom,
  forkJoin,
  of,
  reduce,
} from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { AuthService } from '@core/auth/auth.service';
import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import type {
  AssignDispatchRequest,
  CreateTrackingEventRequest,
  DriverResponse,
  ShipmentResponse,
  ShipmentSearchCriteriaStatus,
  VehicleResponse,
} from '@generated-api/modules/logistics/dtos';
import { UsersService } from '@generated-api/modules/users/Users.service';
import type { PagedModelUserResponse, UserResponse } from '@generated-api/modules/users/dtos';
import { CreateUserRequestRole, ListUsersRole } from '@generated-api/modules/users/dtos';
import {
  ShipmentResponseStatus as ShipmentState,
  ShipmentSearchCriteriaStatus as ShipmentStatus,
} from '@generated-api/modules/logistics/dtos';
import { formatTimestamp } from '../../utils/timestamp-format';

type LogisticsTab = 'vehicles' | 'drivers' | 'shipments';

type VehicleForm = FormGroup<{
  plate: FormControl<string>;
  description: FormControl<string>;
  capacity: FormControl<number | null>;
}>;

type ShipmentFilterForm = FormGroup<{
  status: FormControl<ShipmentSearchCriteriaStatus | null>;
}>;

type DispatchAssignmentForm = FormGroup<{
  driverId: FormControl<string>;
  vehicleId: FormControl<string>;
  routeName: FormControl<string>;
  originWarehouseId: FormControl<string>;
  destinationDescription: FormControl<string>;
}>;

type ShipmentEventForm = FormGroup<{
  occurredAt: FormControl<string>;
  location: FormControl<string>;
  notes: FormControl<string>;
}>;

interface DetailField {
  label: string;
  value: string;
}

type ShipmentEventAction = 'tracking' | 'load' | 'dispatch' | 'deliver' | 'cancel';

const PAGE_SIZE = 10;
const DRIVER_USER_PAGE_SIZE = 100;
const EMPTY_DRIVER_FORM = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  birthDate: '',
  password: '',
  licenseNumber: '',
};

@Component({
  selector: 'app-logistics-page',
  imports: [
    ReactiveFormsModule,
    FormField,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzDescriptionsModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzModalModule,
    NzPaginationModule,
    NzSelectModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './logistics-page.html',
  styleUrl: './logistics-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LogisticsPage {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly logistics = inject(LogisticsService);
  private readonly messages = inject(NzMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly usersApi = inject(UsersService);

  readonly activeTab = signal<LogisticsTab>('vehicles');

  readonly vehicles = signal<VehicleResponse[]>([]);
  readonly vehiclesLoading = signal(false);
  readonly vehiclesError = signal<string | null>(null);
  readonly vehiclesPageIndex = signal(1);
  readonly vehiclesTotal = signal(0);
  readonly vehiclesLoaded = signal(false);

  readonly drivers = signal<DriverResponse[]>([]);
  readonly driversLoading = signal(false);
  readonly driversError = signal<string | null>(null);
  readonly driversPageIndex = signal(1);
  readonly driversTotal = signal(0);
  readonly driversLoaded = signal(false);
  readonly driverUsersById = signal(new Map<string, UserResponse>());

  readonly shipments = signal<ShipmentResponse[]>([]);
  readonly shipmentsLoading = signal(false);
  readonly shipmentsError = signal<string | null>(null);
  readonly shipmentsPageIndex = signal(1);
  readonly shipmentsTotal = signal(0);
  readonly shipmentsLoaded = signal(false);

  readonly vehicleForm: VehicleForm = new FormGroup({
    plate: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    capacity: new FormControl<number | null>(null),
  });
  readonly pendingDriverUserId = signal<string | null>(null);
  readonly driverModel = signal({ ...EMPTY_DRIVER_FORM });
  readonly driverForm = form(this.driverModel, (path) => {
    required(path.firstName, { message: 'First name is required.' });
    required(path.lastName, { message: 'Last name is required.' });
    required(path.email, { message: 'Email is required.' });
    email(path.email, { message: 'Enter a valid email.' });
    required(path.password, { message: 'Password is required.' });
    minLength(path.password, 8, { message: 'Password must have at least 8 characters.' });
    required(path.licenseNumber, { message: 'License number is required.' });
  });
  readonly shipmentFilterForm: ShipmentFilterForm = new FormGroup({
    status: new FormControl<ShipmentSearchCriteriaStatus | null>(null),
  });
  readonly dispatchAssignmentForm: DispatchAssignmentForm = new FormGroup({
    driverId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    vehicleId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    routeName: new FormControl('', { nonNullable: true }),
    originWarehouseId: new FormControl('', { nonNullable: true }),
    destinationDescription: new FormControl('', { nonNullable: true }),
  });
  readonly shipmentEventForm: ShipmentEventForm = new FormGroup({
    occurredAt: new FormControl('', { nonNullable: true }),
    location: new FormControl('', { nonNullable: true }),
    notes: new FormControl('', { nonNullable: true }),
  });

  readonly shipmentStatusOptions = Object.values(ShipmentStatus);
  readonly shipmentState = ShipmentState;
  readonly pageSize = PAGE_SIZE;

  readonly isVehicleCreateOpen = signal(false);
  readonly isDriverCreateOpen = signal(false);
  readonly isVehicleSaving = signal(false);
  readonly isDriverSaving = signal(false);
  readonly isDispatchAssignmentOpen = signal(false);
  readonly isDispatchAssigning = signal(false);
  readonly isShipmentEventOpen = signal(false);
  readonly isShipmentEventSaving = signal(false);
  readonly shipmentEventAction = signal<ShipmentEventAction>('tracking');

  readonly selectedVehicle = signal<VehicleResponse | null>(null);
  readonly selectedDriver = signal<DriverResponse | null>(null);
  readonly selectedShipment = signal<ShipmentResponse | null>(null);
  readonly actionShipment = signal<ShipmentResponse | null>(null);
  readonly isVehicleDetailOpen = signal(false);
  readonly isDriverDetailOpen = signal(false);
  readonly isShipmentDetailOpen = signal(false);

  readonly selectedShipmentFields = computed<DetailField[]>(() => {
    const shipment = this.selectedShipment();
    if (!shipment) {
      return [];
    }

    return [
      { label: 'Shipment number', value: this.text(shipment.shipmentNumber) },
      { label: 'Status', value: this.text(shipment.status) },
      { label: 'Route', value: this.text(shipment.routeName) },
      { label: 'Sales order ID', value: this.text(shipment.salesOrderId) },
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
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      this.activateTab(this.tabFromRouteData(data));
    });
  }

  changeTab(index: number): void {
    const tabs: LogisticsTab[] = ['vehicles', 'drivers', 'shipments'];
    this.activateTab(tabs[index] ?? 'vehicles');
  }

  private activateTab(tab: LogisticsTab): void {
    this.activeTab.set(tab);
    if (tab === 'vehicles' && !this.vehiclesLoaded()) {
      this.loadVehicles();
    } else if (tab === 'drivers' && !this.driversLoaded()) {
      this.loadDrivers();
    } else if (tab === 'shipments' && !this.shipmentsLoaded()) {
      this.loadShipments();
    }
  }

  openVehicleCreate(): void {
    this.vehicleForm.reset();
    this.isVehicleCreateOpen.set(true);
  }

  closeVehicleCreate(): void {
    this.isVehicleCreateOpen.set(false);
  }

  submitVehicle(): void {
    if (this.vehicleForm.invalid) {
      this.vehicleForm.markAllAsTouched();
      return;
    }

    const value = this.vehicleForm.getRawValue();
    this.isVehicleSaving.set(true);
    this.logistics
      .createVehicle({
        plate: value.plate,
        description: value.description,
        capacity: value.capacity ?? undefined,
      })
      .pipe(
        finalize(() => this.isVehicleSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success('Vehicle created.');
          this.isVehicleCreateOpen.set(false);
          this.vehiclesPageIndex.set(1);
          this.loadVehicles();
        },
        error: () => this.messages.error('Unable to create vehicle.'),
      });
  }

  openDriverCreate(): void {
    this.driverModel.set({ ...EMPTY_DRIVER_FORM });
    this.pendingDriverUserId.set(null);
    this.isDriverCreateOpen.set(true);
  }

  closeDriverCreate(): void {
    this.isDriverCreateOpen.set(false);
  }

  submitDriver(): void {
    void submit(this.driverForm, async () => {
      const companyId = this.auth.session()?.activeCompany?.id;
      if (!companyId) {
        this.messages.error('An active company is required to create a driver.');
        return;
      }

      this.isDriverSaving.set(true);
      try {
        const value = this.driverModel();
        let userId = this.pendingDriverUserId();

        if (!userId) {
          const user = await firstValueFrom(
            this.usersApi
              .createUser({
                firstName: value.firstName.trim(),
                lastName: value.lastName.trim(),
                email: value.email.trim(),
                phone: value.phone.trim() || undefined,
                birthDate: value.birthDate || undefined,
                password: value.password,
                companyId,
                role: CreateUserRequestRole.DRIVER,
              })
              .pipe(takeUntilDestroyed(this.destroyRef)),
          );

          if (!user.id) {
            throw new Error('Created user response did not include an ID.');
          }

          userId = user.id;
          this.pendingDriverUserId.set(userId);
        }

        await firstValueFrom(
          this.logistics
            .createDriver({
              userId,
              licenseNumber: value.licenseNumber.trim(),
            })
            .pipe(takeUntilDestroyed(this.destroyRef)),
        );

        this.messages.success('Driver created.');
        this.isDriverCreateOpen.set(false);
        this.pendingDriverUserId.set(null);
        this.driverModel.set({ ...EMPTY_DRIVER_FORM });
        this.driversPageIndex.set(1);
        this.loadDrivers();
      } catch {
        if (this.pendingDriverUserId()) {
          this.messages.error('User created, but the driver profile failed. Retry to finish.');
        } else {
          this.messages.error('Unable to create driver.');
        }
      } finally {
        this.isDriverSaving.set(false);
      }
    });
  }

  openVehicleDetail(vehicle: VehicleResponse): void {
    const id = this.text(vehicle.id);
    this.logistics
      .getVehicleById(id)
      .pipe(
        catchError(() => of(vehicle)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((detail) => {
        this.selectedVehicle.set(detail);
        this.isVehicleDetailOpen.set(true);
      });
  }

  openDriverDetail(driver: DriverResponse): void {
    const id = this.text(driver.id);
    this.logistics
      .getDriverById(id)
      .pipe(
        catchError(() => of(driver)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((detail) => {
        this.selectedDriver.set(detail);
        this.isDriverDetailOpen.set(true);
      });
  }

  openShipmentDetail(shipment: ShipmentResponse): void {
    const id = this.text(shipment.id, '');
    if (!id) {
      this.messages.warning('Shipment detail is unavailable because the record has no ID.');
      return;
    }

    void this.router.navigate(['/logistics/shipments', id]);
  }

  closeVehicleDetail(): void {
    this.isVehicleDetailOpen.set(false);
  }

  closeDriverDetail(): void {
    this.isDriverDetailOpen.set(false);
  }

  closeShipmentDetail(): void {
    this.isShipmentDetailOpen.set(false);
  }

  openDispatchAssignment(shipment: ShipmentResponse): void {
    this.actionShipment.set(shipment);
    this.dispatchAssignmentForm.reset({
      driverId: this.text(shipment.driver?.id, ''),
      vehicleId: this.text(shipment.vehicle?.id, ''),
      routeName: this.text(shipment.routeName, ''),
      originWarehouseId: this.text(shipment.originWarehouseId, ''),
      destinationDescription: this.text(shipment.destinationDescription, ''),
    });
    this.ensureDispatchLookups();
    this.isDispatchAssignmentOpen.set(true);
  }

  closeDispatchAssignment(): void {
    this.isDispatchAssignmentOpen.set(false);
  }

  submitDispatchAssignment(): void {
    const shipmentId = this.text(this.actionShipment()?.id, '');
    if (!shipmentId || this.dispatchAssignmentForm.invalid) {
      this.dispatchAssignmentForm.markAllAsTouched();
      return;
    }

    this.isDispatchAssigning.set(true);
    this.logistics
      .assignShipmentDispatch(shipmentId, this.dispatchAssignmentPayload())
      .pipe(
        finalize(() => this.isDispatchAssigning.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (shipment) => this.handleShipmentActionSuccess(shipment, 'Dispatch assigned.'),
        error: () => this.messages.error('Unable to assign dispatch.'),
      });
  }

  openShipmentEvent(shipment: ShipmentResponse, action: ShipmentEventAction): void {
    this.actionShipment.set(shipment);
    this.shipmentEventAction.set(action);
    this.shipmentEventForm.reset({
      occurredAt: '',
      location: '',
      notes: this.defaultEventNotes(action),
    });
    this.isShipmentEventOpen.set(true);
  }

  closeShipmentEvent(): void {
    this.isShipmentEventOpen.set(false);
  }

  submitShipmentEvent(): void {
    const shipmentId = this.text(this.actionShipment()?.id, '');
    if (!shipmentId) {
      return;
    }

    this.isShipmentEventSaving.set(true);
    this.shipmentEventRequest(shipmentId, this.shipmentEventAction(), this.trackingEventPayload())
      .pipe(
        finalize(() => this.isShipmentEventSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (shipment) =>
          this.handleShipmentActionSuccess(
            shipment,
            this.eventSuccessMessage(this.shipmentEventAction()),
          ),
        error: () =>
          this.messages.error(`Unable to ${this.eventActionLabel().toLowerCase()} shipment.`),
      });
  }

  changeVehiclesPage(page: number): void {
    this.vehiclesPageIndex.set(page);
    this.loadVehicles();
  }

  changeDriversPage(page: number): void {
    this.driversPageIndex.set(page);
    this.loadDrivers();
  }

  changeShipmentsPage(page: number): void {
    this.shipmentsPageIndex.set(page);
    this.loadShipments();
  }

  submitShipmentSearch(): void {
    this.shipmentsPageIndex.set(1);
    this.loadShipments();
  }

  resetShipmentSearch(): void {
    this.shipmentFilterForm.reset();
    this.submitShipmentSearch();
  }

  text(value: unknown, fallback = '-'): string {
    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }
    return fallback;
  }

  formatTimestamp(value: unknown): string {
    return formatTimestamp(value);
  }

  vehicleLabel(vehicle: VehicleResponse | undefined): string {
    if (!vehicle) {
      return '-';
    }
    return vehicle.plate || vehicle.description || vehicle.id || '-';
  }

  driverLabel(driver: DriverResponse | undefined): string {
    if (!driver) {
      return '-';
    }
    return this.driverName(driver) || driver.licenseNumber || driver.id || '-';
  }

  driverName(driver: DriverResponse): string {
    const user = this.driverUser(driver);
    if (!user) {
      return '';
    }

    return user.displayName || [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
  }

  driverPhone(driver: DriverResponse): string {
    return this.driverUser(driver)?.phone || '-';
  }

  driverEmail(driver: DriverResponse): string {
    return this.driverUser(driver)?.email || '-';
  }

  canAssignDispatch(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentState.CREATED;
  }

  canLoadShipment(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentState.DISPATCH_ASSIGNED;
  }

  canDispatchShipment(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentState.LOADED;
  }

  canDeliverShipment(shipment: ShipmentResponse): boolean {
    return shipment.status === ShipmentState.IN_TRANSIT;
  }

  canCancelShipment(shipment: ShipmentResponse): boolean {
    return (
      shipment.status !== ShipmentState.DELIVERED && shipment.status !== ShipmentState.CANCELLED
    );
  }

  shipmentStatusColor(status: ShipmentResponse['status'] | undefined | null): string {
    switch (status) {
      case ShipmentState.CREATED:
        return 'default';
      case ShipmentState.DISPATCH_ASSIGNED:
        return 'processing';
      case ShipmentState.LOADED:
        return 'warning';
      case ShipmentState.IN_TRANSIT:
        return 'blue';
      case ShipmentState.DELIVERED:
        return 'success';
      case ShipmentState.CANCELLED:
        return 'error';
      default:
        return 'default';
    }
  }

  eventActionLabel(): string {
    switch (this.shipmentEventAction()) {
      case 'load':
        return 'Load';
      case 'dispatch':
        return 'Dispatch';
      case 'deliver':
        return 'Deliver';
      case 'cancel':
        return 'Cancel';
      default:
        return 'Add tracking event';
    }
  }

  private loadVehicles(): void {
    this.vehiclesLoading.set(true);
    this.vehiclesError.set(null);

    this.logistics
      .listVehicles({ page: this.vehiclesPageIndex() - 1, size: PAGE_SIZE })
      .pipe(
        finalize(() => this.vehiclesLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          this.vehicles.set(page.content ?? []);
          this.vehiclesTotal.set(page.page?.totalElements ?? 0);
          this.vehiclesLoaded.set(true);
        },
        error: () => {
          this.vehicles.set([]);
          this.vehiclesTotal.set(0);
          this.vehiclesError.set('Unable to load vehicles.');
        },
      });
  }

  private loadDrivers(): void {
    this.driversLoading.set(true);
    this.driversError.set(null);

    forkJoin({
      drivers: this.logistics.listDrivers({
        page: this.driversPageIndex() - 1,
        size: PAGE_SIZE,
      }),
      users: this.listAllDriverUsers().pipe(catchError(() => of([]))),
    })
      .pipe(
        finalize(() => this.driversLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ drivers, users }) => {
          this.drivers.set(drivers.content ?? []);
          this.driversTotal.set(drivers.page?.totalElements ?? 0);
          this.driverUsersById.set(
            new Map(
              users.flatMap((user) =>
                user.id ? ([[user.id, user]] as [string, UserResponse][]) : [],
              ),
            ),
          );
          this.driversLoaded.set(true);
        },
        error: () => {
          this.drivers.set([]);
          this.driversTotal.set(0);
          this.driversError.set('Unable to load drivers.');
        },
      });
  }

  private listAllDriverUsers(): Observable<UserResponse[]> {
    return this.usersApi
      .listUsers({
        role: ListUsersRole.DRIVER,
        page: 0,
        size: DRIVER_USER_PAGE_SIZE,
      })
      .pipe(
        expand((page) => {
          const pageNumber = page.page?.number ?? 0;
          const totalPages = page.page?.totalPages ?? 1;
          if (pageNumber + 1 >= totalPages) {
            return EMPTY;
          }

          return this.usersApi.listUsers({
            role: ListUsersRole.DRIVER,
            page: pageNumber + 1,
            size: DRIVER_USER_PAGE_SIZE,
          });
        }),
        reduce(
          (users, page: PagedModelUserResponse) => [...users, ...(page.content ?? [])],
          [] as UserResponse[],
        ),
      );
  }

  private driverUser(driver: DriverResponse): UserResponse | undefined {
    return driver.userId ? this.driverUsersById().get(driver.userId) : undefined;
  }

  private loadShipments(): void {
    this.shipmentsLoading.set(true);
    this.shipmentsError.set(null);

    const status = this.shipmentFilterForm.controls.status.value;
    this.logistics
      .listShipments(
        {
          criteria: status ? { status } : {},
          page: this.shipmentsPageIndex() - 1,
          size: PAGE_SIZE,
        },
        {
          params: status ? { status } : {},
        },
      )
      .pipe(
        finalize(() => this.shipmentsLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          this.shipments.set(page.content ?? []);
          this.shipmentsTotal.set(page.page?.totalElements ?? 0);
          this.shipmentsLoaded.set(true);
        },
        error: () => {
          this.shipments.set([]);
          this.shipmentsTotal.set(0);
          this.shipmentsError.set('Unable to load shipments.');
        },
      });
  }

  private ensureDispatchLookups(): void {
    const loaders: {
      vehicles?: Observable<unknown>;
      drivers?: Observable<unknown>;
    } = {};

    if (!this.vehiclesLoaded()) {
      this.vehiclesLoading.set(true);
      loaders.vehicles = this.logistics.listVehicles({ page: 0, size: PAGE_SIZE });
    }

    if (!this.driversLoaded()) {
      this.driversLoading.set(true);
      loaders.drivers = this.logistics.listDrivers({ page: 0, size: PAGE_SIZE });
    }

    if (!loaders.vehicles && !loaders.drivers) {
      return;
    }

    forkJoin(loaders)
      .pipe(
        finalize(() => {
          this.vehiclesLoading.set(false);
          this.driversLoading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (lookups) => {
          if ('vehicles' in lookups) {
            const vehiclesPage = lookups.vehicles as {
              content?: VehicleResponse[];
              page?: { totalElements?: number };
            };
            this.vehicles.set(vehiclesPage.content ?? []);
            this.vehiclesTotal.set(vehiclesPage.page?.totalElements ?? 0);
            this.vehiclesLoaded.set(true);
          }
          if ('drivers' in lookups) {
            const driversPage = lookups.drivers as {
              content?: DriverResponse[];
              page?: { totalElements?: number };
            };
            this.drivers.set(driversPage.content ?? []);
            this.driversTotal.set(driversPage.page?.totalElements ?? 0);
            this.driversLoaded.set(true);
          }
        },
        error: () => this.messages.error('Unable to load dispatch assignment options.'),
      });
  }

  private dispatchAssignmentPayload(): AssignDispatchRequest {
    const value = this.dispatchAssignmentForm.getRawValue();
    return {
      driverId: value.driverId,
      vehicleId: value.vehicleId,
      routeName: value.routeName.trim() || undefined,
      originWarehouseId: value.originWarehouseId.trim() || undefined,
      destinationDescription: value.destinationDescription.trim() || undefined,
    };
  }

  private trackingEventPayload(): CreateTrackingEventRequest {
    const value = this.shipmentEventForm.getRawValue();
    return {
      occurredAt: value.occurredAt || undefined,
      location: value.location.trim() || undefined,
      notes: value.notes.trim() || undefined,
    };
  }

  private shipmentEventRequest(
    shipmentId: string,
    action: ShipmentEventAction,
    event: CreateTrackingEventRequest,
  ): Observable<ShipmentResponse> {
    switch (action) {
      case 'load':
        return this.logistics.loadShipment(shipmentId, event);
      case 'dispatch':
        return this.logistics.dispatchShipment(shipmentId, event);
      case 'deliver':
        return this.logistics.deliverShipment(shipmentId, event);
      case 'cancel':
        return this.logistics.cancelShipment(shipmentId, event);
      default:
        return this.logistics.addShipmentTrackingEvent(shipmentId, event);
    }
  }

  private handleShipmentActionSuccess(shipment: ShipmentResponse, message: string): void {
    this.messages.success(message);
    this.isDispatchAssignmentOpen.set(false);
    this.isShipmentEventOpen.set(false);
    this.selectedShipment.update((selected) =>
      selected?.id === shipment.id ? shipment : selected,
    );
    this.shipments.update((shipments) =>
      shipments.map((item) => (item.id === shipment.id ? shipment : item)),
    );
    this.loadShipments();
  }

  private defaultEventNotes(action: ShipmentEventAction): string {
    switch (action) {
      case 'load':
        return 'Shipment loaded.';
      case 'dispatch':
        return 'Shipment dispatched.';
      case 'deliver':
        return 'Shipment delivered.';
      case 'cancel':
        return 'Shipment cancelled.';
      default:
        return '';
    }
  }

  private eventSuccessMessage(action: ShipmentEventAction): string {
    switch (action) {
      case 'load':
        return 'Shipment loaded.';
      case 'dispatch':
        return 'Shipment dispatched.';
      case 'deliver':
        return 'Shipment delivered.';
      case 'cancel':
        return 'Shipment cancelled.';
      default:
        return 'Tracking event added.';
    }
  }

  private tabFromRouteData(data: Record<string, unknown>): LogisticsTab {
    const tab = data['logisticsTab'];
    if (tab === 'drivers' || tab === 'shipments') {
      return tab;
    }
    return 'vehicles';
  }
}
