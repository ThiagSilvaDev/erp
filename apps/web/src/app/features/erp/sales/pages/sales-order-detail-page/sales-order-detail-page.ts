import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, forkJoin, map, of, switchMap } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import type { WarehouseResponse } from '@generated-api/modules/inventory/dtos';
import { SalesService } from '@generated-api/modules/sales/Sales.service';
import type { SalesOrderItemResponse, SalesOrderResponse } from '@generated-api/modules/sales/dtos';
import { SalesOrderResponseStatus } from '@generated-api/modules/sales/dtos/salesOrderResponseStatus';

const PRINT_STYLES = `
  @page { size: A4 portrait; margin: 0; }
  * { box-sizing: border-box; }
  html, body { margin: 0; padding: 0; color: #172033; background: #fff; font-family: Arial, sans-serif; }
  body { font-size: 10px; }
  .order-detail { display: block; padding: 12mm; }
  .order-detail > .ant-card-body { padding: 0; }
  .order-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 0 0 14px; border-bottom: 2px solid #172033; }
  .order-heading { display: flex; align-items: center; }
  .eyebrow, .section-label, .order-status > span { color: #64748b; font-size: 8px; font-weight: 700; letter-spacing: .09em; text-transform: uppercase; }
  .order-heading h2 { margin: 1px 0 0; font-size: 20px; line-height: 1.1; }
  .order-heading p { margin: 2px 0 0; color: #64748b; font-size: 9px; }
  .order-status { display: grid; justify-items: end; gap: 4px; }
  .ant-tag { margin: 0; padding: 1px 7px; border: 1px solid #64748b; border-radius: 3px; color: #172033 !important; background: #fff !important; font-size: 9px; font-weight: 700; }
  .metric-strip { display: grid; grid-template-columns: 1.25fr repeat(3, 1fr); margin: 12px 0; border: 1px solid #cbd5e1; }
  .metric { display: block; padding: 9px 10px; border: 0; border-right: 1px solid #cbd5e1; border-radius: 0; box-shadow: none; }
  .metric:last-child { border-right: 0; }
  .ant-statistic-title { margin-bottom: 3px; color: #64748b; font-size: 8px; font-weight: 600; }
  .ant-statistic-content { font-size: 10px; font-weight: 700; overflow-wrap: anywhere; }
  .metric-total .ant-statistic-content { font-size: 14px; }
  .content-grid { display: flex; flex-direction: column; gap: 12px; }
  .detail-rail { order: -1; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
  .detail-rail .panel:last-child { grid-column: 1 / -1; }
  .panel { display: block; padding: 10px; border: 1px solid #cbd5e1; border-radius: 3px; break-inside: avoid; }
  .panel .ant-card-body { padding: 0; }
  .panel-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; margin-bottom: 7px; }
  .panel-heading h3 { margin: 1px 0 0; font-size: 11px; }
  .panel-heading p { margin: 2px 0 0; color: #64748b; font-size: 8px; }
  .panel-heading nz-icon, .count-badge { display: none; }
  .company-name { margin: -2px 0 7px; color: #64748b; font-size: 9px; }
  .ant-descriptions-view table { width: 100%; border-collapse: collapse; }
  .ant-descriptions-row { border-top: 1px solid #e2e8f0; }
  .ant-descriptions-item { padding: 0; }
  .ant-descriptions-item-container { display: flex; justify-content: space-between; gap: 12px; padding: 5px 0; }
  .ant-descriptions-item-label, .ant-descriptions-item-content { font-size: 8px; }
  .ant-descriptions-item-label { color: #64748b; }
  .ant-descriptions-item-content { max-width: 62%; font-weight: 600; text-align: right; overflow-wrap: anywhere; }
  .code-value { font-family: monospace; font-size: 7px !important; }
  .order-items, .ant-table-wrapper, .ant-table-container, .ant-table-content { overflow: visible !important; }
  .order-items table { width: 100%; min-width: 0 !important; border: 1px solid #cbd5e1; border-collapse: collapse; table-layout: fixed; }
  .order-items thead { display: table-header-group; }
  .order-items tr { break-inside: avoid; }
  .order-items th { padding: 6px 7px; color: #475569; background: #f1f5f9; border-bottom: 1px solid #94a3b8; font-size: 7px; letter-spacing: .05em; text-align: left; text-transform: uppercase; }
  .order-items td { padding: 7px; border-bottom: 1px solid #e2e8f0; font-size: 8px; vertical-align: middle; }
  .order-items tbody tr:last-child td { border-bottom: 0; }
  .product-cell { display: flex; align-items: center; gap: 7px; }
  .product-cell > div { display: grid; gap: 1px; }
  .product-cell > div > span { color: #64748b; font-size: 7px; }
  .number-cell { width: 88px; text-align: right !important; font-variant-numeric: tabular-nums; }
  .line-total { font-weight: 700; }
  .items-total { display: flex; align-items: baseline; justify-content: flex-end; gap: 24px; padding-top: 10px; }
  .items-total span { color: #64748b; font-size: 9px; }
  .items-total strong { font-size: 14px; }
  .print-footer { display: grid; grid-template-columns: 1fr 180px; gap: 40px; margin-top: 30px; padding-top: 12px; border-top: 1px solid #e2e8f0; break-inside: avoid; }
  .approval-field { display: grid; align-content: end; }
  .field-label { color: #94a3b8; font-size: 7px; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }
  .writing-line { height: 34px; border-bottom: 1px solid #475569; }
  .approval-field strong { margin-top: 5px; color: #334155; font-size: 8px; }
  .approval-field small { margin-top: 1px; color: #94a3b8; font-size: 7px; }
  .date-entry { display: grid; grid-template-columns: 36px 10px 36px 10px 1fr; align-items: end; height: 34px; }
  .date-entry span { height: 18px; border-bottom: 1px solid #475569; }
  .date-entry b { padding-bottom: 2px; color: #64748b; font-size: 9px; font-weight: 400; text-align: center; }
  .print-footer p { grid-column: 1 / -1; margin: 8px 0 0; color: #94a3b8; font-size: 7px; text-align: right; }
`;

