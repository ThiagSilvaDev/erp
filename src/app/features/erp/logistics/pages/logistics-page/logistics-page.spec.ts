import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';

import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import { ShipmentSearchCriteriaStatus } from '@generated-api/modules/logistics/dtos';
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
      of({ id: 'shipment-1', shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.CANCELLED }),
    ),
    createDriver: vi.fn(() => of({ id: 'driver-2' })),
    createVehicle: vi.fn(() => of({ id: 'vehicle-2' })),
    deliverShipment: vi.fn(() =>
      of({ id: 'shipment-1', shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.DELIVERED }),
    ),
    dispatchShipment: vi.fn(() =>
      of({ id: 'shipment-1', shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.IN_TRANSIT }),
    ),
    getDriverById: vi.fn((id: string) => of({ id, name: 'Fresh driver' })),
    getShipmentById: vi.fn((id: string) =>
      of({ id, shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.CREATED }),
    ),
    getVehicleById: vi.fn((id: string) => of({ id, plate: 'NEW-1001' })),
    loadShipment: vi.fn(() =>
      of({ id: 'shipment-1', shipmentNumber: 1001, status: ShipmentSearchCriteriaStatus.LOADED }),
    ),
    listDrivers: vi.fn(() => page([{ id: 'driver-1', name: 'Ana' }])),
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

  function createComponent(): ComponentFixture<LogisticsPage> {
    TestBed.configureTestingModule({
      imports: [LogisticsPage],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { data: of({}) } },
        { provide: LogisticsService, useValue: logistics },
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

  it('creates drivers and reloads the driver tab', () => {
    const component = createComponent().componentInstance;
    component.changeTab(1);
    logistics.listDrivers.mockClear();

    component.driverForm.setValue({
      name: 'Bruno',
      licenseNumber: 'CNH-1',
      phone: '555-0100',
    });
    component.submitDriver();

    expect(logistics.createDriver).toHaveBeenCalledWith({
      name: 'Bruno',
      licenseNumber: 'CNH-1',
      phone: '555-0100',
    });
    expect(logistics.listDrivers).toHaveBeenCalledWith({ page: 0, size: 10 });
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

    component.openVehicleDetail({ id: 'vehicle-1', plate: 'OLD-1001' });
    component.openDriverDetail({ id: 'driver-1', name: 'Stale driver' });

    expect(logistics.getVehicleById).toHaveBeenCalledWith('vehicle-1');
    expect(component.selectedVehicle()).toEqual({ id: 'vehicle-1', plate: 'NEW-1001' });
    expect(logistics.getDriverById).toHaveBeenCalledWith('driver-1');
    expect(component.selectedDriver()).toEqual({ id: 'driver-1', name: 'Fresh driver' });
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
