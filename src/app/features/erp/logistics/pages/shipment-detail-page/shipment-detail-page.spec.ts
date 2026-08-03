import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LogisticsService } from '@generated-api/modules/logistics/Logistics.service';
import { ShipmentResponseStatus } from '@generated-api/modules/logistics/dtos';
import { ShipmentDetailPage } from './shipment-detail-page';

describe('ShipmentDetailPage', () => {
  const logistics = {
    getShipmentById: vi.fn(() =>
      of({
        id: 'shipment-1',
        shipmentNumber: 1001,
        status: ShipmentResponseStatus.IN_TRANSIT,
        driver: { id: 'driver-1', licenseNumber: 'CNH-1' },
        vehicle: { id: 'vehicle-1', plate: 'ABC-1234' },
        items: [{ productId: 'product-1', productSku: 'SKU-1', quantity: 2 }],
        trackingEvents: [{ id: 'event-1', status: ShipmentResponseStatus.IN_TRANSIT }],
      }),
    ),
  };

  function createComponent(id = 'shipment-1'): ComponentFixture<ShipmentDetailPage> {
    TestBed.configureTestingModule({
      imports: [ShipmentDetailPage],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ id })) },
        },
        { provide: LogisticsService, useValue: logistics },
      ],
    });

    return TestBed.createComponent(ShipmentDetailPage);
  }

  beforeEach(() => {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
  });

  it('loads shipment detail from the route id', () => {
    const component = createComponent().componentInstance;

    expect(logistics.getShipmentById).toHaveBeenCalledWith('shipment-1');
    expect(component.shipment()?.shipmentNumber).toBe(1001);
    expect(component.driverLabel(component.shipment()?.driver)).toBe('CNH-1');
    expect(component.vehicleLabel(component.shipment()?.vehicle)).toBe('ABC-1234');
  });

  it('shows an error when shipment detail cannot be loaded', () => {
    logistics.getShipmentById.mockReturnValueOnce(throwError(() => new Error('failed')));

    const component = createComponent().componentInstance;

    expect(component.shipment()).toBeNull();
    expect(component.errorMessage()).toBe('Unable to load shipment detail.');
  });
});
