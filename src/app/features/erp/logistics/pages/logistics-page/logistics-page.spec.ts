import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideNzIcons } from 'ng-zorro-antd/icon';

import { AuthService } from '@core/auth/auth.service';
import { icons } from '@core/ui/icons-provider';
import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import { ShipmentSearchCriteriaStatus } from '@generated-api/modules/logistics/dtos';
import { IamService } from '@generated-api/modules/iam/Iam.service';
import { UsersService } from '@generated-api/modules/users/Users.service';
import { LogisticsPage } from './logistics-page';

function page(content: object[] = []) {
  return of({ content, page: { totalElements: content.length } });
}

describe('LogisticsPage', () => {
  const logistics = {
    addShipmentTrackingEvent: vi.fn(() =>
      of({ id: 'shipment-1', shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.CREATED }),
    ),
    assignShipmentDispatch: vi.fn(() =>
      of({
        id: 'shipment-1',
        shipmentNumber: 1001,
        status: ShipmentSearchCriteriaStatus.DISPATCH_ASSIGNED,
      }),
    ),
    cancelShipment: vi.fn(() =>
      of({
        id: 'shipment-1',
        shipmentNumber: 1001,
        status: ShipmentSearchCriteriaStatus.CANCELLED,
      }),
    ),
    createDriver: vi.fn(() => of({ id: 'driver-2', userId: 'user-2' })),
    createVehicle: vi.fn(() => of({ id: 'vehicle-2' })),
    deliverShipment: vi.fn(() =>
      of({
        id: 'shipment-1',
        shipmentNumber: 1001,
        status: ShipmentSearchCriteriaStatus.DELIVERED,
      }),
    ),
    dispatchShipment: vi.fn(() =>
      of({
        id: 'shipment-1',
        shipmentNumber: 1001,
        status: ShipmentSearchCriteriaStatus.IN_TRANSIT,
      }),
    ),
    getDriverById: vi.fn((id: string) => of({ id, userId: 'user-1', licenseNumber: 'CNH-1' })),
    getShipmentById: vi.fn((id: string) =>
      of({ id, shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.CREATED }),
    ),
    getVehicleById: vi.fn((id: string) => of({ id, plate: 'NEW-1001' })),
    loadShipment: vi.fn(() =>
      of({ id: 'shipment-1', shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.LOADED }),
    ),
    listDrivers: vi.fn(() => page([{ id: 'driver-1', userId: 'user-1', licenseNumber: 'CNH-1' }])),
    listShipments: vi.fn(() =>
      page([
        {
          id: 'shipment-1',
          shipmentNumber: 1001,
          status: ShipmentSearchCriteriaStatus.CREATED,
        },
      ]),
    ),
    listVehicles: vi.fn(() => page([{ id: 'vehicle-1', plate: 'ABC-1234' }])),
  };
  const users = {
    createUser: vi.fn(() =>
      of({
        id: 'user-2',
        firstName: 'Bruno',
        lastName: 'Silva',
        email: 'bruno@example.com',
      }),
    ),
    listUsers: vi.fn(() =>
      page([
        {
          id: 'user-1',
          displayName: 'Ana Driver',
          phone: '555-0100',
          email: 'ana@example.com',
        },
      ]),
    ),
  };
  const iam = {
    listRoles: vi.fn(() =>
      of([{ id: 'role-driver', code: 'DRIVER', name: 'Driver', status: 'ACTIVE' }]),
    ),
  };
  const auth = {
    session: vi.fn(() => ({ activeCompany: { id: 'company-1', name: 'ACME' } })),
  };

  function createComponent(): ComponentFixture<LogisticsPage> {
    TestBed.configureTestingModule({
      imports: [LogisticsPage],
      providers: [
        provideNoopAnimations(),
        provideNzIcons(icons),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { data: of({}) } },
        { provide: AuthService, useValue: auth },
        { provide: LogisticsService, useValue: logistics },
        { provide: UsersService, useValue: users },
        { provide: IamService, useValue: iam },
      ],
    });

    return TestBed.createComponent(LogisticsPage);
  }

  beforeEach(() => {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
  });

  it('loads vehicles on entry without using the other tab loaders', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    expect(logistics.listVehicles).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(logistics.listDrivers).not.toHaveBeenCalled();
    expect(logistics.listShipments).not.toHaveBeenCalled();
    expect(component.vehiclesLoading()).toBe(false);
    expect(component.driversLoading()).toBe(false);
    expect(component.shipmentsLoading()).toBe(false);
  });

  it('loads each tab independently when selected', () => {
    const component = createComponent().componentInstance;

    component.changeTab(1);
    component.changeTab(2);

    expect(logistics.listDrivers).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(logistics.listShipments).toHaveBeenCalledWith(
      { criteria: {}, page: 0, size: 10 },
      { params: {} },
    );
  });

  it('creates vehicles and reloads the vehicle tab', () => {
    const component = createComponent().componentInstance;
    logistics.listVehicles.mockClear();

    component.vehicleForm.setValue({
      plate: 'DEF-5678',
      description: 'Truck',
      capacity: 12,
    });
    component.submitVehicle();

    expect(logistics.createVehicle).toHaveBeenCalledWith({
      plate: 'DEF-5678',
      description: 'Truck',
      capacity: 12,
    });
    expect(logistics.listVehicles).toHaveBeenCalledWith({ page: 0, size: 10 });
  });

  it('creates a DRIVER user before creating and reloading the driver profile', async () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;
    component.changeTab(1);
    logistics.listDrivers.mockClear();

    component.openDriverCreate();
    await vi.waitFor(() => expect(component.driverRoleId()).toBe('role-driver'));

    component.driverModel.set({
      firstName: 'Bruno',
      lastName: 'Silva',
      email: 'bruno@example.com',
      phone: '555-0100',
      birthDate: '1990-01-01',
      password: 'password123',
      licenseNumber: 'CNH-1',
    });
    component.submitDriver();
    await vi.waitFor(() => expect(logistics.createDriver).toHaveBeenCalled());

    expect(users.createUser).toHaveBeenCalledWith({
      firstName: 'Bruno',
      lastName: 'Silva',
      email: 'bruno@example.com',
      phone: '555-0100',
      birthDate: '1990-01-01',
      password: 'password123',
      roleIds: ['role-driver'],
    });
    expect(logistics.createDriver).toHaveBeenCalledWith({
      userId: 'user-2',
      licenseNumber: 'CNH-1',
    });
    expect(logistics.listDrivers).toHaveBeenCalledWith({ page: 0, size: 10 });
  });

  it('retries only driver creation after the user was created', async () => {
    const component = createComponent().componentInstance;
    logistics.createDriver.mockReturnValueOnce(throwError(() => new Error('failed')));
    component.openDriverCreate();
    await vi.waitFor(() => expect(component.driverRoleId()).toBe('role-driver'));

    component.driverModel.set({
      firstName: 'Bruno',
      lastName: 'Silva',
      email: 'bruno@example.com',
      phone: '',
      birthDate: '',
      password: 'password123',
      licenseNumber: 'CNH-1',
    });

    component.submitDriver();
    await vi.waitFor(() => expect(component.pendingDriverUserId()).toBe('user-2'));
    component.submitDriver();
    await vi.waitFor(() => expect(logistics.createDriver).toHaveBeenCalledTimes(2));

    expect(users.createUser).toHaveBeenCalledTimes(1);
  });

  it('uses the generated shipment status type when filtering shipments', () => {
    const component = createComponent().componentInstance;

    component.shipmentFilterForm.controls.status.setValue(ShipmentSearchCriteriaStatus.IN_TRANSIT);
    component.submitShipmentSearch();

    expect(logistics.listShipments).toHaveBeenLastCalledWith(
      {
        criteria: { status: ShipmentSearchCriteriaStatus.IN_TRANSIT },
        page: 0,
        size: 10,
      },
      { params: { status: ShipmentSearchCriteriaStatus.IN_TRANSIT } },
    );
  });

  it('loads vehicle and driver details by id before opening detail modals', () => {
    const component = createComponent().componentInstance;
    component.changeTab(1);

    component.openVehicleDetail({ id: 'vehicle-1', plate: 'OLD-1001' });
    component.openDriverDetail({
      id: 'driver-1',
      userId: 'user-1',
      licenseNumber: 'CNH-1',
    });

    expect(logistics.getVehicleById).toHaveBeenCalledWith('vehicle-1');
    expect(component.selectedVehicle()).toEqual({ id: 'vehicle-1', plate: 'NEW-1001' });
    expect(logistics.getDriverById).toHaveBeenCalledWith('driver-1');
    expect(component.selectedDriver()).toEqual({
      id: 'driver-1',
      userId: 'user-1',
      licenseNumber: 'CNH-1',
    });
    expect(component.driverLabel(component.selectedDriver() ?? undefined)).toBe('Ana Driver');
  });

  it('routes shipment view actions to the shipment detail page', () => {
    const component = createComponent().componentInstance;
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.openShipmentDetail({ id: 'shipment-1', shipmentNumber: 1000 });

    expect(navigate).toHaveBeenCalledWith(['/logistics/shipments', 'shipment-1']);
    expect(logistics.getShipmentById).not.toHaveBeenCalled();
  });

  it('assigns shipment dispatch using selected driver and vehicle', () => {
    const component = createComponent().componentInstance;

    component.openDispatchAssignment({
      id: 'shipment-1',
      shipmentNumber: 1001,
      status: ShipmentSearchCriteriaStatus.CREATED,
      routeName: 'Route A',
      originWarehouseId: 'warehouse-1',
      destinationDescription: 'Dock',
    });
    component.dispatchAssignmentForm.patchValue({
      driverId: 'driver-1',
      vehicleId: 'vehicle-1',
    });
    component.submitDispatchAssignment();

    expect(logistics.assignShipmentDispatch).toHaveBeenCalledWith('shipment-1', {
      driverId: 'driver-1',
      vehicleId: 'vehicle-1',
      routeName: 'Route A',
      originWarehouseId: 'warehouse-1',
      destinationDescription: 'Dock',
    });
    expect(logistics.listShipments).toHaveBeenCalled();
  });

  it('runs shipment state actions through the generated API methods', () => {
    const component = createComponent().componentInstance;
    const shipment = { id: 'shipment-1', shipmentNumber: 1001 };

    component.openShipmentEvent(shipment, 'load');
    component.shipmentEventForm.patchValue({ location: 'Warehouse', notes: 'Loaded' });
    component.submitShipmentEvent();

    component.openShipmentEvent(shipment, 'dispatch');
    component.submitShipmentEvent();

    component.openShipmentEvent(shipment, 'deliver');
    component.submitShipmentEvent();

    component.openShipmentEvent(shipment, 'cancel');
    component.submitShipmentEvent();

    component.openShipmentEvent(shipment, 'tracking');
    component.submitShipmentEvent();

    expect(logistics.loadShipment).toHaveBeenCalledWith('shipment-1', {
      occurredAt: undefined,
      location: 'Warehouse',
      notes: 'Loaded',
    });
    expect(logistics.dispatchShipment).toHaveBeenCalled();
    expect(logistics.deliverShipment).toHaveBeenCalled();
    expect(logistics.cancelShipment).toHaveBeenCalled();
    expect(logistics.addShipmentTrackingEvent).toHaveBeenCalled();
  });
});
