import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { provideNzIcons } from 'ng-zorro-antd/icon';

import { icons } from '@core/ui/icons-provider';
import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import {
  ShipmentResponseStatus,
  type ShipmentResponse,
} from '@generated-api/modules/logistics/dtos';
import { DriverShipmentsPage } from './driver-shipments-page';

describe('DriverShipmentsPage', () => {
  const shipments: ShipmentResponse[] = [
    {
      id: 'assigned',
      shipmentNumber: 1001,
      status: ShipmentResponseStatus.DISPATCH_ASSIGNED,
    },
    {
      id: 'loaded',
      shipmentNumber: 1002,
      status: ShipmentResponseStatus.LOADED,
    },
    {
      id: 'transit',
      shipmentNumber: 1003,
      status: ShipmentResponseStatus.IN_TRANSIT,
    },
    {
      id: 'delivered',
      shipmentNumber: 1004,
      status: ShipmentResponseStatus.DELIVERED,
    },
    {
      id: 'cancelled',
      shipmentNumber: 1005,
      status: ShipmentResponseStatus.CANCELLED,
    },
  ];

  const logistics = {
    getCurrentDriver: vi.fn(() =>
      of({
        id: 'driver-1',
        userId: 'user-1',
        displayName: 'Ana Driver',
        licenseNumber: 'CNH-1',
      }),
    ),
    listCurrentDriverShipments: vi.fn(() =>
      of({
        content: shipments,
        page: { number: 0, size: 10, totalElements: shipments.length, totalPages: 1 },
      }),
    ),
    loadCurrentDriverShipment: vi.fn(() =>
      of({
        ...shipments[0],
        status: ShipmentResponseStatus.LOADED,
      }),
    ),
    dispatchCurrentDriverShipment: vi.fn(() =>
      of({
        ...shipments[1],
        status: ShipmentResponseStatus.IN_TRANSIT,
      }),
    ),
    addCurrentDriverShipmentTrackingEvent: vi.fn(() => of(shipments[2])),
    deliverCurrentDriverShipment: vi.fn(() =>
      of({
        ...shipments[2],
        status: ShipmentResponseStatus.DELIVERED,
      }),
    ),
  };

  beforeEach(() => {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      imports: [DriverShipmentsPage],
      providers: [
        provideNoopAnimations(),
        provideNzIcons(icons),
        { provide: LogisticsService, useValue: logistics },
      ],
    });
  });

  it('loads the current driver and paged shipments', () => {
    const component = TestBed.createComponent(DriverShipmentsPage).componentInstance;

    expect(logistics.getCurrentDriver).toHaveBeenCalledOnce();
    expect(logistics.listCurrentDriverShipments).toHaveBeenCalledWith({
      page: 0,
      size: 10,
    });
    expect(component.driverName()).toBe('Ana Driver');
    expect(component.shipments()).toHaveLength(5);
  });

  it('exposes workflow actions only for the matching shipment states', () => {
    const component = TestBed.createComponent(DriverShipmentsPage).componentInstance;

    expect(component.canLoad(shipments[0])).toBe(true);
    expect(component.canDispatch(shipments[1])).toBe(true);
    expect(component.canTrack(shipments[2])).toBe(true);
    expect(component.canDeliver(shipments[2])).toBe(true);

    for (const shipment of [shipments[3], shipments[4]]) {
      expect(component.canLoad(shipment)).toBe(false);
      expect(component.canDispatch(shipment)).toBe(false);
      expect(component.canTrack(shipment)).toBe(false);
      expect(component.canDeliver(shipment)).toBe(false);
    }
  });

  it('uses current-driver mutations and refreshes the list after each success', async () => {
    const component = TestBed.createComponent(DriverShipmentsPage).componentInstance;
    logistics.listCurrentDriverShipments.mockClear();

    component.eventModel.set({
      occurredAt: '2026-07-29T10:30',
      location: 'Warehouse',
      notes: 'Ready',
    });
    component.openAction(shipments[0], 'load');
    component.eventModel.set({
      occurredAt: '2026-07-29T10:30',
      location: 'Warehouse',
      notes: 'Ready',
    });
    component.submitAction();
    await vi.waitFor(() => {
      expect(logistics.loadCurrentDriverShipment).toHaveBeenCalledOnce();
      expect(component.isMutating()).toBe(false);
    });

    component.openAction(shipments[1], 'dispatch');
    component.submitAction();
    await vi.waitFor(() => {
      expect(logistics.dispatchCurrentDriverShipment).toHaveBeenCalledOnce();
      expect(component.isMutating()).toBe(false);
    });

    component.openAction(shipments[2], 'tracking');
    component.submitAction();
    await vi.waitFor(() => {
      expect(logistics.addCurrentDriverShipmentTrackingEvent).toHaveBeenCalledOnce();
      expect(component.isMutating()).toBe(false);
    });

    component.openAction(shipments[2], 'deliver');
    component.submitAction();
    await vi.waitFor(() => {
      expect(logistics.deliverCurrentDriverShipment).toHaveBeenCalledOnce();
      expect(component.isMutating()).toBe(false);
    });

    expect(logistics.loadCurrentDriverShipment).toHaveBeenCalledWith('assigned', {
      occurredAt: '2026-07-29T10:30',
      location: 'Warehouse',
      notes: 'Ready',
    });
    expect(logistics.listCurrentDriverShipments).toHaveBeenCalledTimes(4);
  });

  it('opens inline details from the loaded collection', () => {
    const component = TestBed.createComponent(DriverShipmentsPage).componentInstance;

    component.openDetail(shipments[2]);

    expect(component.isDetailOpen()).toBe(true);
    expect(component.selectedShipment()?.id).toBe('transit');
  });
});