@Component({
  selector: 'app-sales-order-detail-page',
  imports: [
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDescriptionsModule,
    NzDividerModule,
    NzEmptyModule,
    NzIconModule,
    NzPageHeaderModule,
    NzSpinModule,
    NzStatisticModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './sales-order-detail-page.html',
  styleUrl: './sales-order-detail-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SalesOrderDetailPage {
  private readonly sales = inject(SalesService);
  private readonly inventory = inject(InventoryService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);
  private readonly elementRef = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly order = signal<SalesOrderResponse | null>(null);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly printedAt = signal<string | null>(null);
  readonly warehouseCodes = signal<Record<string, string>>({});

  readonly itemCount = computed(() => this.order()?.items?.length ?? 0);
  readonly itemsTotal = computed(() => {
    const items = this.order()?.items ?? [];
    return items.reduce((sum, i) => sum + (i.quantity ?? 0) * (i.unitPriceAmount ?? 0), 0);
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id');
      if (!id) {
        this.errorMessage.set('Order ID is missing.');
        return;
      }
      this.loadOrder(id);
    });
  }

  text(value: unknown): string {
    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }
    return '';
  }

  statusColor(status: string | undefined): string {
    switch (status) {
      case SalesOrderResponseStatus.CONFIRMED:
        return 'green';
      case SalesOrderResponseStatus.CANCELLED:
        return 'red';
      case SalesOrderResponseStatus.DRAFT:
        return 'orange';
      default:
        return 'default';
    }
  }

  statusLabel(status: string | undefined): string {
    switch (status) {
      case SalesOrderResponseStatus.CONFIRMED:
        return 'Confirmed';
      case SalesOrderResponseStatus.CANCELLED:
        return 'Cancelled';
      case SalesOrderResponseStatus.DRAFT:
        return 'Draft';
      default:
        return 'Unknown';
    }
  }

  paymentLabel(method: string | undefined): string {
    switch (method) {
      case 'CASH':
        return 'Cash';
      case 'CREDIT_CARD':
        return 'Credit Card';
      case 'DEBIT_CARD':
        return 'Debit Card';
      case 'BANK_TRANSFER':
        return 'Bank Transfer';
      case 'PIX':
        return 'PIX';
      default:
        return this.text(method);
    }
  }

  itemTotal(item: SalesOrderItemResponse): number {
    return item.lineTotal ?? (item.quantity ?? 0) * (item.unitPriceAmount ?? 0);
  }

  warehouseCode(warehouseId: string | undefined): string {
    return warehouseId ? (this.warehouseCodes()[warehouseId] ?? '—') : '—';
  }

  formatCurrency(value: number | undefined | null, currencyCode?: string): string {
    if (value == null) return '-';
    if (!currencyCode) {
      return value.toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    }

    try {
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: currencyCode,
      }).format(value);
    } catch {
      return `${currencyCode} ${value.toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      })}`;
    }
  }

  print(): void {
    const printedAt = new Date().toLocaleString('pt-BR');
    this.printedAt.set(printedAt);
    this.printOrderDocument(printedAt);
  }

  backToOrders(): void {
    void this.router.navigate(['/sales-orders']);
  }

  private printOrderDocument(printedAt: string): void {
    const source = this.elementRef.nativeElement.querySelector<HTMLElement>('[data-print-source]');
    const hostWindow = this.document.defaultView;
    if (!source || !hostWindow) {
      return;
    }

    const frame = this.document.createElement('iframe');
    frame.title = 'Print sales order';
    frame.style.cssText = 'position:fixed;right:100%;bottom:0;width:1px;height:1px;border:0;';
    this.document.body.append(frame);

    const printDocument = frame.contentDocument;
    const printWindow = frame.contentWindow;
    if (!printDocument || !printWindow) {
      frame.remove();
      return;
    }

    printDocument.open();
    printDocument.write(
      `<!doctype html><html><head><meta charset="utf-8"><title></title><style>${PRINT_STYLES}</style></head><body></body></html>`,
    );
    printDocument.close();
    const printableOrder = source.cloneNode(true) as HTMLElement;
    printableOrder
      .querySelectorAll<HTMLElement>('[data-print-exclude]')
      .forEach((element) => element.remove());
    const timestamp = printableOrder.querySelector<HTMLElement>('.print-footer p');
    if (timestamp) {
      timestamp.textContent = `Printed ${printedAt}`;
    }
    printDocument.body.append(printableOrder);

    const cleanup = () => frame.remove();
    printWindow.addEventListener('afterprint', cleanup, { once: true });
    printWindow.setTimeout(() => {
      printWindow.focus();
      printWindow.print();
      hostWindow.setTimeout(cleanup, 60_000);
    });
  }

  private loadOrder(id: string): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.sales
      .getSalesOrderById(id)
      .pipe(
        switchMap((order) => {
          const warehouseIds = [
            ...(order.items ?? []).map((item) => item.warehouseId),
            order.deliveryRoute?.originWarehouseId,
          ].filter((warehouseId): warehouseId is string => Boolean(warehouseId));
          const uniqueWarehouseIds = [...new Set(warehouseIds)];

          if (!uniqueWarehouseIds.length) {
            return of({ order, warehouseCodes: {} });
          }

          return forkJoin(
            uniqueWarehouseIds.map((warehouseId) =>
              this.inventory
                .getWarehouseById(warehouseId)
                .pipe(catchError(() => of<WarehouseResponse | null>(null))),
            ),
          ).pipe(
            map((warehouses) => ({
              order,
              warehouseCodes: Object.fromEntries(
                warehouses
                  .filter(
                    (warehouse): warehouse is WarehouseResponse & { id: string; code: string } =>
                      Boolean(warehouse?.id && warehouse.code),
                  )
                  .map((warehouse) => [warehouse.id, warehouse.code]),
              ),
            })),
          );
        }),
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(),
      )
      .subscribe({
        next: ({ order, warehouseCodes }) => {
          this.warehouseCodes.set(warehouseCodes);
          this.order.set(order);
        },
        error: () => {
          this.order.set(null);
          this.warehouseCodes.set({});
          this.errorMessage.set('Unable to load sales order detail.');
        },
      });
  }
}
